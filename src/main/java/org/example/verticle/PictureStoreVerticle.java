package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import org.example.data.Picture;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;

public class PictureStoreVerticle extends AbstractVerticle {
    private final static String COLLECTION = "picture";
    private MongoClient client;
    private MessageDigest SHA1;

    {
        try {
            SHA1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            SHA1 = null;
        }
    }

    @Override
    public void start() {
        client = MongoClient.createShared(vertx, new JsonObject()
                .put("db_name", "my_DB"));
        vertx.eventBus().consumer("savePicture", this::savePicture);
        vertx.eventBus().consumer("getPicture", this::getPicture);
    }

    private void savePicture(Message<JsonObject> message) {
        Picture picture = message.body().mapTo(Picture.class);
        picture.setHash(SHA1.digest(picture.getPicture()));
        client.find(COLLECTION, new JsonObject().put("hash", picture.getHash()), result -> {
            Optional<JsonObject> existingPicture = result.result().stream()
                    .filter(e -> Arrays.equals(e.getBinary("picture"), picture.getPicture())).findAny();
            if (existingPicture.isPresent()) {
                message.reply(existingPicture.get().getString("_id"));
            } else {

                client.insert(COLLECTION, JsonObject.mapFrom(picture),
                        insertResult -> message.reply(insertResult.result()));
            }
        });
    }

    private void getPicture(Message<String> message) {
        client.find(COLLECTION, new JsonObject().put("_id", message.body()),
                result -> message.reply(result.result().isEmpty() ? new JsonObject() : result.result().get(0)));
    }
}
