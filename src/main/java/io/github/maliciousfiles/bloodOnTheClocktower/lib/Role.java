package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import com.google.common.collect.ImmutableMap;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.roles.Poisoner;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.roles.Washerwoman;
import org.bukkit.Material;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface Role {
    Map<String, Class<? extends Role>> BY_ID = new ImmutableMap.Builder<String, Class<? extends Role>>()
            .put("poisoner", Poisoner.class)
            .put("washerwoman", Washerwoman.class)
            .build();
    Map<String, Integer> CMD_IDs = new ImmutableMap.Builder<String, Integer>() // custom model data IDs
            .put("alchemist", 1)
            .put("amnesiac", 2)
            .put("artist", 3)
            .put("atheist", 4)
            .put("cannibal", 5)
            .put("chambermaid", 6)
            .put("chef", 7)
            .put("choirboy", 8)
            .put("clockmaker", 9)
            .put("courtier", 10)
            .put("dreamer", 11)
            .put("empath", 12)
            .put("engineer", 13)
            .put("exorcist", 14)
            .put("farmer", 15)
            .put("flowergirl", 16)
            .put("fool", 17)
            .put("fortuneteller", 18)
            .put("gambler", 19)
            .put("general", 20)
            .put("gossip", 21)
            .put("grandmother", 22)
            .put("highpriestess", 23)
            .put("huntsman", 24)
            .put("innkeeper", 25)
            .put("investigator", 26)
            .put("juggler", 27)
            .put("king", 28)
            .put("knight", 29)
            .put("librarian", 30)
            .put("lycanthrope", 31)
            .put("magician", 32)
            .put("mathematician", 33)
            .put("mayor", 34)
            .put("minstrel", 35)
            .put("monk", 36)
            .put("noble", 37)
            .put("oracle", 38)
            .put("pacifist", 39)
            .put("philosopher", 40)
            .put("pixie", 41)
            .put("poppygrower", 42)
            .put("professor", 43)
            .put("ravenkeeper", 44)
            .put("sage", 45)
            .put("sailor", 46)
            .put("savant", 47)
            .put("seamstress", 48)
            .put("slayer", 49)
            .put("snakecharmer", 50)
            .put("soldier", 51)
            .put("steward", 52)
            .put("tealady", 53)
            .put("towncrier", 54)
            .put("undertaker", 55)
            .put("virgin", 56)
            .put("washerwoman", 57)
            .put("barber", 58)
            .put("butler", 59)
            .put("damsel", 60)
            .put("drunk", 61)
            .put("golem", 62)
            .put("goon", 63)
            .put("heretic", 64)
            .put("klutz", 65)
            .put("lunatic", 66)
            .put("moonchild", 67)
            .put("mutant", 68)
            .put("puzzlemaster", 69)
            .put("recluse", 70)
            .put("saint", 71)
            .put("snitch", 72)
            .put("sweetheart", 73)
            .put("tinker", 74)
            .put("assassin", 75)
            .put("baron", 76)
            .put("boomdandy", 77)
            .put("cerenovus", 78)
            .put("devilsadvocate", 79)
            .put("eviltwin", 80)
            .put("fearmonger", 81)
            .put("godfather", 82)
            .put("marionette", 83)
            .put("mastermind", 84)
            .put("mezepheles", 85)
            .put("organgrinder", 86)
            .put("pithag", 87)
            .put("poisoner", 88)
            .put("psychopath", 89)
            .put("scarletwoman", 90)
            .put("spy", 91)
            .put("vizier", 92)
            .put("witch", 93)
            .put("alhadikhia", 94)
            .put("fanggu", 95)
            .put("imp", 96)
            .put("legion", 97)
            .put("lleech", 98)
            .put("nodashii", 99)
            .put("po", 100)
            .put("pukka", 101)
            .put("riot", 102)
            .put("shabaloth", 103)
            .put("vigormortis", 104)
            .put("vortox", 105)
            .put("zombuul", 106)
            .put("scapegoat", 107)
            .put("gunslinger", 108)
            .put("beggar", 109)
            .put("bureaucrat", 110)
            .put("thief", 111)
            .put("butcher", 112)
            .put("bonecollector", 113)
            .put("harlot", 114)
            .put("barista", 115)
            .put("deviant", 116)
            .put("apprentice", 117)
            .put("matron", 118)
            .put("voudon", 119)
            .put("judge", 120)
            .put("bishop", 121)
            .put("gangster", 122)
            .put("doomsayer", 123)
            .put("angel", 124)
            .put("buddhist", 125)
            .put("hellslibrarian", 126)
            .put("revolutionary", 127)
            .put("fiddler", 128)
            .put("toymaker", 129)
            .put("fibbin", 130)
            .put("duchess", 131)
            .put("sentinel", 132)
            .put("spiritofivory", 133)
            .put("djinn", 134)
            .put("stormcatcher", 135)
            .build();

    String getRoleName();
    String getRoleDescription();
    Material getIcon();
    float getFirstNightOrder(); // https://docs.google.com/spreadsheets/d/1eJkBC6rF-VU6J0h0KJvyiXjs2HLl6Yjzw9jfVYHOW34/edit
    float getNormalNightOrder(); // https://docs.google.com/spreadsheets/d/1eJkBC6rF-VU6J0h0KJvyiXjs2HLl6Yjzw9jfVYHOW34/edit

    default void handleNight(BOTCPlayer me, Game game) throws InterruptedException, ExecutionException {
        if (game.getTurn() == 0 && getFirstNightOrder() >= 0 || game.getTurn() > 0 && getNormalNightOrder() >= 0) {
            me.wake();
            doNightAction(me, game);
            me.sleep();
        }
    }
    void doNightAction(BOTCPlayer me, Game game) throws InterruptedException, ExecutionException;

    enum DeathCause { STORY, EXECUTION, PLAYER }
    default boolean canDieTo(DeathCause cause, @Nullable BOTCPlayer killer, Game game) { return true; }
}
