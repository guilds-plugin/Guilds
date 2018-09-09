package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import me.glaremasters.guilds.guild.GuildRole;
import me.glaremasters.guilds.utils.ConfirmAction;
import org.bukkit.entity.Player;

import static me.glaremasters.guilds.utils.ConfigUtils.getInt;

/**
 * Created by GlareMasters
 * Date: 9/9/2018
 * Time: 2:31 PM
 */
public class CommandUpgrade extends CommandBase {

    private Guilds guilds;

    public CommandUpgrade(Guilds guilds) {
        super(guilds, "upgrade",false, null, "<name>", 0, 0);
        this.guilds = guilds;
    }

    @Override
    public void execute(Player player, String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        int tier = guild.getTier();
        GuildRole role = GuildRole.getRole(guild.getMember(player.getUniqueId()).getRole());
        if (!role.canUpgradeGuild()) return;
        if (tier >= getInt("max-number-of-tiers")) return;
        if (guild.getMembersToRankup() != 0 && guild.getMembers().size() < guild.getMembersToRankup()) return;
        double balance = guild.getBalance();
        double upgradeCost = guild.getTierCost();
        if (balance < upgradeCost) return;
        guilds.getCommandHandler().addAction(player, new ConfirmAction() {
            @Override
            public void accept() {
                if (balance < upgradeCost) return;
                // Carry over perms check
                guild.updateTier(tier + 1);
                // Add new perms
            }

            @Override
            public void decline() {
                guilds.getCommandHandler().removeAction(player);
            }
        });
    }
}
