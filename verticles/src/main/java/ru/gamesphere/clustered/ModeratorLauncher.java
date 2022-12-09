package ru.gamesphere.clustered;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import ru.gamesphere.Verticles.ModeratorVerticle;

public class ModeratorLauncher {
    public static void main(String[] args) {
        Vertx.clusteredVertx(
                new VertxOptions(),
                vertxResult -> {
                    final var options = new DeploymentOptions().setWorker(true);
                    vertxResult.result().deployVerticle(new ModeratorVerticle("RandomModer", "RushRoyale"), options);
                }
        );
    }
}
