package ru.gamesphere.clustered;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import ru.gamesphere.Verticles.Util.ClanInfo;
import ru.gamesphere.Verticles.ClanVerticle;

import java.util.HashSet;

public class ClanLauncher {
    public static void main(String[] args) {
        Vertx.clusteredVertx(
                new VertxOptions(),
                vertxResult -> {
                    final var options = new DeploymentOptions().setWorker(true);
                    vertxResult.result().deployVerticle(new ClanVerticle(new ClanInfo(10,
                                    10,
                                    "RushRoyale",
                                    false,
                                    new HashSet<>(),
                                    new HashSet<>(),
                                    null)),
                            options);
                }
        );
    }
}
