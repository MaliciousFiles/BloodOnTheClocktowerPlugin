package io.github.maliciousfiles.bloodOnTheClocktower.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public abstract class BOTCCommand implements CommandExecutor, TabCompleter {
    protected abstract void doCommand(CommandSender sender, String[] args);
    protected abstract Collection<String> tabComplete(CommandSender sender, String[] args);

    protected void error(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message)
                .color(NamedTextColor.RED));
    }
    protected void success(CommandSender sender, String message) {
        sender.sendMessage(Component.text(message)
                .color(NamedTextColor.GREEN));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        doCommand(commandSender, strings);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return tabComplete(commandSender, strings).stream()
                .filter(str->str.toLowerCase().startsWith(strings[strings.length-1].toLowerCase()))
                .sorted().toList();
    }
}
