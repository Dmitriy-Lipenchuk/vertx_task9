package ru.gamesphere.clustered;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import ru.gamesphere.Verticles.ModeratorVerticle;

public class ModeratorLauncher {
    public static void main(String[] args) {
        Vertx.clusteredVertx(
                new VertxOptions(),
                ModeratorLauncher::handle
        );
    }

    private static void handle(AsyncResult<Vertx> vertxResult) {
        vertxResult.result().deployVerticle(new ModeratorVerticle("RandomModer", "RushRoyale"));
    }
}
