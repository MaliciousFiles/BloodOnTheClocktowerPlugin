package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.play.*;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
    public static Game getGame(UUID id) { return games.get(id); }
    public static Collection<Game> getGames() { return games.values(); }

    public enum Winner { NONE, GOOD, EVIL }

    private final Map<UUID, BOTCPlayer> mcPlayerToBOTC = new HashMap<>();
    private final UUID uuid;
    private final ScriptInfo script;
    private final SeatList seats;
    private final ChoppingBlock block;
    private final Storyteller storyteller;
    private final List<BOTCPlayer> players;

    private int turn;
    private boolean night;
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
    public boolean isNight() {
        return night;
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

    public enum LogPriority {
        LOW(NamedTextColor.DARK_GRAY),
        MEDIUM(NamedTextColor.GRAY),
        HIGH(TextColor.color(206, 206, 206));

        public final TextColor color;
        LogPriority(TextColor color) {
            this.color = color;
        }
    }

    public void logDirect(Component message, LogPriority priority) {
        message = Component.empty().append(Component.text("[BOTC] ", NamedTextColor.BLUE))
                .append(message.color(priority.color));

        if (priority != LogPriority.LOW) storyteller.giveInfo(message);
        BloodOnTheClocktower.logger().info(message);
    }

    // Include a "{n}" in message to substitute in a component for players[n]
    public void log(String message, BOTCPlayer source, LogPriority priority, BOTCPlayer... players) {
        Component component = ChatComponents.substitutePlayerInfo(" "+message, priority.color, players);

        if (source != null) {
            component = Component.empty().append(ChatComponents.roleInfo(source.getRoleInfo())).append(component);
        } else {
            component = Component.empty().append(Component.text("[BOTC]", NamedTextColor.BLUE)).append(component);
        }

        if (priority != LogPriority.LOW) storyteller.giveInfo(component);
        BloodOnTheClocktower.logger().info(component);
    }

    public void startGame() throws ExecutionException, InterruptedException {
        new StorytellerPauseHook(storyteller, "begin the game").get();

        setup();
        while (true) {
            night = true;
            runNight();
            night = false;
            if (isGameOver()) break;

            runDay();
            turn++;
        }

        if (winner == Winner.GOOD) {
            log("the good team has won", null, LogPriority.HIGH);
        } else {
            log("the evil team has won", null, LogPriority.HIGH);
        }

        Bukkit.getScheduler().runTask(BloodOnTheClocktower.instance, this::cleanup);
        games.remove(uuid);
    }

    private void setup() throws ExecutionException, InterruptedException {
        log("setup", null, LogPriority.LOW);
        for (BOTCPlayer player : players) {
            player.setup();
        }
    }

    public interface NightAction {
        String name();
        boolean shouldRun();
        float order();
        public List<BOTCPlayer> players();
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
        public List<BOTCPlayer> players() {
            return players.stream()
                    .filter(p->p.getRoleInfo().type() == Role.Type.MINION)
                    .toList();
        }

        @Override
        public void run() throws ExecutionException, InterruptedException {
            log("minion info", null, LogPriority.LOW);
            List<BOTCPlayer> demons = players.stream()
                    .filter(p->p.getRoleInfo().type() == Role.Type.DEMON)
                    .toList();
            List<BOTCPlayer> minions = players();

            minions.forEach(BOTCPlayer::wake);
            demons.forEach(m->m.glow(Stream.concat(minions.stream(), Stream.of(storyteller)).toList()));

            for (BOTCPlayer minion : minions) {
                if (demons.isEmpty()) {
                    minion.giveInfo(Component.text("There is no demon"));
                } else if (demons.size() == 1) {
                    minion.giveInfo(Component.text("The demon is " + demons.getFirst().getName()));
                } else {
                    minion.giveInfo(Component.text("The demons are " + String.join(", ", demons.stream().map(BOTCPlayer::getName).toList())));
                }
            }
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
        public List<BOTCPlayer> players() {
            return players.stream()
                    .filter(p->p.getRoleInfo().type() == Role.Type.DEMON)
                    .toList();
        }

        @Override
        public void run() throws ExecutionException, InterruptedException {
            log("demon info", null, LogPriority.LOW);
            List<BOTCPlayer> demons = players();
            List<BOTCPlayer> minions = players.stream()
                    .filter(p->p.getRoleInfo().type() == Role.Type.MINION)
                    .toList();

            demons.forEach(BOTCPlayer::wake);
            minions.forEach(m->m.glow(Stream.concat(demons.stream(), Stream.of(storyteller)).toList()));

            for (BOTCPlayer demon : demons) {
                if (minions.isEmpty()) {
                    demon.giveInfo(Component.text("You have no minions"));
                } else if (minions.size() == 1) {
                    demon.giveInfo(Component.text("Your minion is " + minions.getFirst().getName()));
                } else {
                    demon.giveInfo(Component.text("Your minions are " + String.join(", ", minions.stream().map(BOTCPlayer::getName).toList())));
                }
            }

            List<RoleInfo> bluffs = new RoleChoiceHook(storyteller, Game.this, "Select bluffs for the demon", 3).get();
            log("demon bluffs: " + String.join(", ", bluffs.stream().map(RoleInfo::title).toList()), null, LogPriority.MEDIUM);

            Component bluffInfo = bluffs.stream().map(ChatComponents::roleInfo).reduce(Component.text("Your bluffs are "),
                    (curr, bluff) -> curr.append(bluff).append(Component.text(", ")));
            demons.forEach(d->d.giveInfo(bluffInfo));
        }
    }

    private void runNight() throws ExecutionException, InterruptedException {
        if (isGameOver()) { return; }

        log("begin Night " + turn, null, LogPriority.HIGH);

        PriorityQueue<NightAction> nightActions = new PriorityQueue<>(Comparator.comparing(NightAction::order));

        new StorytellerPauseHook(storyteller, "begin Night").get();
        players.forEach(BOTCPlayer::sleep);
        log("dusk", null, LogPriority.LOW);
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
                MinecraftHook<Void> pause = new StorytellerPauseHook(storyteller, "run " + action.name())
                        .cancellable(storyteller.CANCEL, "skip "+action.name());
                pause.get();
                if (pause.isCancelled()) continue;

                action.players().forEach(BOTCPlayer::wake);

                action.run();

                new StorytellerPauseHook(storyteller, "put players to sleep").get();
                action.players().forEach(BOTCPlayer::sleep);
                storyteller.deglow();
                players.forEach(BOTCPlayer::deglow);
            }
        }

        if (isGameOver()) { return; }
        new StorytellerPauseHook(storyteller, "begin Dawn").get();
        log("dawn", null, LogPriority.LOW);
        for (BOTCPlayer player : players) {
            if (player.hasAbility()) player.handleDawn();
        }
        players.forEach(BOTCPlayer::wake);

        List<BOTCPlayer> shouldDie = players.stream().filter(BOTCPlayer::shouldDie).toList();
        new StorytellerPauseHook(storyteller, "show deaths ("+
                String.join(", ", shouldDie.stream().map(BOTCPlayer::getName).toList())+")").get();
        shouldDie.forEach(BOTCPlayer::die);
    }

    private void runDay() throws ExecutionException, InterruptedException {
        if (isGameOver()) { return; }

        log("begin Day " + turn, null, LogPriority.HIGH);

        seats.setAllCanStand(true);

        new StorytellerPauseHook(storyteller, "call to table").get();

        players.forEach(p ->
                p.getPlayer().sendTitlePart(TitlePart.TITLE, Component.text("Return to Seat", PlayerWrapper.INSTRUCTION_COLOR)));
        seats.setAllCanStand(false);

        storyteller.NOMINATE.enable(()-> Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                storyteller.CANCEL.tempDisable();
                storyteller.CONTINUE.tempDisable();
                storyteller.NOMINATE.tempDisable();

                CompletableFuture<Void> instruction2 = storyteller.giveInstruction("Select the NOMINEE");
                CompletableFuture<Void> instruction = storyteller.giveInstruction("Select the NOMINATOR");

                BOTCPlayer nominator = new SelectPlayerHook(storyteller, this, 1, _->true, false).get().getFirst();
                instruction.complete(null);

                BOTCPlayer nominee = new SelectPlayerHook(storyteller, this, 1, _->true).get().getFirst();
                instruction2.complete(null);

                // TODO: give the roles a chance to run actions
                log("{0} nominated {1}", null, LogPriority.MEDIUM, nominator, nominee);

                MinecraftHook<Void> pause = new StorytellerPauseHook(storyteller, "make nominator and nominee stand")
                        .cancellable(storyteller.CANCEL, "exit nomination");
                pause.get();
                if (pause.isCancelled()) {
                    nominee.deglow();
                    storyteller.NOMINATE.enable(null);
                    return;
                }

                seats.eject(nominator);
                seats.eject(nominee);

                int votesNecessary = Mth.ceil(players.stream().filter(BOTCPlayer::isAlive).count()/2f);
                if (block.getOnTheBlock() == null) block.setVotesNecessary(votesNecessary);

                pause = new StorytellerPauseHook(storyteller, "tally votes")
                        .cancellable(storyteller.CANCEL, "exit nomination");
                pause.get();

                seats.forceSit(nominator);
                seats.forceSit(nominee);

                if (pause.isCancelled()) {
                    if (block.getOnTheBlock() == null) block.clear();
                    nominee.deglow();
                    storyteller.NOMINATE.enable(null);
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

                storyteller.CANCEL.tempDisable();
                storyteller.CONTINUE.tempDisable();

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

                log("{0} received " + voteCount + " votes", null, LogPriority.MEDIUM, nominee);
                pause = new StorytellerPauseHook(storyteller, ("update the block ("+voteCount+" votes)").replace("1 votes", "1 vote"))
                        .cancellable(storyteller.CANCEL, "exit nomination");
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
                storyteller.NOMINATE.enable(null);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
        MinecraftHook<Void> pause = new StorytellerPauseHook(storyteller, "execute the player on the block")
                .cancellable(storyteller.CANCEL, "go to Night");
        pause.get();

        storyteller.NOMINATE.disable();

        if (!pause.isCancelled()) {
            BOTCPlayer executee = block.getOnTheBlock();
            if (executee != null) {
                log("{0} was executed", null, LogPriority.HIGH, executee);
                executee.execute(false);
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
                    continue;
                }
            }
            if (player.blocksGoodVictory()) {
                goodVictoryBlocked = true;
            }
        }

        if (!demonAlive && !goodVictoryBlocked) {
            winner = Winner.GOOD;
        } else if (alive <= 2) {
            winner = Winner.EVIL;
        }
    }

    private void cleanup() {
        getPlayers().forEach(p -> {
            if (p instanceof BOTCPlayer bp) bp.wake();

            p.deglow();
            p.getPlayer().setInvisible(false);
            ((CraftPlayer) p.getPlayer()).getHandle().connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(TEAM));
            p.resetInventory();
        });
        seats.cleanup();
        block.cleanup();
    }

    public static void destruct() {
        games.forEach((_, g) -> g.cleanup());
    }
}
