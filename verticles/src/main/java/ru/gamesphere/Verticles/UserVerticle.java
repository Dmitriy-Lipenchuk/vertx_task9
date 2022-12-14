package ru.gamesphere.Verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import ru.gamesphere.Verticles.Util.ClanInfo;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class UserVerticle extends AbstractVerticle {
    private final String username;
    private String clanName;

    @Override
    public void start() {
        joinInClan();

        vertx.eventBus().<JsonObject>consumer("user.message." + username, event -> {
                    final JsonObject jsonMessage = event.body();
                    String message = jsonMessage.getString("message");
                    String sender = jsonMessage.getString("sender");

                    System.out.println(username + " получил сообщение от " + sender + ": " + message);
                }
        );

        vertx.eventBus().<JsonObject>consumer(
                "clan.users.all",
                event -> {
                    final JsonObject jsonMessage = event.body();
                    String clanName = jsonMessage.getString("clanName");

                    if (clanName.equals(this.clanName)) {
                        System.out.println("Админ кикнул всех из клана! Нужно быстрее перезайти чтобы хватило места");
                        reenterClan();
                    } else {
                        System.out.println("Ничего себе! В клане " + clanName + " админ провёл вайп.");
                    }

                }
        );

    }

    private void joinInClan() {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().entries(clans -> {
                    if (clans.result().isEmpty()) {
                        System.out.println("Clans is not initialized");
                        return;
                    }

                    Optional<ClanInfo> clan = clans.result().values().stream()
                            .filter(ClanInfo::isAdminOnline)
                            .findAny();

                    clan.ifPresentOrElse(
                            this::joinRequest,
                            () -> System.out.println(username + " не обнаружил активных кланов :(")
                    );
                })
        );
    }

    private void joinRequest(ClanInfo info) {
        String clanName = info.getClanName();
        System.out.println("Хочу вступить в клан " + clanName + " посылаю заявку модеру");

        JsonObject jsonObject = new JsonObject().put("name", username);

        vertx.eventBus().<JsonObject>request("clan.join." + clanName, jsonObject, response -> {
            if (response.succeeded()) {
                requestApproved(clanName, response);
            } else {
                vertx.setPeriodic(5000, timer -> {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        vertx.eventBus().<JsonObject>request("clan.join." + clanName, jsonObject, innerResponse -> {
                            if (innerResponse.succeeded()) {
                                requestApproved(clanName, innerResponse);
                                vertx.cancelTimer(timer);
                            } else {
                                System.out.println("Не приняли :( Попробую подать заявку позже");
                            }
                        });
                    }
                });
            }
        });
    }

    private void requestApproved(String clanName, AsyncResult<Message<JsonObject>> response) {
        final JsonObject jsonMessage = response.result().body();
        this.clanName = jsonMessage.getString("clan");

        System.out.println("Приняли в клан " + clanName + "! Ура! Пора в рейд! Позову кого-нибудь с собой");

        vertx.setPeriodic(5000, timer -> sendMessage());
    }

    private void sendMessage() {
        if (clanName == null) {
            System.out.println("Вы не состоте ни в одном из кланов" +
                    " Чтобы послать сообщение необходимо вступить в клан");
            return;
        }

        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanName, clanInfo -> {
                    ClanInfo info = clanInfo.result();

                    Set<String> users = info.getMembersList();

                    if (users.size() > 1) {
                        String recipient = users.stream()
                                .filter(x -> !x.equals(username))
                                .findAny()
                                .get();

                        System.out.println(username + " посылает сообщение " + recipient);

                        JsonObject jsonObject = new JsonObject().put("message", recipient + " го в рейд!")
                                .put("sender", username);

                        vertx.eventBus().send("user.message." + recipient, jsonObject);
                    } else {
                        System.out.println(username + " хочет послать сообщение кому-нибудь, но он один в клане :(");
                    }
                })

        );
    }

    private void reenterClan() {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanName, clanInfo -> {
                    final ClanInfo info = clanInfo.result();

                    if (info.getMembersList().size() >= info.getMaxUsers()) {
                        System.out.println("Все места заняты. Не успел :(");
                        clanName = null;

                        return;
                    }

                    Set<String> users = info.getMembersList();
                    users.add(username);
                    map.result().put(clanName,
                            new ClanInfo(info.getMaxUsers(),
                                    info.getMaxModerators(),
                                    info.getClanName(),
                                    info.isAdminOnline(),
                                    users,
                                    info.getModersList(),
                                    info.getAdminName()
                            ),
                            output -> System.out.println(username + " зашёл заново в " + clanName)
                    );
                })
        );
    }
}
