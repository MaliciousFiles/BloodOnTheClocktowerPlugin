from PIL import Image, UnidentifiedImageError
import requests

def load(name, i, myId=None):
    if myId is None: myId = name

    Image.open(requests.get(f"https://quiz.bloodontheclocktower.com/img/characters/{name}.webp", stream=True).raw).convert("RGBA").save(f"assets/botc/textures/item/{myId}.png")
    with open(f"assets/botc/models/item/{myId}.json", "w") as f:
        f.write("""{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "botc:item/{myId}"
  }
}""".replace("{myId}", myId))

    with open("assets/minecraft/models/item/paper.json", "r+") as f:
        text = f.read()[:-6] + ("," if i != 1 else "") + """
    {
        "predicate": {
            "custom_model_data": {i}
        },
        "model": "botc:item/{myId}"
    }
  ]
}""".replace("{i}", str(i)).replace("{myId}", myId)
        
        f.seek(0)
        f.write(text)

IDs = {}

with open("roles.txt") as r:
    i = 1
    for role in r.read().split("\n"):
        role = role.lower().replace("'", "")

        rId = role.replace(" ", "_").replace("-", "_")
        rName = role.replace(" ", "").replace("-", "")

        try:
            load(rName, i, rId)
            IDs[rId] = i
            i += 1
        except UnidentifiedImageError:
            print("couldn't load "+role)    

print(IDs)
