package ru.gamesphere.Verticles;

import io.vertx.core.AbstractVerticle;
import lombok.AllArgsConstructor;
import ru.gamesphere.Verticles.Util.ClanInfo;

@AllArgsConstructor
public class ClanVerticle extends AbstractVerticle {

    private final ClanInfo clanInfo;

    @Override
    public void start() {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().put(clanInfo.getClanName(), clanInfo,
                        completion -> System.out.println("Clan '" + clanInfo.getClanName() + "' created - " + clanInfo)));

        vertx.setPeriodic(3000, timer -> showClanStatus());
    }

    public void showClanStatus () {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanInfo.getClanName(), future -> System.out.println(future.result()))
        );
    }
}
