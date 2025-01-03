package io.github.maliciousfiles.bloodOnTheClocktower.commands;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.Game;
import io.github.maliciousfiles.bloodOnTheClocktower.play.GameInit;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class StartGameCommand extends BOTCCommand {
    @Override
    protected void doCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            error(sender, "Must specify a table");
            return;
        }

        String table = args[0];
        if (!SeatsCommand.getTables().contains(table)) {
            error(sender, "Table '" + table + "' does not exist");
            return;
        }

        // TODO: uncomment checks
//        if (Arrays.stream(args).anyMatch(a->Arrays.stream(args).skip(1).filter(v->v.equals(a)).count()>1)) {
//            error(sender, "Duplicate players provided");
//            return;
//        }

        List<Player> players = Arrays.stream(args).skip(1).map(Bukkit::getPlayer).toList();
        if (players.contains(null)) {
            error(sender, "Invalid player provided");
            return;
        }
//        if (players.size() < 5) {
//            error(sender, "Not enough players");
//            return;
//        }

        Location block = SeatsCommand.getBlock(players.getFirst().getWorld(), table);
        if (block == null) {
            error(sender, "Table must have a chopping block");
            return;
        }
        List<Location> seats = SeatsCommand.getSeats(players.getFirst().getWorld(), table);
        if (seats.size() != 15) {
            error(sender, "Table must have 15 seats to use");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                gameTasks.add(Thread.currentThread());
                GameInit.initGame(seats, block, players.getFirst(), players.subList(1, players.size()));
            } catch (ExecutionException | InterruptedException _) {}
        });
    }

    @Override
    protected Collection<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return SeatsCommand.getTables();
        }

        return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n->Arrays.stream(args).noneMatch(a->a.equalsIgnoreCase(n))).toList();
    }

    private static final List<Thread> gameTasks = new ArrayList<>();
    public static void destruct() {
        gameTasks.forEach(Thread::interrupt);
        gameTasks.clear();
    }
}
