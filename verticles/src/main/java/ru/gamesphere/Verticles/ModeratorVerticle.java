package ru.gamesphere.Verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
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
        Promise<Void> addModerPromise = Promise.promise();
        addModer(addModerPromise);

        Promise<Void> clanJoinComnumerPromise = Promise.promise();
        vertx.eventBus().<JsonObject>consumer(
                "clan.join." + clanName,
                this::handleUserJoinRequest
        ).completionHandler(clanJoinComnumerPromise);

        Promise<Void> clanModersAllPromise = Promise.promise();
        vertx.eventBus().consumer(
                "clan.moders.all." + clanName,
                event -> {
                    System.out.println("Админ кикнул всех из клана! Нужно быстрее перезайти чтобы хватило места");
                    reenterClan();
                }
        ).completionHandler(clanModersAllPromise);

        CompositeFuture.all(addModerPromise.future(), clanJoinComnumerPromise.future(), clanModersAllPromise.future())
                .onComplete(result -> {
                    if (result.succeeded()) {
                        startPromise.complete();
                    } else {
                        startPromise.fail(result.cause());
                    }
                });
    }

    public void addModer(Promise<Void> startPromise) {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanName, clanInfo -> {
                    final ClanInfo info = clanInfo.result();

                    if (info == null) {
                        System.out.println("Clan is not initialized");
                        startPromise.fail("Clan is not initialized");

                        return;
                    }

                    if (info.getModersList().contains(moderName)) {
                        System.out.println("Такой модер уже существует");
                        startPromise.fail("Такой модер уже существует");

                        return;
                    }

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
                            output -> {
                                System.out.println(moderName + " закреплён за " + clanName);
                                startPromise.complete();
                            }
                    );
                })
        );
    }

    private void handleUserJoinRequest(Message<JsonObject> event) {
        final JsonObject message = event.body();
        final String userName = message.getString("name");

        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanName, clanInfo -> {
                    final ClanInfo info = clanInfo.result();

                    if (info.getMembersList().size() >= info.getMaxUsers()) {
                        System.out.println("В клане нет мест");
                        event.fail(1, "В клане нет мест");

                        return;
                    }

                    if (info.getMembersList().contains(userName)) {
                        System.out.println("Такой пользователь уже есть");
                        event.fail(1, "Такой пользователь уже есть");

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
                                event.reply(json);
                                System.out.println(userName + " приняли в " + clanName);
                            }
                    );
                })
        );
    }

    private void reenterClan() {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanName, clanInfo -> {
                    final ClanInfo info = clanInfo.result();

                    if (info.getModersList().size() >= info.getMaxModerators()) {
                        System.out.println("Все слоты модеров заняты. Не успел :(");
                        vertx.eventBus().consumer("clan.moders.all." + clanName).unregister();
                        vertx.eventBus().consumer("clan.join." + clanName).unregister();

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
