package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Guilds;
import me.glaremasters.guilds.commands.base.CommandBase;
import org.bukkit.command.CommandSender;

import static me.glaremasters.guilds.utils.ConfigUtils.color;

/**
 * Created by GlareMasters
 * Date: 8/29/2018
 * Time: 10:34 AM
 */
public class CommandHelp extends CommandBase {

    private Guilds guilds;

    private final int MAX_PAGE_SIZE = 6;

    public CommandHelp(Guilds guilds) {
        super(guilds, "help", true, null, "[page]", 0, 1);
        this.guilds = guilds;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean nextPage = true;

        int page = 0;

        if (args.length > 0) {
            try {
                page = Integer.valueOf(args[0]);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
        for (int i = 0; i < MAX_PAGE_SIZE; i++) {

            int index = ((page - 1) * 6) + i;
            if (index > guilds.getCommandHandler().getCommands().size() - 1) {
                nextPage = false;
                break;
            }

            CommandBase command = guilds.getCommandHandler().getCommands().get(index);

            sender.sendMessage(color("{command} {arguments} - {description}").replace("{command}", command.getName()).replace("{arguments}", command.getArguments()).replace("{description}", command.getDescription()));
            sender.sendMessage(String.valueOf(page + 1));
        }
    }
}
