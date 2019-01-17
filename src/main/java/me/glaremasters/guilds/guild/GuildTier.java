package me.glaremasters.guilds.guild;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GuildTier {

    private final String name;
    private final double cost;
    private final int maxMembers;
    private final int mobXpMultiplier;
    private final int damageMultiplier;
    private final double maxBankBalance;
    private final int membersToRankup;
    private final List<String> permissions;

}
