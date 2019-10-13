package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.example.data.Data;
import org.example.picture.PictureType;

import java.util.Optional;

public class RouterVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer("router", this::router);
    }

    private void router(Message<String> message) {
        if (message.body() != null && !message.body().isEmpty()) {
            System.out.println("Router message: " + message.body());
            Data data = Json.decodeValue(message.body(), Data.class);
            if (data.getText() != null) {
                forwardMessage(data.getAddress(), data.getText(), null);
                vertx.eventBus().send("database.save", message.body());
            }
            Optional<PictureType> type = Optional.ofNullable(data.getPicture()).map(PictureType::detectType);
            type.ifPresent(pictureType -> vertx.eventBus().send("savePicture", new JsonObject().put("picture", data.getPicture()).put("type", pictureType.toString()),
                    r -> {
                        forwardMessage(data.getAddress(), null, r.result().body().toString());
                    }));
        }
    }

    private void forwardMessage(String address, String text, String pictureId) {
        vertx.eventBus().send("/token/" + address, new JsonObject().put("text", text).put("pictureId", pictureId).toString());
    }
}
