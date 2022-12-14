package ru.gamesphere.Verticles.Util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
@RequiredArgsConstructor
public final class ClanInfo implements Serializable {
    private final int maxUsers;
    private final int maxModerators;
    private final String clanName;
    private final boolean isAdminOnline;
    private final Set<String> membersList;
    private final Set<String> modersList;
    private final String adminName;

    public ClanInfo() {
        this.maxUsers = 10;
        this.maxModerators = 1;
        this.clanName = "DefaultClan";
        this.isAdminOnline = true;
        this.membersList = new HashSet<>();
        this.modersList = new HashSet<>();
        this.adminName = "DefaultAdmin";
    }
}
