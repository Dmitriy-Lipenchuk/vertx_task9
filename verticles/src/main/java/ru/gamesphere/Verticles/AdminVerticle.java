package ru.gamesphere.Verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import ru.gamesphere.Verticles.Util.ClanInfo;

@AllArgsConstructor
public class AdminVerticle extends AbstractVerticle {
    private final String clanName;
    private final String adminName;
    private final int userMaxQuantity;
    private final int modersMaxQuantity;
    private final boolean onlineStatus;

    @Override
    public void start(Promise<Void> startPromise){
        setClanProperties(startPromise, userMaxQuantity, modersMaxQuantity, onlineStatus);
    }

    public void setClanProperties(Promise<Void> startPromise, int usersMaxQuantity, int modersMaxQuantity, boolean status) {
        vertx.sharedData().<String, ClanInfo>getAsyncMap("clansInfo", map ->
                map.result().entries(clans -> {
                    final ClanInfo info = clans.result().get(clanName);

                    if (info == null) {
                        System.out.println("Setting clan properties failed");
                        return;
                    }

                    if (info.getAdminName() != null) {
                        System.out.println("У этого кллана уже есть админ!");
                        startPromise.fail("У этого кллана уже есть админ!");

                        return;
                    }

                    if (info.getModersList().size() > modersMaxQuantity || info.getMembersList().size() > usersMaxQuantity) {
                        System.out.println("Всем модерам необходимо перезайти в клан");
                        JsonObject jsonObject = new JsonObject().put("message", "Необходимо перезайти");
                        vertx.eventBus().send("clan.moders.all." + info.getClanName(), jsonObject);
                    }

                    if (info.getMembersList().size() > usersMaxQuantity) {
                        System.out.println("Всем юзерам необходимо перезайти в клан");
                        JsonObject jsonObject = new JsonObject().put("message", "Необходимо перезайти");
                        vertx.eventBus().send("clan.users.all." + info.getClanName(), jsonObject);
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
                                System.out.println(clanName + " параметры клана успено изменены");
                                startPromise.complete();
                            }
                    );
                })
        );
    }
}
