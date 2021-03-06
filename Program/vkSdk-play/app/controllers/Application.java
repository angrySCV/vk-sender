package controllers;

import play.libs.EventSource;
import play.libs.F;
import play.libs.Json;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import vk.VKApiConst;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Application extends Controller {


    public static Result index () {
        return ok(views.html.index.render("Hello Play Framework"));
    }

    public static Result syncFoo () {
        return ok(views.html.index.render("sync foo"));
    }

    public static F.Promise<Result> asyncFoo () {
        Map<String, String> param = new HashMap<>();
        param.put("group_id", "94535470");
        F.Promise<WS.Response> responsePromise = WS.url(VKApiConst.API_URL +"groups.getMembers")
                .setQueryParameter("group_id", "94535470")
                .setQueryParameter("fields", "sex, domain")
                .get();
        F.Promise<User> result = responsePromise.map(fromWeb -> Json.fromJson(fromWeb.asJson().get("response").get("users").get(0), User.class));

// responsePromise.map(result -> ok(views.html.index.render(result.asJson().get("response").toString())));

        return result.map(response -> ok(views.html.index.render(response.domain)));
    }

    public static F.Promise<Result> asyncNonBlockingFoo() {
        return F.Promise.delayed(() -> ok("async non-blocking foo"), 5, TimeUnit.SECONDS);
    }

    public static F.Promise<Result> reactiveRequest() {
        F.Promise<WS.Response> typesafePromise = WS.url(VKApiConst.API_URL).get();
        return typesafePromise.map(response -> ok(views.html.index.render(response.getBody())));
    }

    public static F.Promise<Result> reactiveComposition() {
        final F.Promise<WS.Response> twitterPromise = WS.url("http://www.twitter.com").get();
        final F.Promise<WS.Response> typesafePromise = WS.url("http://www.typesafe.com").get();
        return twitterPromise.flatMap((twitter) ->
                typesafePromise.map((typesafe) ->
                        ok(twitter.getBody() + typesafe.getBody())));
    }

    public static Result events() {
        EventSource eventSource = new EventSource() {
            public void onConnected() {
                sendData("hello");
            }
        };
        return ok(eventSource);
    }

    public static WebSocket<String> echo() {
        return new WebSocket<String>() {
            public void onReady(final In<String> in, final Out<String> out) {
                in.onMessage(out::write);
            }
        };
    }

}
