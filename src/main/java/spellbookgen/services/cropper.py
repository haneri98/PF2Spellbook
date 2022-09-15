import sys

from PIL import Image
import numpy as np

image_name = sys.argv[1]
image_loc = "C:\\Users\\Hans\\IdeaProjects\\SpellbookGenerator\\src\\images\\" + image_name
image_save = "C:\\Users\\Hans\\IdeaProjects\\SpellbookGenerator\\src\\imagesCropped\\" + image_name
im = Image.open(image_loc)
im.load()
image_data = np.asarray(im)



def find_pixel_location(im, pixel_RGB, axis, reverse):
    image_data = np.asarray(im)
    pixel_location = -999
    for x in range(image_data.shape[1]):
        num1 = x
        if reverse:
            num1 = image_data.shape[1] - x - 3
        for y in range(image_data.shape[0]):
            num2 = y
            if reverse:
                num2 = image_data.shape[0] - y - 3
            if im.getpixel((num2, num1))[0] == pixel_RGB[0] and im.getpixel((num2, num1))[1] == pixel_RGB[1] and im.getpixel((num2, num1))[2] == pixel_RGB[2]:
                if axis == 1:
                    pixel_location = x
                if axis == 0:
                    pixel_location = y
                break
        if pixel_location != -999:
            break
    return pixel_location


height_from_bottom = find_pixel_location(im, (255, 255, 255), 1, True) - 6
height_from_top = find_pixel_location(im, (103, 172, 196), 1, False) - 13
length_from_left = find_pixel_location(im, (98, 125, 98), 0, True)
length_from_right = find_pixel_location(im, (15, 15, 15), 0, True) + 4
im_new = im.crop((image_data.shape[0] - length_from_left, height_from_top, image_data.shape[0] - length_from_right, image_data.shape[1] - height_from_bottom))
im_new.save(image_save)


