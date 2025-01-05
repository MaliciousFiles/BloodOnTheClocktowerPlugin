package io.github.maliciousfiles.bloodOnTheClocktower.commands;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.play.GameInit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class HaltGameCommand extends BOTCCommand {
    @Override
    protected void doCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                error(sender, "You must be a player to use this command parameter-less");
                return;
            }

            Game game = Game.getGame(player);
            if (game == null) {
                error(sender, "You are not in a game");
                return;
            }

            game.halt();
            return;
        }

        try {
            Game game = Game.getGame(UUID.fromString(args[0]));
            if (game == null) {
                error(sender, "Game not found");
                return;
            }

            game.halt();
        } catch (IllegalArgumentException e) {
            error(sender, "Invalid UUID");
        }
    }

    @Override
    protected Collection<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Game.getGames().stream().map(Game::getId).map(UUID::toString).toList();
        }

        return List.of();
    }

    private static final List<Thread> gameTasks = new ArrayList<>();
    public static void destruct() {
        gameTasks.forEach(Thread::interrupt);
        gameTasks.clear();
    }
}
