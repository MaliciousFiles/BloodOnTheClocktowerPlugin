package io.github.maliciousfiles.bloodOnTheClocktower.commands;

import io.github.maliciousfiles.bloodOnTheClocktower.util.BOTCConfiguration;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// TODO: somehow warn the user that the seat order matters (and should be done in a circle)
public class SeatsCommand extends BOTCCommand {
    // create-table: create a table with the given name
    // delete-table: delete the table with the given name
    // get-tables: list all tables
    // add: adds a seat to the given table
    // remove: removes a seat from the given table
    // list: lists all seats in the given table
    private static final BOTCConfiguration config = BOTCConfiguration.getConfig("seats.yml");
    public static Collection<String> getTables() {
        return config.getKeys(false);
    }
    public static List<Location> getSeats(World world, String table) {
        return config.getStringList(table).stream().limit(15).map(s -> new Location(world, Integer.parseInt(s.split(",")[0]), Integer.parseInt(s.split(",")[1]), Integer.parseInt(s.split(",")[2]))).toList();
    }

    private static final List<String> subCommands = List.of("create-table", "delete-table", "get-tables", "add", "remove", "list");

    @Override
    protected void doCommand(CommandSender sender, String[] args) {
        if (args.length == 0 || !subCommands.contains(args[0].toLowerCase())) {
            error(sender, "Invalid subcommand");
            return;
        }

        if (args[0].equalsIgnoreCase("create-table")) {
            if (args.length < 2) {
                error(sender, "Must specify a name for the table");
                return;
            }
            if (config.contains(args[1])) {
                error(sender, "Table '" + args[1] + "' already exists");
                return;
            }

            config.set(args[1], List.of());
            success(sender, "Table '" + args[1] + "' created");
        } else if (args[0].equalsIgnoreCase("delete-table")) {
            if (args.length < 2) {
                error(sender, "Must specify a table to delete");
                return;
            }
            if (!config.contains(args[1])) {
                error(sender, "Table '" + args[1] + "' does not exist");
                return;
            }

            config.set(args[1], null);
            success(sender, "Table '" + args[1] + "' deleted");
        } else if (args[0].equalsIgnoreCase("get-tables")) {
            if (config.getKeys(false).isEmpty()) {
                error(sender, "No tables");
                return;
            }

            success(sender, "Tables: " + String.join(", ", config.getKeys(false)));
        } else if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 3) {
                error(sender, "Must specify a table and a location");
                return;
            }
            if (!config.contains(args[1])) {
                error(sender, "Table '" + args[1] + "' does not exist");
                return;
            }
            if (config.getStringList(args[1]).size() == 15) {
                error(sender, "Table '" + args[1] + "' is full (max 15 seats)");
                return;
            }
            if (!args[2].matches("-?\\d+,-?\\d+,-?\\d+")) {
                error(sender, "Invalid location format '"+args[2]+"', must be 'x,y,z'");
                return;
            }

            List<String> seats = config.getStringList(args[1]);

            if (seats.contains(args[2])) {
                error(sender, "Seat already exists in table '" + args[1] + "'");
                return;
            }

            seats.add(args[2]);
            config.set(args[1], seats);

            success(sender, "Seat added to table '" + args[1] + "'");
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 3) {
                error(sender, "Must specify a table and a location");
                return;
            }
            if (!config.contains(args[1])) {
                error(sender, "Table '" + args[1] + "' does not exist");
                return;
            }

            List<String> seats = config.getStringList(args[1]);

            if (!seats.contains(args[2])) {
                error(sender, "Seat does not exist in table '" + args[1] + "'");
                return;
            }

            seats.remove(args[2]);
            config.set(args[1], seats);

            success(sender, "Seat removed from table '" + args[1] + "'");
        } else if (args[0].equalsIgnoreCase("list")) {
            if (args.length < 2) {
                error(sender, "Must specify a table");
                return;
            }
            if (!config.contains(args[1])) {
                error(sender, "Table '" + args[1] + "' does not exist");
                return;
            }

            success(sender, "Seats: " + String.join(" | ", config.getStringList(args[1])));
        }

        config.save();
    }

    @Override
    protected Collection<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return subCommands;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("delete-table") || args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("list")) {
                return getTables();
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("remove")) {
                return config.getStringList(args[1]);
            }
            if (args[0].equalsIgnoreCase("add") && sender instanceof Player player) {
                if (config.contains(args[1])) {
                    RayTraceResult hit = player.rayTraceBlocks(5, FluidCollisionMode.NEVER);
                    if (hit != null && hit.getHitBlock() != null) {
                        Location loc = hit.getHitBlock().getLocation();
                        return List.of(loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
                    }
                }
            }
        }

        return List.of();
    }
}
