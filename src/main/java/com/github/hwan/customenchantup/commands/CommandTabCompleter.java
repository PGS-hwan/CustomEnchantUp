package com.github.hwan.customenchantup.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import com.github.hwan.customenchantup.commands.subcommands.AbstractSubCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandTabCompleter implements TabCompleter {
    private Map<String, AbstractSubCommand> subCommands;

    public CommandTabCompleter(Map<String, AbstractSubCommand> subCommands) {
        this.subCommands = subCommands;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            List<String> possible = new ArrayList<>();
            for (Map.Entry<String, AbstractSubCommand> entry : subCommands.entrySet()) {
                if (entry.getValue().hasPermission(sender)) {
                    possible.add(entry.getKey());
                }
            }
            StringUtil.copyPartialMatches(prefix, possible, completions);
        } else if (args.length > 1) {
            String subCommandName = args[0].toLowerCase();
            AbstractSubCommand subCommand = subCommands.get(subCommandName);
            if (subCommand != null && subCommand.hasPermission(sender)) {
                List<String> subCompletions = subCommand.getTabComplete(sender, args);
                if (subCompletions != null) {
                    completions.addAll(subCompletions);
                }
            }
        }

        return completions;
    }
}