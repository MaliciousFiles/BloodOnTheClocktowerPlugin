from PIL import Image, UnidentifiedImageError
import requests

def load(name, i):
    Image.open(requests.get(f"https://quiz.bloodontheclocktower.com/img/characters/{name}.webp", stream=True).raw).convert("RGBA").save(f"../BOTC Resource Pack/assets/botc/textures/item/{name}.png")
    with open(f"../BOTC Resource Pack/assets/botc/models/item/{name}.json", "w") as f:
        f.write("""{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "botc:item/{name}"
  }
}""".replace("{name}", name))

    with open("../BOTC Resource Pack/assets/minecraft/models/item/paper.json", "r+") as f:
        text = f.read()[:-6] + ("," if i != 1 else "") + """
    {
        "predicate": {
            "custom_model_data": {i}
        },
        "model": "botc:item/{name}"
    }
  ]
}""".replace("{i}", str(i)).replace("{name}", name)
        
        f.seek(0)
        f.write(text)

IDs = {}

with open("roles.txt") as r:
    i = 1
    for role in r.read().split("\n"):
        role = role.lower().replace("'", "").replace(" ", "").replace("-", "")

        try:
            load(role, i)
            IDs[role] = i
            i += 1
        except UnidentifiedImageError:
            print("couldn't load "+role)    

print(IDs)
