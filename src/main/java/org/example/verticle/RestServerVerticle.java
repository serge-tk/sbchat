package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.example.picture.PictureType;

import java.net.HttpURLConnection;
import java.util.Optional;

public class RestServerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        HttpServer httpServer = vertx.createHttpServer();
        Router httpRouter = Router.router(vertx);
        httpRouter.route().handler(BodyHandler.create());
        httpRouter.post("/sendMessage")
                .handler(request -> {
                    vertx.eventBus().send("router", request.getBodyAsString());
                    request.response().end("ok");
                });
        httpRouter.get("/getHistory")
                .handler(request ->
                    vertx.eventBus().send("getHistory", request.getBodyAsString(), result ->
                        request.response().end(result.result().body().toString())
                    )
                );

        httpRouter.post("/savePicture")
                .handler(request -> {
                    PictureType contentType = PictureType.detectType(request.getBody().getBytes());
                    if (contentType != null) {
                        JsonObject picture = new JsonObject().put("type", contentType.toString()).put("picture", request.getBody().getBytes());
                        vertx.eventBus().send("savePicture", picture, result ->
                                request.response().end(result.result().body().toString()));
                    } else {
                        request.response().setStatusCode(HttpURLConnection.HTTP_UNSUPPORTED_TYPE).end();
                    }
                });

        httpRouter.get("/getPicture")
                .handler(request -> {
                    String id = request.request().getParam("id");
                    vertx.eventBus().send("getPicture", id, result -> {
                        JsonObject picture = (JsonObject) result.result().body();
                        if (picture.isEmpty()) {
                            request.response().setStatusCode(HttpURLConnection.HTTP_NOT_FOUND).end();
                        } else {
                            request.response().putHeader("Content-Type", picture.getString("type"));
                            request.response().end(Buffer.buffer(picture.getBinary("picture")));
                        }
                    });
                });
        httpServer.requestHandler(httpRouter::accept);
        httpServer.listen(8081);
    }
}
