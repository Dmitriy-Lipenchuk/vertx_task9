package ru.gamesphere.clustered;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import ru.gamesphere.Verticles.UserVerticle;

public class UserLauncher {
    public static void main(String[] args) {
        Vertx.clusteredVertx(
                new VertxOptions(),
                vertxResult -> vertxResult.result().deployVerticle(new UserVerticle("Vasya3"))
        );
    }
}
