package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Created by GlareMasters on 6/28/2018.
 */
@Getter
@Builder
@AllArgsConstructor
public class GuildRole {

    private String name;
    private String node;

    private int level;

    private boolean chat, invite, kick, promote, demote, changePrefix, changeName, changeHome, removeGuild, addAlly,
            removeAlly, allyChat, openVault, transferGuild, changeStatus, activateBuff, upgradeGuild, depositMoney,
            withdrawMoney, claimLand, unclaimLand, destroy, place, interact;
    }
}
