package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ChatComponents;
import io.github.maliciousfiles.bloodOnTheClocktower.play.ChoppingBlock;
import io.github.maliciousfiles.bloodOnTheClocktower.play.PlayerWrapper;
import io.github.maliciousfiles.bloodOnTheClocktower.play.SeatList;
import io.github.maliciousfiles.bloodOnTheClocktower.play.hooks.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
    private final List<Role> rolesInPlay;
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
        this.rolesInPlay = this.players.stream().map(BOTCPlayer::getRole).toList();
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
    public Storyteller getStoryteller() {
        return storyteller;
    }
    public List<Role> getRoles() {
        return rolesInPlay;
    }
    public ScriptInfo getScript() {
        return script;
    }

    public List<BOTCPlayer> getAwake() {
        return players.stream().filter(BOTCPlayer::isAwake).toList();
    }

    public void startGame() throws ExecutionException, InterruptedException {
        new StorytellerPauseHook(storyteller, "Continue to begin the game").get();

        setup();
        while (true) {
            runNight();
            if (isGameOver()) break;

            runDay();
            turn++;
        }
        // TODO: announce game end
    }

    private void setup() throws ExecutionException, InterruptedException {
        for (BOTCPlayer player : players) {
            player.getRole().setup();
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
            List<BOTCPlayer> demons = players.stream()
                    .filter(p->p.getRole().info.type() == Role.Type.DEMON)
                    .toList();
            List<BOTCPlayer> minions = players.stream()
                    .filter(p->p.getRole().info.type() == Role.Type.MINION)
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

            new StorytellerPauseHook(storyteller, "Give Minion information, then continue").get();
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
            List<BOTCPlayer> demons = players.stream()
                    .filter(p->p.getRole().info.type() == Role.Type.DEMON)
                    .toList();
            List<BOTCPlayer> minions = players.stream()
                    .filter(p->p.getRole().info.type() == Role.Type.MINION)
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

            Component bluffInfo = bluffs.stream().map(ChatComponents::roleInfo).reduce(Component.text("Your bluffs are "),
                    (curr, bluff) -> curr.append(bluff).append(Component.text(", ")));
            demons.forEach(d->d.giveInfo(bluffInfo));

            new StorytellerPauseHook(storyteller, "Give Demon information, then continue").get();
            demons.forEach(BOTCPlayer::sleep);
        }
    }

    private void runNight() throws ExecutionException, InterruptedException {
        if (isGameOver()) { return; }

        PriorityQueue<NightAction> nightActions = new PriorityQueue<>(Comparator.comparing(NightAction::order));

        new StorytellerPauseHook(storyteller, "Continue to Night").get();
        players.forEach(BOTCPlayer::sleep);
        for (BOTCPlayer player : players) {
            if (player.getRole().hasAbility()) player.getRole().handleDusk();
            nightActions.addAll(player.getRole().getNightActions());
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
                new StorytellerPauseHook(storyteller, "Continue to " + action.name()).get();

                storyteller.deglow();
                players.forEach(BOTCPlayer::deglow);

                action.run();
            }
        }

        if (isGameOver()) { return; }
        new StorytellerPauseHook(storyteller, "Continue to Dawn").get();
        for (BOTCPlayer player : players) {
            if (player.getRole().hasAbility()) player.getRole().handleDawn();
        }
        players.forEach(BOTCPlayer::wake);
    }

    private void runDay() throws ExecutionException, InterruptedException {
        seats.setAllCanStand(true);

        new StorytellerPauseHook(storyteller, "Continue to call to table").get();

        players.forEach(p ->
                p.getPlayer().sendTitlePart(TitlePart.TITLE, Component.text("Return to Seat", NamedTextColor.BLUE)));
        seats.setAllCanStand(false);

        storyteller.NOMINATE.enable(()-> Bukkit.getScheduler().runTaskAsynchronously(BloodOnTheClocktower.instance, () -> {
            try {
                storyteller.NOMINATE.disable();
                BOTCPlayer nominator = new PlayerChoiceHook(this, "Select the NOMINATOR").get();

                CompletableFuture<Void> instruction = storyteller.giveInstruction("Select the NOMINEE");
                BOTCPlayer nominee = new SelectPlayerHook(storyteller, this, 1, _->true).get().getFirst();
                instruction.complete(null);

                seats.setCanStand(nominator, true);
                seats.setCanStand(nominee, true);

                int votesNecessary = Mth.ceil(players.stream().filter(BOTCPlayer::isAlive).count()/2f);
                if (block.getOnTheBlock() == null) block.setVotesNecessary(votesNecessary);

                new StorytellerPauseHook(storyteller, "Continue to vote").get();

                seats.setCanStand(nominator, false);
                seats.setCanStand(nominee, false);

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

                new StorytellerPauseHook(storyteller, ("Continue to update the block ("+voteCount+" votes)").replace("1 votes", "1 vote")).get();
                if (block.getVotes() > 0) {
                    if (voteCount > block.getVotes()) {
                        block.setPlayerWithVotes(nominee, voteCount);
                    } else if (voteCount == block.getVotes()) {
                        block.clear();
                    }
                } else if (voteCount >= votesNecessary) {
                    block.setPlayerWithVotes(nominee, voteCount);
                }
                if (block.getOnTheBlock() == null) block.clear();

                players.forEach(p -> seats.setVoting(p, SeatList.VoteState.NO));

                nominee.deglow();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            storyteller.NOMINATE.enable(null);
        }));
        new StorytellerPauseHook(storyteller, "Continue to execution").get();
        storyteller.NOMINATE.disable();

        if (block.getOnTheBlock() != null) {
            new AnvilDropHook(block.getOnTheBlock().getPlayer().getLocation().add(0, 8, 0)).get();

            Bukkit.getScheduler().callSyncMethod(BloodOnTheClocktower.instance, ()->{
                block.getOnTheBlock().kill();
                return null; // it wants a return type >:c
            }).get();
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
            if (player.getRole().countsAsAlive()) {
                alive++;
                if (player.getRole().info.type() == Role.Type.DEMON) {
                    demonAlive = true;
                    break;
                }
            }
            if (player.getRole().blocksGoodVictory()) {
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
}
