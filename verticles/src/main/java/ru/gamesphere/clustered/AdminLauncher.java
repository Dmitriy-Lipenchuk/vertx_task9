package ru.gamesphere.clustered;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import ru.gamesphere.Verticles.AdminVerticle;

public class AdminLauncher {
    public static void main(String[] args) {
        Vertx.clusteredVertx(new VertxOptions(),
                AdminLauncher::handle
        );
    }

    private static void handle(AsyncResult<Vertx> vertxResult) {
        vertxResult.result().deployVerticle(new AdminVerticle(
                "RushRoyale",
                "BanHammer",
                3,
                5,
                true)
        );
    }
}
