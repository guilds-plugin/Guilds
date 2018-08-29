package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.guild.Guild;
import org.bukkit.entity.Player;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public class CommandRename extends CommandBase {

    private Guilds guilds;

    public CommandRename(Guilds guilds) {
        super(guilds, "rename", false, null, "<name>", 1, 1);
        this.guilds = guilds;
    }

    @Override
    public void execute(Player player,String[] args) {
        Guild guild = Guild.getGuild(player.getUniqueId());
        if (guild == null) return;
        String name = guild.getName();
        Guilds.getGuilds().getDatabase().removeGuild(Guild.getGuild(name));
        guild.updateName(color(args[0]));
    }

}
