package ru.gamesphere.clustered;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import ru.gamesphere.Verticles.Util.ClanInfo;
import ru.gamesphere.Verticles.ClanVerticle;

import java.util.HashSet;

public class ClanLauncher {
    public static void main(String[] args) {
        Vertx.clusteredVertx(
                new VertxOptions(),
                ClanLauncher::handle
        );
    }

    private static void handle(AsyncResult<Vertx> vertxResult) {
        ClanInfo clanInfo = new ClanInfo(
                10,
                10,
                "RushRoyale",
                false,
                new HashSet<>(),
                new HashSet<>(),
                null
        );

        vertxResult.result().deployVerticle(new ClanVerticle(clanInfo));
    }
}
