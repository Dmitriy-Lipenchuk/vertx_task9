package ru.gamesphere.Verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.AllArgsConstructor;
import ru.gamesphere.Verticles.Util.ClanInfo;

@AllArgsConstructor
public class ClanVerticle extends AbstractVerticle {

    private final ClanInfo clanInfo;

    @Override
    public void start(Promise<Void> startPromise) {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanInfo.getClanName(), currentInfo -> {
                    ClanInfo info = currentInfo.result();

                    if (info != null) {
                        System.out.println("Клан с таким именем уже существует");
                        startPromise.fail("Клан с таким именем уже существует");

                        return;
                    }

                    map.result().put(clanInfo.getClanName(), clanInfo,
                            completion -> {
                                System.out.println("Клан '" + clanInfo.getClanName() + "' создан - " + clanInfo);
                                startPromise.complete();
                            });
                })
        );

        vertx.setPeriodic(3000, timer -> showClanStatus());
    }

    private void showClanStatus() {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanInfo.getClanName(), future -> System.out.println(future.result()))
        );
    }
}
