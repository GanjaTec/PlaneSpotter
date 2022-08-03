from PIL import Image

# filename will be set through user-pre input in java
# (with @param 'params' in method PyAdapter.runScript())
filename = ''
# img will be set through Image.open(..)
img = None
try:
    img = Image.open(filename)
    img = img.rotate(180).transpose(Image.FLIP_LEFT_RIGHT)
except IOError:
    print('Python-IOError occurred!')
