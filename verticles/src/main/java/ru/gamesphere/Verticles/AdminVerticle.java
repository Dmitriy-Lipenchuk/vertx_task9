package ru.gamesphere.Verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import ru.gamesphere.Verticles.Util.ClanInfo;

import java.util.Set;

@AllArgsConstructor
public class AdminVerticle extends AbstractVerticle {
    private final String clanName;
    private final String adminName;
    private final int usersMaxQuantity;
    private final int modersMaxQuantity;
    private boolean onlineStatus;

    @Override
    public void start(Promise<Void> startPromise) {
        setClanProperties(startPromise, usersMaxQuantity, modersMaxQuantity, onlineStatus);
    }

    @Override
    public void stop() {
        if (onlineStatus) {
            onlineStatus = false;
            setOfflineStatus();
        }
    }

    private void setClanProperties(Promise<Void> startPromise, int usersMaxQuantity, int modersMaxQuantity, boolean status) {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanName, clan -> {
                    final ClanInfo info = clan.result();

                    if (info == null) {
                        System.out.println("Setting clan properties failed");
                        startPromise.fail("Setting clan properties failed");

                        return;
                    }

                    if (info.getAdminName() != null && !info.getAdminName().equals(adminName)) {
                        System.out.println("У этого кллана уже есть админ!");
                        startPromise.fail("У этого кллана уже есть админ!");

                        return;
                    }

                    Set<String> users = info.getMembersList();
                    Set<String> moders = info.getModersList();

                    if (info.getModersList().size() > modersMaxQuantity) {
                        System.out.println("Всем модерам необходимо перезайти в клан");
                        moders.clear();
                        JsonObject jsonObject = new JsonObject().put("message", "Необходимо перезайти");
                        vertx.eventBus().publish("clan.moders.all." + info.getClanName(), jsonObject);
                    }

                    if (info.getMembersList().size() > usersMaxQuantity) {
                        System.out.println("Всем юзерам необходимо перезайти в клан");
                        users.clear();
                        JsonObject jsonObject = new JsonObject().put("message", "Необходимо перезайти")
                                .put("clanName", clanName);
                        vertx.eventBus().publish("clan.users.all", jsonObject);
                    }

                    map.result().put(clanName,
                            new ClanInfo(usersMaxQuantity,
                                    modersMaxQuantity,
                                    info.getClanName(),
                                    status,
                                    info.getMembersList(),
                                    info.getModersList(),
                                    adminName
                            ),
                            output -> {
                                System.out.println(clanName + " параметры клана успешно изменены");
                                startPromise.complete();
                            }
                    );
                })
        );
    }

    private void setOfflineStatus() {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().get(clanName, clan -> {
                    final ClanInfo info = clan.result();

                    map.result().put(clanName,
                            new ClanInfo(usersMaxQuantity,
                                    modersMaxQuantity,
                                    info.getClanName(),
                                    false,
                                    info.getMembersList(),
                                    info.getModersList(),
                                    adminName
                            ),
                            output -> System.out.println(clanName + " админ ушёл в offline")
                    );
                })
        );
    }
}
