package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ChatComponents;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ChoppingBlock;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.play.SeatList;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.TitlePart;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Game {
    private final PlayerTeam TEAM = new PlayerTeam(null, "") {
        public boolean canSeeFriendlyInvisibles() { return true; }
        public Team.CollisionRule getCollisionRule() { return Team.CollisionRule.NEVER; }
        public Collection<String> getPlayers() { return Stream.concat(players.stream(), Stream.of(storyteller)).map(PlayerWrapper::getName).toList(); }
    };

    private static final Map<UUID, Game> games = new HashMap<>();
    public static Game getGame(UUID id) {
        return games.get(id);
    }

    public enum Winner { NONE, GOOD, EVIL }

    private final Map<UUID, BOTCPlayer> mcPlayerToBOTC = new HashMap<>();
    private final UUID uuid;
    private final ScriptInfo script;
    private final SeatList seats;
    private final ChoppingBlock block;
    private final Storyteller storyteller;
    private final List<BOTCPlayer> players;

    private int turn;
    private Winner winner = Winner.NONE;

    public Game(SeatList seats, ChoppingBlock block, ScriptInfo script, Storyteller storyteller, List<BOTCPlayer> players) {
        players.forEach(p->p.setGame(this));

        this.uuid = UUID.randomUUID();
        games.put(uuid, this);

        this.seats = seats;
        this.block = block;
        this.script = script;
        this.storyteller = storyteller;
        this.players = players;
        this.turn = 1;

        players.forEach(p -> mcPlayerToBOTC.put(p.getPlayer().getUniqueId(), p));

        storyteller.setTeam(TEAM);
        players.forEach(p -> p.setTeam(TEAM));

        Component playerList = players.stream().map(p -> ChatComponents.playerInfo(p, NamedTextColor.WHITE)).reduce(Component.text("Players: "),
                (result, p) -> result.append(p).append(Component.text(", ")));
        log(playerList, LogPriority.HIGH);
    }
    public UUID getId() {
        return uuid;
    }

    public BOTCPlayer getBOTCPlayer(Player mcPlayer) {
        return mcPlayerToBOTC.get(mcPlayer.getUniqueId());
    }

    public SeatList getSeats() {
        return seats;
    }

    public int getTurn() {
        return turn;
    }
    public Storyteller getStoryteller() {
        return storyteller;
    }

    public ScriptInfo getScript() {
        return script;
    }
    public List<PlayerWrapper> getPlayers() {
        List<PlayerWrapper> list = new ArrayList<>(players);
        list.add(storyteller);

        return list;
    }

    public List<BOTCPlayer> getAwake() {
        return players.stream().filter(BOTCPlayer::isAwake).toList();
    }

    public enum LogPriority { LOW, MEDIUM, HIGH }

    public void log(Component message, LogPriority priority) {
        // TODO
        BloodOnTheClocktower.logger().info(message);
        if (priority == LogPriority.HIGH || priority == LogPriority.MEDIUM) {
            storyteller.giveInfo(message);
        }
    }

    // Include a "{n}" in message to substitute in a component for players[n]
    public void log(String message, LogPriority priority, BOTCPlayer... players) {
        message = "[BOTC] " + message;
        TextColor color = switch (priority) {
            case LOW -> NamedTextColor.DARK_GRAY;
            case MEDIUM -> NamedTextColor.GRAY;
            case HIGH -> NamedTextColor.WHITE;
        };
        log(ChatComponents.substitutePlayerInfo(message, color, players), priority);
    }

    public void startGame() throws ExecutionException, InterruptedException {
        new StorytellerPauseHook(storyteller, "begin the game").get();

        setup();
        while (true) {
            runNight();
            if (isGameOver()) break;

            runDay();
            turn++;
        }
        // TODO: announce game end
        if (winner == Winner.GOOD) {
            log("The good team has won", LogPriority.HIGH);
        } else {
            log("The evil team has won", LogPriority.HIGH);
        }
    }

    private void setup() throws ExecutionException, InterruptedException {
        log("Setup", LogPriority.LOW);
        for (BOTCPlayer player : players) {
            player.setup();
        }
    }

    public interface NightAction {
        String name();
        boolean shouldRun();
        float order();
        void run() throws ExecutionException, InterruptedException;
    }

    private class MinionInfoNightAction implements NightAction {
        @Override
        public String name() { return "Minion Info"; }

        @Override
        public boolean shouldRun() { return true; }

        @Override
        public float order() { return 10.5f; }

        @Override
        public void run() throws ExecutionException, InterruptedException {
            log("Minion info", LogPriority.LOW);
            List<BOTCPlayer> demons = players.stream()
                    .filter(p->p.getRoleInfo().type() == Role.Type.DEMON)
                    .toList();
            List<BOTCPlayer> minions = players.stream()
                    .filter(p->p.getRoleInfo().type() == Role.Type.MINION)
                    .toList();
            minions.forEach(BOTCPlayer::wake);
            for (BOTCPlayer minion : minions) {
                if (demons.size() == 0) {
                    minion.giveInfo(Component.text("There is no demon"));
                } else if (demons.size() == 1) {
                    minion.giveInfo(Component.text("The demon is " + demons.getFirst().getName()));
                } else {
                    minion.giveInfo(Component.text("The demons are " + String.join(", ", demons.stream().map(BOTCPlayer::getName).toList())));
                }
            }

            new StorytellerPauseHook(storyteller, "put Minions to sleep").get();
            minions.forEach(BOTCPlayer::sleep);
        }
    }

    private class DemonInfoNightAction implements NightAction {
        @Override
        public String name() { return "Demon Info"; }

        @Override
        public boolean shouldRun() { return true; }

        @Override
        public float order() { return 11.5f; }

        @Override
        public void run() throws ExecutionException, InterruptedException {
            log("Demon info", LogPriority.LOW);
            List<BOTCPlayer> demons = players.stream()
                    .filter(p->p.getRoleInfo().type() == Role.Type.DEMON)
                    .toList();
            List<BOTCPlayer> minions = players.stream()
                    .filter(p->p.getRoleInfo().type() == Role.Type.MINION)
                    .toList();
            demons.forEach(BOTCPlayer::wake);
            for (BOTCPlayer demon : demons) {
                if (minions.size() == 0) {
                    demon.giveInfo(Component.text("You have no minions"));
                } else if (minions.size() == 1) {
                    demon.giveInfo(Component.text("Your minion is " + minions.getFirst().getName()));
                } else {
                    demon.giveInfo(Component.text("Your minions are " + String.join(", ", minions.stream().map(BOTCPlayer::getName).toList())));
                }
            }

            List<RoleInfo> bluffs = new RoleChoiceHook(storyteller, Game.this, "Select bluffs for the demon", 3).get();
            log("Demon bluffs: " + String.join(", ", bluffs.stream().map(RoleInfo::title).toList()), LogPriority.MEDIUM);

            Component bluffInfo = bluffs.stream().map(ChatComponents::roleInfo).reduce(Component.text("Your bluffs are "),
                    (curr, bluff) -> curr.append(bluff).append(Component.text(", ")));
            demons.forEach(d->d.giveInfo(bluffInfo));

            new StorytellerPauseHook(storyteller, "put Demon to sleep").get();
            demons.forEach(BOTCPlayer::sleep);
        }
    }

    private void runNight() throws ExecutionException, InterruptedException {
        if (isGameOver()) { return; }

        log("Night " + turn, LogPriority.HIGH);

        PriorityQueue<NightAction> nightActions = new PriorityQueue<>(Comparator.comparing(NightAction::order));

        new StorytellerPauseHook(storyteller, "begin Night").get();
        players.forEach(BOTCPlayer::sleep);
        log("Dusk", LogPriority.LOW);
        for (BOTCPlayer player : players) {
            if (player.hasAbility()) player.handleDusk();
            nightActions.addAll(player.getNightActions());
        }

        // TODO: uncomment
        if (turn == 1 /*&& players.size() > 5*/) {
            nightActions.add(new MinionInfoNightAction());
            nightActions.add(new DemonInfoNightAction());
        }

        while (!nightActions.isEmpty()) {
            if (isGameOver()) { return; }

            NightAction action = nightActions.poll();
            if (action.shouldRun()) {
                StorytellerPauseHook pause = new StorytellerPauseHook(storyteller, "run " + action.name(), "skip "+action.name());
                pause.get();
                if (pause.isCancelled()) continue;

                storyteller.deglow();
                players.forEach(BOTCPlayer::deglow);

                action.run();
            }
        }

        if (isGameOver()) { return; }
        new StorytellerPauseHook(storyteller, "begin Dawn").get();
        log("Dawn", LogPriority.LOW);
        for (BOTCPlayer player : players) {
            if (player.hasAbility()) player.handleDawn();
        }
        players.forEach(BOTCPlayer::wake);
    }

    private void runDay() throws ExecutionException, InterruptedException {
        if (isGameOver()) { return; }

        log("Day " + turn, LogPriority.HIGH);

        seats.setAllCanStand(true);

        new StorytellerPauseHook(storyteller, "call to table").get();

        players.forEach(p ->
                p.getPlayer().sendTitlePart(TitlePart.TITLE, Component.text("Return to Seat", PlayerWrapper.INSTRUCTION_COLOR)));
        seats.setAllCanStand(false);

        storyteller.NOMINATE.enable(()-> Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                storyteller.CANCEL.tempDisable();
                storyteller.NOMINATE.tempDisable();
                CompletableFuture<Void> instruction = storyteller.giveInstruction("Select the NOMINATOR");
                BOTCPlayer nominator = new SelectPlayerHook(storyteller, this, 1, _->true).get().getFirst();
                instruction.complete(null);

                instruction = storyteller.giveInstruction("Select the NOMINEE");
                BOTCPlayer nominee = new SelectPlayerHook(storyteller, this, 1, _->true).get().getFirst();
                instruction.complete(null);

                // TODO: give the roles a chance to run actions
                log("{0} nominated {1}", LogPriority.MEDIUM, nominator, nominee);

                StorytellerPauseHook pause = new StorytellerPauseHook(storyteller, "allow nominator and nominee to stand", "exit nomination");
                pause.get();
                if (pause.isCancelled()) {
                    nominee.deglow();
                    return;
                }

                seats.setCanStand(nominator, true);
                seats.setCanStand(nominee, true);

                int votesNecessary = Mth.ceil(players.stream().filter(BOTCPlayer::isAlive).count()/2f);
                if (block.getOnTheBlock() == null) block.setVotesNecessary(votesNecessary);

                pause = new StorytellerPauseHook(storyteller, "tally votes", "exit nomination");
                pause.get();

                seats.setCanStand(nominator, false);
                seats.setCanStand(nominee, false);

                if (pause.isCancelled()) {
                    if (block.getOnTheBlock() == null) block.clear();
                    nominee.deglow();
                    return;
                }

                List<CompletableFuture<Void>> instructions = players.stream()
                        .filter(p->p.isAlive() || p.hasDeadVote())
                        .map(p -> p.giveInstruction("Select the Vote item to vote YES"))
                        .toList();
                List<ConfirmVoteHook> votes = players.stream().map(
                        p -> p.isAlive() || p.hasDeadVote()
                                ? new ConfirmVoteHook(p, storyteller, v->seats.setVoting(p, v))
                                : null).toList();
                CompletableFuture<Void> voteInstruction = storyteller.giveInstruction("Click players in a circle to lock their votes");

                int voteCount = 0;
                for (int start = players.indexOf(nominee)+1, i = 0; i < players.size(); i++) {
                    int idx = (start+i)%players.size();
                    BOTCPlayer player = players.get(idx);

                    boolean vote = Optional.ofNullable(votes.get(idx)).map(h -> {
                                try {
                                    return h.get();
                                } catch (ExecutionException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                    }).orElse(false);

                    if (vote) {
                        if (!player.isAlive()) player.useDeadVote();
                        voteCount++;
                    }
                }
                instructions.forEach(c -> c.complete(null));
                voteInstruction.complete(null);

                log("{0} received " + voteCount + " votes", LogPriority.MEDIUM, nominee);
                pause = new StorytellerPauseHook(storyteller, ("update the block ("+voteCount+" votes)").replace("1 votes", "1 vote"), "exit nomination");
                pause.get();

                if (!pause.isCancelled()) {
                    if (block.getVotes() > 0) {
                        if (voteCount > block.getVotes()) {
                            block.setPlayerWithVotes(nominee, voteCount);
                        } else if (voteCount == block.getVotes()) {
                            block.clear();
                        }
                    } else if (voteCount >= votesNecessary) {
                        block.setPlayerWithVotes(nominee, voteCount);
                    }
                }
                if (block.getOnTheBlock() == null) block.clear();

                players.forEach(p -> seats.setVoting(p, SeatList.VoteState.NO));

                nominee.deglow();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            storyteller.NOMINATE.enable(null);
        }));
        StorytellerPauseHook pause = new StorytellerPauseHook(storyteller, "execute the player on the block", "go to Night");
        pause.get();

        storyteller.NOMINATE.disable();

        if (!pause.isCancelled()) {
            BOTCPlayer executee = block.getOnTheBlock();
            if (executee != null) {
                log("{0} was executed", LogPriority.HIGH, executee);
                // TODO: make this happen on ALL executions (including storyteller ones)
                new AnvilDropHook(executee.getPlayer().getLocation().add(0, 8, 0)).get();

                Bukkit.getScheduler().callSyncMethod(BloodOnTheClocktower.instance, () -> {
                    executee.handleDeathAttempt(BOTCPlayer.DeathCause.EXECUTION, null);
                    return null; // it wants a return type >:c
                }).get();
            }
        }
        block.clear();
    }

    public boolean isGameOver() {
        return winner != Winner.NONE;
    }

    public void checkVictory() {
        if (isGameOver()) { return; }

        int alive = 0;
        boolean demonAlive = false;
        boolean goodVictoryBlocked = false;
        for (BOTCPlayer player : players) {
            if (player.countsAsAlive()) {
                alive++;
                if (player.getRoleInfo().type() == Role.Type.DEMON) {
                    demonAlive = true;
                    break;
                }
            }
            if (player.blocksGoodVictory()) {
                goodVictoryBlocked = true;
                break;
            }
        }

        if (!demonAlive && !goodVictoryBlocked) {
            winner = Winner.GOOD;
        } else if (alive <= 2) {
            winner = Winner.EVIL;
        }
    }

    private void cleanup() {
        players.forEach(p -> {
            p.getPlayer().setInvisible(false);
            ((CraftPlayer) p.getPlayer()).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(TEAM));
        });
    }

    public static void destruct() {
        games.forEach((_, g) -> g.cleanup());
    }
}
