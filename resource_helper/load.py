from PIL import Image, UnidentifiedImageError
import requests

def load(name, i):
#     html = requests.get(f"https://wiki.bloodontheclocktower.com/File:Icon_{name}.png").text
#     url = "https://wiki.bloodontheclocktower.com/images"+html.split('href="/images')[1].split('"')[0]
#
#     img = Image.open(requests.get(url, stream=True).raw).convert("RGBA").crop((95.5, 95.5, 495.5, 495.5))
#     data = img.getdata()
#     newdata = []
#     for px in data:
#         if px[3] < 255: newdata.append((255,255,255,0))
#         else: newdata.append(px)
#     img.putdata(newdata)
#     img.save(f"../BOTC Resource Pack/assets/botc/textures/item/{name}.png")
    
    with open(f"../BOTC Resource Pack/assets/botc/models/item/{name}.json", "w") as f:
        f.write("""{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "botc:item/{name}"
  }
}""".replace("{name}", name))

    with open(f"../BOTC Resource Pack/assets/botc/models/item/{name}_mini.json", "w") as f:
            f.write("""{
      "parent": "minecraft:item/generated",
      "textures": {
        "layer0": "botc:item/{name}"
      },
      "display": {
        "thirdperson_righthand": {
          "translation": [ -2.5, 2.5, 0 ],
          "scale": [ 0.6, 0.6, 0.6 ]
        },
        "thirdperson_lefthand": {
         "translation": [ -2.5, 2.5, 0 ],
         "scale": [ 0.6, 0.6, 0.6 ]
        },
        "firstperson_righthand": {
         "translation": [ -2.5, 2.5, 0 ],
         "scale": [ 0.6, 0.6, 0.6 ]
        },
        "firstperson_lefthand": {
          "translation": [ -2.5, 2.5, 0 ],
          "scale": [ 0.6, 0.6, 0.6 ]
        },
        "gui": {
          "translation": [ -2.5, 2.5, 0 ],
          "scale": [ 0.6, 0.6, 0.6 ]
        },
        "head": {
          "translation": [ -2.5, 2.5, 0 ],
          "scale": [ 0.6, 0.6, 0.6 ]
        },
        "ground": {
          "translation": [ -2.5, 2.5, 0 ],
          "scale": [ 0.6, 0.6, 0.6 ]
        },
        "fixed": {
          "translation": [ -2.5, 2.5, 0 ],
          "scale": [ 0.6, 0.6, 0.6 ]
        }
      }
    }""".replace("{name}", name))

#     with open("../BOTC Resource Pack/assets/botc/items/role.json", "r+") as f:
#         text = f.read()[:-6] + ("," if i != 1 else "") + """
#     {
#         "predicate": {
#             "custom_model_data": {i}
#         },
#         "model": "botc:item/{name}"
#     }
#   ]
# }""".replace("{i}", str(i)).replace("{name}", name)
        
#         f.seek(0)
#         f.write(text)

IDs = {}

with open("roles.txt") as r:
    i = 1
    for role in r.read().split("\n"):
        if role == "": continue
        
        role = role.lower().replace("'", "").replace(" ", "").replace("-", "")

        try:
            load(role, i)
            IDs[role] = i
            i += 1
        except UnidentifiedImageError:
            print("couldn't load "+role)    

print(IDs)
