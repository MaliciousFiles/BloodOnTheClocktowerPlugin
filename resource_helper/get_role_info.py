import requests
import pyperclip

nightOrders = ["Apprentice", "Barista", "Bone Collector", "Bureaucrat", "Harlot", "Thief", "Philosopher", "Alchemist", "Poppy Grower", "Magician", "Snitch", "Sailor", "Engineer", "Preacher", "Poisoner", "Widow", "Courtier", "Inkeeper", "Gambler", "Snake Charmer", "Monk", "Devil's Advocate", "Evil Twin", "Witch", "Cerenovus", "Pit-Hag", "Fearmonger", "Harpy", "Mezepheles", "Scarlet Woman", "Summoner", "Lunatic", "Exorcist", "Lycanthrope", "Legion", "Imp", "Zombuul", "Pukka", "Shabaloth", "Po", "Fang Gu", "No Dashii", "Vortox", "Lord of Typhon", "Vigormortis", "Ojo", "Al-Hadikhia", "Lleech", "Lil' Monsta", "Yaggababble", "Kazali", "Pixie", "Gossip", "Sweetheart", "Banshee", "Professor", "Choirboy", "Huntsman", "Damsel", "Grandmother", "Ravenkeeper", "Empath", "Balloonist", "King", "Bounty Hunter", "Nightwatchman", "Acrobat", "Hatter", "Barber", "Sage", "Amnesiac", "Farmer", "Tinker", "Moonchild", "Fortune Teller", "Undertaker", "Dreamer", "Flowergirl", "Town Crier", "Oracle", "Seamstress", "Washerwoman", "Librarian", "Investigator", "Chef", "Steward", "Knight", "Noble", "Shugenja", "Juggler", "Village Idiot", "Cult Leader", "Butler", "Spy", "Ogre", "High Priestess", "General", "Chambermaid", "Mathematician", "Leviathan", "Vizier"]


out = ""
cmdID = 1
with open("roles.txt") as roles:
    for role in roles.read().split("\n"):
        if role == "": continue

        ID = role.lower().replace("'", "").replace(" ", "").replace("-", "")
        html = requests.get(f"https://wiki.bloodontheclocktower.com/{role.replace(' ', '_')}").text

        name = role
        title = role
        description = html.split("Summary</span>")[1].split("<p>")[1].split("</p>")[0][1:-2]
        roleType = "Role.Type."+html.split("/Character_Types#")[1].split("\"")[0].upper()
        nightOrder = nightOrders.index(role)+1 if role in nightOrders else -1
        handler = "Poisoner.class"
        out += f'{ID.upper()}("{name}", "{description}", {roleType}, {nightOrder}, {handler}, {cmdID}),\n'

        cmdID = cmdID + 1

pyperclip.copy(out[:-2]+";")
