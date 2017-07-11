package me.glaremasters.guilds.commands;

import me.glaremasters.guilds.Main;
import me.glaremasters.guilds.commands.base.CommandBase;
import me.glaremasters.guilds.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandHelp extends CommandBase {

    private final int MAX_PAGE_SIZE = 6;

    public CommandHelp() {
        super("help", "View all commands", "guilds.command.help", true, null, "[page]", 0, 1);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (Main.getInstance().getConfig().getBoolean("description.patch")) {
            Bukkit.dispatchCommand(sender, "help guilds");

        } else {
            boolean nextPage = true;

            int page = 1;

            if (args.length > 0) {
                try {
                    page = Integer.valueOf(args[0]);
                } catch (NumberFormatException ex) {
                    Message.sendMessage(sender, Message.COMMAND_ERROR_INVALID_NUMBER.replace("{input}", args[0]));
                }
            }

            for (int i = 0; i < MAX_PAGE_SIZE; i++) {

                int index = ((page - 1) * 6) + i;
                if (index > Main.getInstance().getCommandHandler().getCommands().size() - 1) {
                    nextPage = false;
                    if (i == 0) {
                        Message.sendMessage(sender, Message.COMMAND_HELP_INVALID_PAGE);
                    }

                    break;
                }

                CommandBase command = Main.getInstance().getCommandHandler().getCommands().get(index);

                Message.sendMessage(sender, Message.COMMAND_HELP_MESSAGE.replace("{command}", command.getName(), "{arguments}", command.getArguments(), "{description}", command.getDescription()));

            }

            if (nextPage) {
                Message.sendMessage(sender, Message.COMMAND_HELP_NEXT_PAGE.replace("{next-page}", String.valueOf((page + 1))));
            }
        }
    }
}
