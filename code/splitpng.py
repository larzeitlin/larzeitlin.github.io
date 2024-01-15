from PIL import Image
import os

def split_image(file_path, tile_size=(16, 16)):
    if not os.path.isfile(file_path):
        print("File not found:", file_path)
        return

    img = Image.open(file_path)
    img_width, img_height = img.size

    x_tiles = img_width // tile_size[0]
    y_tiles = img_height // tile_size[1]

    output_dir = "../assets/tiles/"
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    for x in range(x_tiles):
        for y in range(y_tiles):
            left = x * tile_size[0]
            upper = y * tile_size[1]
            right = left + tile_size[0]
            lower = upper + tile_size[1]
            bbox = (left, upper, right, lower)
            tile = img.crop(bbox)
            tile.save(os.path.join(output_dir, f"tile_{x}_{y}.png"))

split_image("../assets/tiles.png")
