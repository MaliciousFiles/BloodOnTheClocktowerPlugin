from PIL import Image, UnidentifiedImageError
import requests
import colorsys
import math

M=0.4
img = Image.open(requests.get("https://images.vexels.com/media/users/3/151846/raw/82bd668f87a708c87adf9b2c36f221fb-black-spiral-vector.jpg", stream=True).raw).convert("RGBA").crop((250, 0, 1250, 1000))
for r in range(1000):
    for c in range(1000):
        coords = (c-500,r-500)
        if False:#sum(img.getpixel((c,r))[:3])/3 < 40:
            perc = min(0.25**(math.sqrt(coords[0]*coords[0] + coords[1]*coords[1])/335)+0.1,1)#math.log(max(1 - math.sqrt(coords[0]*coords[0] + coords[1]*coords[1])/700, 0.00000001), 10) + 1
            mod = perc * 0.9
            img.putpixel((c,r), (max(int(255/mod*M), 0), max(int(56/mod*M), 0), max(int(245/mod*M), 0), 255))
        else: img.putpixel((c,r), (255, 255, 255, 0))

W=1
for t in range(0, M := 35*1000):
    perc = math.log(t/M+0.35, 10)+0.87
    t = t/1000

    cos = math.cos(t)
    sin = math.sin(t)
    coords = (t/2*cos, t/2*sin)

    for w in range(-16, 16):
        img.putpixel((int(24*coords[0]+w*cos)+518, int(24*coords[1]+w*sin)+500), (int(255*perc), int(56*perc), int(245*perc), 255))
    #for m in range(int(23.5*20), int(25.5*20)):
    #    m = m/20
    #    img.putpixel((int(m*coords[0])+518, int(m*coords[1])+500), (int(255*perc), int(56*perc), int(245*perc), 255))

img.save("test.png")
