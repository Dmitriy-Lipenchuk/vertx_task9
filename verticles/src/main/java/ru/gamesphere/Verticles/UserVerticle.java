package ru.gamesphere.Verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import ru.gamesphere.Verticles.Util.ClanInfo;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public class UserVerticle extends AbstractVerticle {
    private final String username;

    @Override
    public void start() {
        joinInClan();

        vertx.eventBus().<JsonObject>consumer("request.approved." + username, event -> {
            final JsonObject jsonMessage = event.body();
            String clanName = jsonMessage.getString("clan");

            System.out.println("Приняли в клан " + clanName + "! Ура! Пора в рейд! Позову кого-нибудь с собой");

            vertx.setPeriodic(5000, timer -> sendMessage(clanName));
        });

        vertx.eventBus().consumer("request.denied." + username, event -> {
                    System.out.println("Не приняли :( Видимо в клане нет мест. Попробую подать заявку позже");

                    vertx.setPeriodic(5000, timer -> {
                        if (ThreadLocalRandom.current().nextBoolean()) {
                            joinInClan();
                        }
                    });
                }
        );

        vertx.eventBus().<JsonObject>consumer("user.message." + username, event -> {
                    final JsonObject jsonMessage = event.body();
                    String message = jsonMessage.getString("message");
                    String sender = jsonMessage.getString("sender");

                    System.out.println(username + " получил сообщений от " + sender + ": " + message);
                }
        );
    }

    public void joinInClan() {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().entries(clans -> {
                    if (clans.result().isEmpty()) {
                        System.out.println("Clans is not initialized");
                        return;
                    }

                    Optional<ClanInfo> clan = clans.result().values().stream()
                            .filter(ClanInfo::isAdminOnline)
                            .findAny();

                    if (clan.isPresent()) {
                        String clanName = clan.get().getClanName();

                        System.out.println("Хочу вступить в клан " + clanName + " посылаю заявку модеру");

                        JsonObject jsonObject = new JsonObject().put("name", username);
                        vertx.eventBus().send("clan.join." + clanName, jsonObject);
                    } else {
                        System.out.println(username + " не обнаружил активных кланов :(");
                    }
                })
        );
    }

    public void sendMessage(String clanName) {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().entries(clans -> {
                    if (clans.result().isEmpty()) {
                        System.out.println("Clans is not initialized");
                        return;
                    }

                    final Optional<ClanInfo> clan = clans.result().values().stream()
                            .filter(x -> x.getClanName().equals(clanName))
                            .findAny();

                    Set<String> users = new HashSet<>();

                    if (clan.isPresent()) {
                        users = clan.get().getMembersList();
                    }


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

}
