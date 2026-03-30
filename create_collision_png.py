from PIL import Image

img = Image.new('RGBA', (16, 16), (80, 60, 40, 255))
img.save('assets/collision_tile.png')
print("Created collision_tile.png")
