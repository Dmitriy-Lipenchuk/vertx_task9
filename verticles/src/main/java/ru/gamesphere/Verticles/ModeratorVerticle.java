package ru.gamesphere.Verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import ru.gamesphere.Verticles.Util.ClanInfo;

import java.util.Set;

@AllArgsConstructor
public class ModeratorVerticle extends AbstractVerticle {
    private final String moderName;
    private final String clanName;

    @Override
    public void start(Promise<Void> startPromise) {
        addModer(startPromise);

        vertx.eventBus().<JsonObject>consumer(
                "clan.join." + clanName,
                event -> {
                    final JsonObject message = event.body();
                    final String userName = message.getString("name");

                    vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                            map.result().entries(clans -> {
                                final ClanInfo info = clans.result().get(clanName);

                                if (info.getMembersList().size() >= info.getMaxUsers()) {
                                    System.out.println("В клане нет мест");
                                    vertx.eventBus().send("request.denied." + userName, null);

                                    return;
                                }

                                Set<String> members = info.getMembersList();
                                members.add(userName);
                                map.result().put(clanName,
                                        new ClanInfo(info.getMaxUsers(),
                                                info.getMaxModerators(),
                                                info.getClanName(),
                                                info.isAdminOnline(),
                                                members,
                                                info.getModersList(),
                                                info.getAdminName()
                                        ),
                                        approve -> {
                                            JsonObject json = new JsonObject().put("clan", clanName);
                                            vertx.eventBus().send("request.approved." + userName, json);
                                            System.out.println(userName + " приняли в " + clanName);
                                        }
                                );
                            })
                    );
                }
        );
    }

    public void addModer (Promise<Void> startPromise) {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().entries(clans -> {
                    final ClanInfo info = clans.result().get(clanName);

                    if (info.getModersList().size() >= info.getMaxModerators()) {
                        System.out.println("Все слоты модеров заняты");
                        startPromise.fail("Все слоты модеров заняты");
                        return;
                    }

                    Set<String> moders = info.getModersList();
                    moders.add(moderName);
                    map.result().put(clanName,
                            new ClanInfo(info.getMaxUsers(),
                                    info.getMaxModerators(),
                                    info.getClanName(),
                                    info.isAdminOnline(),
                                    info.getMembersList(),
                                    moders,
                                    info.getAdminName()
                            ),
                            output -> System.out.println(moderName + " закреплён за " + clanName)
                    );
                })
        );
    }
}
