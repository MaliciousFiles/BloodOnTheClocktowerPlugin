package io.github.maliciousfiles.bloodOnTheClocktower.lib;

import io.github.maliciousfiles.bloodOnTheClocktower.lib.roles.Imp;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.roles.Poisoner;
import io.github.maliciousfiles.bloodOnTheClocktower.lib.roles.Washerwoman;
import io.github.maliciousfiles.bloodOnTheClocktower.util.DataComponentPair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.maliciousfiles.bloodOnTheClocktower.BloodOnTheClocktower.createItem;

public enum RoleInfo {
    ACROBAT("Acrobat", "Each night*, choose a player: if they are or become drunk or poisoned tonight, you die.", Role.Type.TOWNSFOLK, 67, Poisoner.class),
    ALCHEMIST("Alchemist", "You have a Minion ability. When using this, the Storyteller may prompt you to choose differently.", Role.Type.TOWNSFOLK, 8, Poisoner.class),
    ALSAAHIR("Alsaahir", "Each day, if you publicly guess which players are Minion(s) and which are Demon(s), good wins.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    AMNESIAC("Amnesiac", "You do not know what your ability is. Each day, privately guess what it is: you learn how accurate you are.", Role.Type.TOWNSFOLK, 71, Poisoner.class),
    ARTIST("Artist", "Once per game, during the day, privately ask the Storyteller any yes/no question.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    ATHEIST("Atheist", "The Storyteller can break the game rules, and if executed, good wins, even if you are dead. [No evil characters]", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    BALLOONIST("Balloonist", "Each night, you learn a player of a different character type than last night. [+0 or +1 Outsider]", Role.Type.TOWNSFOLK, 63, Poisoner.class),
    BANSHEE("Banshee", "If the Demon kills you, all players learn this. From now on, you may nominate twice per day and vote twice per nomination.", Role.Type.TOWNSFOLK, 55, Poisoner.class),
    BOUNTYHUNTER("Bounty Hunter", "You start knowing 1 evil player. If the player you know dies, you learn another evil player tonight. [1 Townsfolk is evil]", Role.Type.TOWNSFOLK, 65, Poisoner.class),
    CANNIBAL("Cannibal", "You have the ability of the recently killed executee. If they are evil, you are poisoned until a good player dies by execution.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    CHAMBERMAID("Chambermaid", "Each night, choose 2 alive players (not yourself): you learn how many woke tonight due to their ability.", Role.Type.TOWNSFOLK, 98, Poisoner.class),
    CHEF("Chef", "You start knowing how many pairs of evil players there are.", Role.Type.TOWNSFOLK, 85, Poisoner.class),
    CHOIRBOY("Choirboy", "If the Demon kills the King, you learn which player is the Demon. [+the King]", Role.Type.TOWNSFOLK, 57, Poisoner.class),
    CLOCKMAKER("Clockmaker", "You start knowing how many steps from the Demon to its nearest Minion.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    COURTIER("Courtier", "Once per game, at night, choose a character: they are drunk for 3 nights & 3 days.", Role.Type.TOWNSFOLK, 17, Poisoner.class),
    CULTLEADER("Cult Leader", "Each night, you become the alignment of an alive neighbor. If all good players choose to join your cult, your team wins.", Role.Type.TOWNSFOLK, 92, Poisoner.class),
    DREAMER("Dreamer", "Each night, choose a player (not yourself or Travellers): you learn 1 good & 1 evil character, 1 of which is correct.", Role.Type.TOWNSFOLK, 77, Poisoner.class),
    EMPATH("Empath", "Each night, you learn how many of your 2 alive neighbors are evil.", Role.Type.TOWNSFOLK, 62, Poisoner.class),
    ENGINEER("Engineer", "Once per game, at night, choose which Minions or which Demon is in play.", Role.Type.TOWNSFOLK, 13, Poisoner.class),
    EXORCIST("Exorcist", "Each night*, choose a player (different to last night): the Demon, if chosen, learns who you are then doesn't wake tonight.", Role.Type.TOWNSFOLK, 33, Poisoner.class),
    FARMER("Farmer", "When you die at night, an alive good player becomes a Farmer.", Role.Type.TOWNSFOLK, 72, Poisoner.class),
    FISHERMAN("Fisherman", "Once per game, during the day, visit the Storyteller for some advice to help your team win.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    FLOWERGIRL("Flowergirl", "Each night*, you learn if a Demon voted today.", Role.Type.TOWNSFOLK, 78, Poisoner.class),
    FOOL("Fool", "The 1st time you die, you don't.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    FORTUNETELLER("Fortune Teller", "Each night, choose 2 players: you learn if either is a Demon. There is a good player that registers as a Demon to you.", Role.Type.TOWNSFOLK, 75, Poisoner.class),
    GAMBLER("Gambler", "Each night*, choose a player & guess their character: if you guess wrong, you die.", Role.Type.TOWNSFOLK, 19, Poisoner.class),
    GENERAL("General", "Each night, you learn which alignment the Storyteller believes is winning: good, evil, or neither.", Role.Type.TOWNSFOLK, 97, Poisoner.class),
    GOSSIP("Gossip", "Each day, you may make a public statement. Tonight, if it was true, a player dies.", Role.Type.TOWNSFOLK, 53, Poisoner.class),
    GRANDMOTHER("Grandmother", "You start knowing a good player & their character. If the Demon kills them, you die too.", Role.Type.TOWNSFOLK, 60, Poisoner.class),
    HIGHPRIESTESS("High Priestess", "Each night, learn which player the Storyteller believes you should talk to most.", Role.Type.TOWNSFOLK, 96, Poisoner.class),
    HUNTSMAN("Huntsman", "Once per game, at night, choose a living player: the Damsel, if chosen, becomes a not-in-play Townsfolk. [+the Damsel]", Role.Type.TOWNSFOLK, 58, Poisoner.class),
    INNKEEPER("Innkeeper", "Each night*, choose 2 players: they can't die tonight, but 1 is drunk until dusk.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    INVESTIGATOR("Investigator", "You start knowing that 1 of 2 players is a particular Minion.", Role.Type.TOWNSFOLK, 84, Poisoner.class),
    JUGGLER("Juggler", "On your 1st day, publicly guess up to 5 players' characters. That night, you learn how many you got correct.", Role.Type.TOWNSFOLK, 90, Poisoner.class),
    KING("King", "Each night, if the dead equal or outnumber the living, you learn 1 alive character. The Demon knows you are the King.", Role.Type.TOWNSFOLK, 64, Poisoner.class),
    KNIGHT("Knight", "You start knowing 2 players that are not the Demon.", Role.Type.TOWNSFOLK, 87, Poisoner.class),
    LIBRARIAN("Librarian", "You start knowing that 1 of 2 players is a particular Outsider. (Or that zero are in play.)", Role.Type.TOWNSFOLK, 83, Poisoner.class),
    LYCANTHROPE("Lycanthrope", "Each night*, choose an alive player. If good, they die & the Demon doesn’t kill tonight. One good player registers as evil.", Role.Type.TOWNSFOLK, 34, Poisoner.class),
    MAGICIAN("Magician", "The Demon thinks you are a Minion. Minions think you are a Demon.", Role.Type.TOWNSFOLK, 10, Poisoner.class),
    MATHEMATICIAN("Mathematician", "Each night, you learn how many players' abilities worked abnormally (since dawn) due to another character's ability.", Role.Type.TOWNSFOLK, 99, Poisoner.class),
    MAYOR("Mayor", "If only 3 players live & no execution occurs, your team wins. If you die at night, another player might die instead.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    MINSTREL("Minstrel", "When a Minion dies by execution, all other players (except Travellers) are drunk until dusk tomorrow.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    MONK("Monk", "Each night*, choose a player (not yourself): they are safe from the Demon tonight.", Role.Type.TOWNSFOLK, 21, Poisoner.class),
    NIGHTWATCHMAN("Nightwatchman", "Once per game, at night, choose a player: they learn you are the Nightwatchman.", Role.Type.TOWNSFOLK, 66, Poisoner.class),
    NOBLE("Noble", "You start knowing 3 players, 1 and only 1 of which is evil.", Role.Type.TOWNSFOLK, 88, Poisoner.class),
    ORACLE("Oracle", "Each night*, you learn how many dead players are evil.", Role.Type.TOWNSFOLK, 80, Poisoner.class),
    PACIFIST("Pacifist", "Executed good players might not die.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    PHILOSOPHER("Philosopher", "Once per game, at night, choose a good character: gain that ability. If this character is in play, they are drunk.", Role.Type.TOWNSFOLK, 7, Poisoner.class),
    PIXIE("Pixie", "You start knowing 1 in-play Townsfolk. If you were mad that you were this character, you gain their ability when they die.", Role.Type.TOWNSFOLK, 52, Poisoner.class),
    POPPYGROWER("Poppy Grower", "Minions & Demons do not know each other. If you die, they learn who each other are that night.", Role.Type.TOWNSFOLK, 9, Poisoner.class),
    PREACHER("Preacher", "Each night, choose a player: a Minion, if chosen, learns this. All chosen Minions have no ability.", Role.Type.TOWNSFOLK, 14, Poisoner.class),
    PROFESSOR("Professor", "Once per game, at night*, choose a dead player: if they are a Townsfolk, they are resurrected.", Role.Type.TOWNSFOLK, 56, Poisoner.class),
    RAVENKEEPER("Ravenkeeper", "If you die at night, you are woken to choose a player: you learn their character.", Role.Type.TOWNSFOLK, 61, Poisoner.class),
    SAGE("Sage", "If the Demon kills you, you learn that it is 1 of 2 players.", Role.Type.TOWNSFOLK, 70, Poisoner.class),
    SAILOR("Sailor", "Each night, choose an alive player: either you or they are drunk until dusk. You can't die.", Role.Type.TOWNSFOLK, 12, Poisoner.class),
    SAVANT("Savant", "Each day, you may visit the Storyteller to learn 2 things in private: 1 is true & 1 is false.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    SEAMSTRESS("Seamstress", "Once per game, at night, choose 2 players (not yourself): you learn if they are the same alignment.", Role.Type.TOWNSFOLK, 81, Poisoner.class),
    SHUGENJA("Shugenja", "You start knowing if your closest evil player is clockwise or anti-clockwise. If equidistant, this info is arbitrary.", Role.Type.TOWNSFOLK, 89, Poisoner.class),
    SLAYER("Slayer", "Once per game, during the day, publicly choose a player: if they are the Demon, they die.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    SNAKECHARMER("Snake Charmer", "Each night, choose an alive player: a chosen Demon swaps characters & alignments with you & is then poisoned.", Role.Type.TOWNSFOLK, 20, Poisoner.class),
    SOLDIER("Soldier", "You are safe from the Demon.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    STEWARD("Steward", "You start knowing 1 good player.", Role.Type.TOWNSFOLK, 86, Poisoner.class),
    TEALADY("Tea Lady", "If both your alive neighbors are good, they can't die.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    TOWNCRIER("Town Crier", "Each night*, you learn if a Minion nominated today.", Role.Type.TOWNSFOLK, 79, Poisoner.class),
    UNDERTAKER("Undertaker", "Each night*, you learn which character died by execution today.", Role.Type.TOWNSFOLK, 76, Poisoner.class),
    VILLAGEIDIOT("Village Idiot", "Each night, choose a player: you learn their alignment. [+0 to +2 Village Idiots. 1 of the extras is drunk]", Role.Type.TOWNSFOLK, 91, Poisoner.class),
    VIRGIN("Virgin", "The 1st time you are nominated, if the nominator is a Townsfolk, they are executed immediately.", Role.Type.TOWNSFOLK, -1, Poisoner.class),
    WASHERWOMAN("Washerwoman", "You start knowing that 1 of 2 players is a particular Townsfolk.", Role.Type.TOWNSFOLK, 82, Washerwoman.class),
    BARBER("Barber", "If you died today or tonight, the Demon may choose 2 players (not another Demon) to swap characters.", Role.Type.OUTSIDER, 69, Poisoner.class),
    BUTLER("Butler", "Each night, choose a player (not yourself): tomorrow, you may only vote if they are voting too.", Role.Type.OUTSIDER, 93, Poisoner.class),
    DAMSEL("Damsel", "All Minions know a Damsel is in play. If a Minion publicly guesses you (once), your team loses.", Role.Type.OUTSIDER, 59, Poisoner.class),
    DRUNK("Drunk", "You do not know you are the Drunk. You think you are a Townsfolk character, but you are not.", Role.Type.OUTSIDER, -1, Poisoner.class),
    GOLEM("Golem", "You may only nominate once per game. When you do, if the nominee is not the Demon, they die.", Role.Type.OUTSIDER, -1, Poisoner.class),
    GOON("Goon", "Each night, the 1st player to choose you with their ability is drunk until dusk. You become their alignment.", Role.Type.OUTSIDER, -1, Poisoner.class),
    HATTER("Hatter", "If you died today or tonight, the Minion & Demon players may choose new Minion & Demon characters to be.", Role.Type.OUTSIDER, 68, Poisoner.class),
    HERETIC("Heretic", "Whoever wins, loses & whoever loses, wins, even if you are dead.", Role.Type.OUTSIDER, -1, Poisoner.class),
    KLUTZ("Klutz", "When you learn that you died, publicly choose 1 alive player: if they are evil, your team loses.", Role.Type.OUTSIDER, -1, Poisoner.class),
    LUNATIC("Lunatic", "You think you are a Demon, but you are not. The Demon knows who you are & who you choose at night.", Role.Type.OUTSIDER, 32, Poisoner.class),
    MOONCHILD("Moonchild", "When you learn that you died, publicly choose 1 alive player. Tonight, if it was a good player, they die.", Role.Type.OUTSIDER, 74, Poisoner.class),
    MUTANT("Mutant", "If you are \"mad\" about being an Outsider, you might be executed.", Role.Type.OUTSIDER, -1, Poisoner.class),
    OGRE("Ogre", "On your 1st night, choose a player (not yourself): you become their alignment (you don't know which) even if drunk or poisoned.", Role.Type.OUTSIDER, 95, Poisoner.class),
    PLAGUEDOCTOR("Plague Doctor", "When you die, the Storyteller gains a Minion ability.", Role.Type.OUTSIDER, -1, Poisoner.class),
    POLITICIAN("Politician", "If you were the player most responsible for your team losing, you change alignment & win, even if dead.", Role.Type.OUTSIDER, -1, Poisoner.class),
    PUZZLEMASTER("Puzzlemaster", "1 player is drunk, even if you die. If you guess (once) who it is, learn the Demon player, but guess wrong & get false info.", Role.Type.OUTSIDER, -1, Poisoner.class),
    RECLUSE("Recluse", "You might register as evil & as a Minion or Demon, even if dead.", Role.Type.OUTSIDER, -1, Poisoner.class),
    SAINT("Saint", "If you die by execution, your team loses.", Role.Type.OUTSIDER, -1, Poisoner.class),
    SNITCH("Snitch", "Each Minion gets 3 bluffs.", Role.Type.OUTSIDER, 11, Poisoner.class),
    SWEETHEART("Sweetheart", "When you die, 1 player is drunk from now on.", Role.Type.OUTSIDER, 54, Poisoner.class),
    TINKER("Tinker", "You might die at any time.", Role.Type.OUTSIDER, 73, Poisoner.class),
    ZEALOT("Zealot", "If there are 5 or more players alive, you must vote for every nomination.", Role.Type.OUTSIDER, -1, Poisoner.class),
    ASSASSIN("Assassin", "Once per game, at night*, choose a player: they die, even if for some reason they could not.", Role.Type.MINION, -1, Poisoner.class),
    BARON("Baron", "There are extra Outsiders in play. [+2 Outsiders]", Role.Type.MINION, -1, Poisoner.class),
    BOFFIN("Boffin", "The Demon (even if drunk or poisoned) has a not-in-play good character's ability. You both know which.", Role.Type.MINION, -1, Poisoner.class),
    BOOMDANDY("Boomdandy", "If you are executed, all but 3 players die. After a 10 to 1 countdown, the player with the most players pointing at them, dies.", Role.Type.MINION, -1, Poisoner.class),
    CERENOVUS("Cerenovus", "Each night, choose a player & a good character: they are \"mad\" they are this character tomorrow, or might be executed.", Role.Type.MINION, 25, Poisoner.class),
    DEVILSADVOCATE("Devil's Advocate", "Each night, choose a living player (different to last night): if executed tomorrow, they don't die.", Role.Type.MINION, 22, Poisoner.class),
    EVILTWIN("Evil Twin", "You & an opposing player know each other. If the good player is executed, evil wins. Good can't win if you both live.", Role.Type.MINION, 23, Poisoner.class),
    FEARMONGER("Fearmonger", "Each night, choose a player: if you nominate & execute them, their team loses. All players know if you choose a new player.", Role.Type.MINION, 27, Poisoner.class),
    GOBLIN("Goblin", "If you publicly claim to be the Goblin when nominated & are executed that day, your team wins.", Role.Type.MINION, -1, Poisoner.class),
    GODFATHER("Godfather", "You start knowing which Outsiders are in play. If 1 died today, choose a player tonight: they die. [-1 or +1 Outsider]", Role.Type.MINION, -1, Poisoner.class),
    HARPY("Harpy", "Each night, choose 2 players: tomorrow, the 1st player is mad that the 2nd is evil, or one or both might die.", Role.Type.MINION, 28, Poisoner.class),
    MARIONETTE("Marionette", "You think you are a good character, but you are not. The Demon knows who you are. [You neighbor the Demon]", Role.Type.MINION, -1, Poisoner.class),
    MASTERMIND("Mastermind", "If the Demon dies by execution (ending the game), play for 1 more day. If a player is then executed, their team loses.", Role.Type.MINION, -1, Poisoner.class),
    MEZEPHELES("Mezepheles", "You start knowing a secret word. The 1st good player to say this word becomes evil that night.", Role.Type.MINION, 29, Poisoner.class),
    ORGANGRINDER("Organ Grinder", "All players keep their eyes closed when voting & the vote tally is secret. Each night, choose if you are drunk or not.", Role.Type.MINION, -1, Poisoner.class),
    PITHAG("Pit-Hag", "Each night*, choose a player & a character they become (if not in play). If a Demon is made, deaths tonight are arbitrary.", Role.Type.MINION, 26, Poisoner.class),
    POISONER("Poisoner", "Each night, choose a player: they are poisoned tonight and tomorrow day.", Role.Type.MINION, 15, Poisoner.class),
    PSYCHOPATH("Psychopath", "Each day, before nominations, you may publicly choose a player: they die. If executed, you only die if you lose roshambo.", Role.Type.MINION, -1, Poisoner.class),
    SCARLETWOMAN("Scarlet Woman", "If there are 5 or more players alive & the Demon dies, you become the Demon. (Travellers don't count)", Role.Type.MINION, 30, Poisoner.class),
    SPY("Spy", "Each night, you see the Grimoire. You might register as good & as a Townsfolk or Outsider, even if dead.", Role.Type.MINION, 94, Poisoner.class),
    SUMMONER("Summoner", "You get 3 bluffs. On the 3rd night, choose a player: they become an evil Demon of your choice. [No Demon]", Role.Type.MINION, 31, Poisoner.class),
    VIZIER("Vizier", "All players know you are the Vizier. You can not die during the day. If good voted, you may choose to execute immediately.", Role.Type.MINION, 101, Poisoner.class),
    WIDOW("Widow", "On your first night, look at the Grimoire & choose a player: they are poisoned. 1 good player knows a Widow is in play.", Role.Type.MINION, 16, Poisoner.class),
    WITCH("Witch", "Each night, choose a player: if they nominate tomorrow, they die. If just 3 players live, you lose this ability.", Role.Type.MINION, 24, Poisoner.class),
    XAAN("Xaan", "On night X, all Townsfolk are poisoned until dusk. [X Outsiders]", Role.Type.MINION, -1, Poisoner.class),
    ALHADIKHIA("Al-Hadikhia", "Each night*, you may choose 3 players (all players learn who): each silently chooses to live or die, but if all live, all die.", Role.Type.DEMON, 47, Poisoner.class),
    FANGGU("Fang Gu", "Each night*, choose a player: they die. The 1st Outsider this kills becomes an evil Fang Gu & you die instead. [+1 Outsider]", Role.Type.DEMON, 41, Poisoner.class),
    IMP("Imp", "Each night*, choose a player: they die. If you kill yourself this way, a Minion becomes the Imp.", Role.Type.DEMON, 36, Imp.class),
    KAZALI("Kazali", "Each night*, choose a player: they die. [You choose which players are which Minions. -? to +? Outsiders]", Role.Type.DEMON, 51, Poisoner.class),
    LEGION("Legion", "Each night*, a player might die. Executions fail if only evil voted. You register as a Minion too. [Most players are Legion]", Role.Type.DEMON, 35, Poisoner.class),
    LEVIATHAN("Leviathan", "If more than 1 good player is executed, evil wins. All players know you are in play. After day 5, evil wins.", Role.Type.DEMON, 100, Poisoner.class),
    LILMONSTA("Lil' Monsta", "Each night, Minions choose who babysits Lil' Monsta & 'is the Demon'. Each night*, a player might die. [+1 Minion]", Role.Type.DEMON, 49, Poisoner.class),
    LLEECH("Lleech", "Each night*, choose a player: they die. You start by choosing a player: they are poisoned. You die if & only if they are dead.", Role.Type.DEMON, 48, Poisoner.class),
    LORDOFTYPHON("Lord of Typhon", "Each night*, choose a player: they die. [Evil characters are in a line. You are in the middle. +1 Minion. -? to +? Outsiders]", Role.Type.DEMON, 44, Poisoner.class),
    NODASHII("No Dashii", "Each night*, choose a player: they die. Your 2 Townsfolk neighbors are poisoned.", Role.Type.DEMON, 42, Poisoner.class),
    OJO("Ojo", "Each night*, choose a character: they die. If they are not in play, the Storyteller chooses who dies.", Role.Type.DEMON, 46, Poisoner.class),
    PO("Po", "Each night*, you may choose a player: they die. If your last choice was no-one, choose 3 players tonight.", Role.Type.DEMON, 40, Poisoner.class),
    PUKKA("Pukka", "Each night, choose a player: they are poisoned. The previously poisoned player dies then becomes healthy.", Role.Type.DEMON, 38, Poisoner.class),
    RIOT("Riot", "On day 3, Minions become Riot & nominees die but nominate an alive player immediately. This must happen.", Role.Type.DEMON, -1, Poisoner.class),
    SHABALOTH("Shabaloth", "Each night*, choose 2 players: they die. A dead player you chose last night might be regurgitated.", Role.Type.DEMON, 39, Poisoner.class),
    VIGORMORTIS("Vigormortis", "Each night*, choose a player: they die. Minions you kill keep their ability & poison 1 Townsfolk neighbor. [-1 Outsider]", Role.Type.DEMON, 45, Poisoner.class),
    VORTOX("Vortox", "Each night*, choose a player: they die. Townsfolk abilities yield false info. Each day, if no-one is executed, evil wins.", Role.Type.DEMON, 43, Poisoner.class),
    YAGGABABBLE("Yaggababble", "You start knowing a secret phrase. For each time you said it publicly today, a player might die.", Role.Type.DEMON, 50, Poisoner.class),
    ZOMBUUL("Zombuul", "Each night*, if no-one died today, choose a player: they die. The 1st time you die, you live but register as dead.", Role.Type.DEMON, 37, Poisoner.class),
    SCAPEGOAT("Scapegoat", "If a player of your alignment is executed, you might be executed instead.", Role.Type.TRAVELLER, -1, Poisoner.class),
    GUNSLINGER("Gunslinger", "Each day, after the 1st vote has been tallied, you may choose a player that voted: they die.", Role.Type.TRAVELLER, -1, Poisoner.class),
    BEGGAR("Beggar", "You must use a vote token to vote. If a dead player gives you theirs, you learn their alignment. You are sober and healthy.", Role.Type.TRAVELLER, -1, Poisoner.class),
    BUREAUCRAT("Bureaucrat", "Each night, choose a player (not yourself): their vote counts as 3 votes tomorrow.", Role.Type.TRAVELLER, 4, Poisoner.class),
    THIEF("Thief", "Each night, choose a player (not yourself): their vote counts negatively tomorrow.", Role.Type.TRAVELLER, 6, Poisoner.class),
    BUTCHER("Butcher", "Each day, after the 1st execution, you may nominate again.", Role.Type.TRAVELLER, -1, Poisoner.class),
    BONECOLLECTOR("Bone Collector", "Once per game, at night*, choose a dead player: they regain their ability until dusk.", Role.Type.TRAVELLER, 3, Poisoner.class),
    HARLOT("Harlot", "Each night*, choose a living player: if they agree, you learn their character, but you both might die.", Role.Type.TRAVELLER, 5, Poisoner.class),
    BARISTA("Barista", "Each night, until dusk, 1) a player becomes sober, healthy & gets true info, or 2) their ability works twice. They learn which.", Role.Type.TRAVELLER, 2, Poisoner.class),
    DEVIANT("Deviant", "If you were funny today, you cannot die by exile.", Role.Type.TRAVELLER, -1, Poisoner.class),
    APPRENTICE("Apprentice", "On your 1st night, you gain a Townsfolk ability (if good) or a Minion ability (if evil).", Role.Type.TRAVELLER, 1, Poisoner.class),
    MATRON("Matron", "Each day, you may choose up to 3 sets of 2 players to swap seats. Players may not leave their seats to talk in private.", Role.Type.TRAVELLER, -1, Poisoner.class),
    VOUDON("Voudon", "Only you & the dead can vote. They don't need a vote token to do so. A 50% majority isn't required.", Role.Type.TRAVELLER, -1, Poisoner.class),
    JUDGE("Judge", "Once per game, if another player nominated, you may choose to force the current execution to pass or fail.", Role.Type.TRAVELLER, -1, Poisoner.class),
    BISHOP("Bishop", "Only the Storyteller can nominate. At least 1 opposing player must be nominated each day.", Role.Type.TRAVELLER, -1, Poisoner.class),
    GANGSTER("Gangster", "Once per day, you may choose to kill an alive neighbor, if your other alive neighbor agrees.", Role.Type.TRAVELLER, -1, Poisoner.class),
    DOOMSAYER("Doomsayer", "If 4 or more players live, each living player may publicly choose (once per game) that a player of their own alignment dies.", Role.Type.FABLED, -1, Poisoner.class),
    ANGEL("Angel", "Something bad might happen to whoever is most responsible for the death of a new player.", Role.Type.FABLED, -1, Poisoner.class),
    BUDDHIST("Buddhist", "For the first 2 minutes of each day, veteran players may not talk.", Role.Type.FABLED, -1, Poisoner.class),
    HELLSLIBRARIAN("Hell's Librarian", "Something bad might happen to whoever talks when the Storyteller has asked for silence.", Role.Type.FABLED, -1, Poisoner.class),
    REVOLUTIONARY("Revolutionary", "2 neighboring players are known to be the same alignment. Once per game, 1 of them registers falsely.", Role.Type.FABLED, -1, Poisoner.class),
    FIDDLER("Fiddler", "Once per game, the Demon secretly chooses an opposing player: all players choose which of these 2 players win.", Role.Type.FABLED, -1, Poisoner.class),
    TOYMAKER("Toymaker", "The Demon may choose not to attack & must do this at least once per game. Evil players get normal starting info.", Role.Type.FABLED, -1, Poisoner.class),
    FIBBIN("Fibbin", "Once per game, 1 good player might get incorrect information.", Role.Type.FABLED, -1, Poisoner.class),
    DUCHESS("Duchess", "Each day, 3 players may choose to visit you. At night*, each visitor learns how many visitors are evil, but 1 gets false info.", Role.Type.FABLED, -1, Poisoner.class),
    SENTINEL("Sentinel", "There might be 1 extra or 1 fewer Outsider in play.", Role.Type.FABLED, -1, Poisoner.class),
    SPIRITOFIVORY("Spirit of Ivory", "There can't be more than 1 extra evil player.", Role.Type.FABLED, -1, Poisoner.class),
    DJINN("Djinn", "Use the Djinn's special rule. All players know what it is.", Role.Type.FABLED, -1, Poisoner.class),
    BOOTLEGGER("Bootlegger", "This script has homebrew characters or rules.", Role.Type.FABLED, -1, Poisoner.class),
    FERRYMAN("Ferryman", "On the final day, all dead players regain their vote token.", Role.Type.FABLED, -1, Poisoner.class),
    GARDENER("Gardener", "The Storyteller assigns 1 or more players' characters.", Role.Type.FABLED, -1, Poisoner.class),
    STORMCATCHER("Storm Catcher", "Name a good character. If in play, they can only die by execution, but evil players learn which player it is.", Role.Type.FABLED, -1, Poisoner.class);

    private final String title, description;
    private final Role.Type type;
    private final float nightOrder; // https://docs.google.com/spreadsheets/d/1eJkBC6rF-VU6J0h0KJvyiXjs2HLl6Yjzw9jfVYHOW34/edit?gid=0#gid=0
    private final Class<? extends Role> handler;

    RoleInfo(String title, String description, Role.Type type, float nightOrder, Class<? extends Role> handler) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.nightOrder = nightOrder;
        this.handler = handler;
    }

    public String id() {
        return name().toLowerCase();
    }
    public String title() {
        return title;
    }
    public String description() {
        return description;
    }
    public Role.Type type() {
        return type;
    }
    public BOTCPlayer.Alignment alignment() { return (type == Role.Type.MINION || type == Role.Type.DEMON) ? BOTCPlayer.Alignment.EVIL : BOTCPlayer.Alignment.GOOD; }
    public float nightOrder() {
        return nightOrder;
    }

    public <T> T getInstance(BOTCPlayer me, Game game) {
        try {
            return (T) handler.getConstructor(BOTCPlayer.class, Game.class, RoleInfo.class).newInstance(me, game, this);
        } catch (ClassCastException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static final Map<Role.Type, TextColor> ROLE_COLORS = Map.of(
            Role.Type.TOWNSFOLK, TextColor.color(0, 190, 239),
            Role.Type.OUTSIDER, TextColor.color(1, 68, 194),
            Role.Type.MINION, TextColor.color(221, 20, 24),
            Role.Type.DEMON, TextColor.color(119, 13, 14),
            Role.Type.FABLED, TextColor.color(255, 205, 0)
    );
    public ItemStack getItem() {
        return getItem(null, true);
    }

    public ItemStack getItem(String nameOverride, boolean showDescription) {
        String name = nameOverride == null ? title : nameOverride;

        List<Component> lore = new ArrayList<>();
        if (showDescription) {
            int i = 0;
            while (i < description.length()) {
                int endIdx = i+50;
                if (endIdx < description.length()) {
                    while (endIdx > i && description.charAt(endIdx-1) != ' ') endIdx--;
                } else {
                    endIdx = description.length();
                }

                lore.add(Component.text(description.substring(i, endIdx))
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.GRAY));
                i = endIdx;
            }
        }

        return createItem(Material.PAPER,
                DataComponentPair.name(ROLE_COLORS.containsKey(type)
                        ? Component.text(name, ROLE_COLORS.get(type))
                        : Component.text(name.substring(0, name.length()/2), ROLE_COLORS.get(Role.Type.TOWNSFOLK))
                            .append(Component.text(name.substring(name.length()/2), ROLE_COLORS.get(Role.Type.MINION)))),
                DataComponentPair.lore(lore.toArray(Component[]::new)),
                DataComponentPair.model("role"),
                DataComponentPair.cmd(id()));
    }
}
