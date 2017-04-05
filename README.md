# LWJGL---Textures
     
Steps to reproduce:
Edit the make file to the folder where you placed the code.

Scene 1 – Quad Scene
•	Quad Scene displays 4 quad objects with the basic textures.
•	The texture filter applied to the basic quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.


Scene 2 – Box Scene
•	Box Scene displays 4 box objects with the basic textures.
•	The texture filter applied to the basic quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.

Scene 3 – Cylinder Scene
•	Cylinder is presented in the third scene by passing 8 faces from the constructor.
•	The texture filter applied to the basic quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.
•	Texture is wrapped by importing new image to the class.
•	Cylinder top/bottom faces got texture

Scene 4 – quad2Scene
o	Quad Scene displays 4 quad objects which shows Mipmapped image and non Mipmapped image.
o	The texture filter applied to the left quad Scene are GL_TEXTURE_MIN_FILTER, GL_NEAREST, GL_TEXTURE_MAG_FILTER, GL_LINEAR.
o	The texture filter applied to the right quad Scene are GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR, GL_TEXTURE_MAG_FILTER, GL_LINEAR
o	Right image is non mipmapped image and the left image is mipmapped image.

Scene 5 – quad3Scene
•	Quad Scene displays 4 quad objects which shows texture filter parameters difference with min and mag filters.
•	The texture filter applied to the left quad Scene are GL_TEXTURE_MIN_FILTER, GL_LINEAR, GL_TEXTURE_MAG_FILTER, _LINEAR
•	.
•	The texture filter applied to the right quad Scene are GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR, GL_TEXTURE_MAG_FILTER, GL_LINEAR


Scene 6 – quad4Scene
•	Quad Scene displays 4 quad objects which shows texture filter parameters difference with min and mag filters.

•	The texture filter applied to the left quad Scene are GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR, GL_TEXTURE_MAG_FILTER, GL_LINEAR
•	.
•	The texture filter applied to the right quad Scene are GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST, GL_TEXTURE_MAG_FILTER, GL_LINEAR

Scene 7 – quad5Scene
•	Quad Scene displays 4 quad objects which shows texture filter parameters difference with min and mag filters.

•	The texture filter applied to the left quad Scene are GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST, GL_TEXTURE_MAG_FILTER, GL_LINEAR.
•	The texture filter applied to the right quad Scene are GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR,]GL_TEXTURE_MAG_FILTER, GL_NEAREST

Scene 8 – quad6Scene
•	Quad Scene displays 4 quad objects which shows texture filter parameters difference with wrap filters and min,mag filters.

•	The texture filter applied to the left quad Scene are L_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR, _TEXTURE_MAG_FILTER, GL_NEAREST
•	The texture filter applied to the right quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE


Scene9 – quad7Scene
•	Quad Scene displays 4 quad objects which shows texture filter parameters difference with wrap filters

•	The texture filter applied to the left quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER)
•	The texture filter applied to the right quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER

Scene 10 – quad8Scene
•	Quad Scene displays 4 quad objects which shows texture filter parameters difference with wrap filters

•	The texture filter applied to the left quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE
•	The texture filter applied to the right quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_REPEAT

Scene 11 – quad9Scene

•	Quad Scene displays 4 quad objects which shows texture filter parameters difference with wrap filters

•	The texture filter applied to the left quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER
•	The texture filter applied to the right quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_REPEAT

Scene 12 – box10scene
•	This scene shows difference between mipmapped box image and non mip mapped box image
•	The texture filter applied to the left quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER

•	The texture filter applied to the right quad Scene are GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR, GL_TEXTURE_MAG_FILTER, GL_LINEAR


Scene 13 – cylinderscene1
•	This cylinder Cylinder object displays default texture
•	The texture filter applied to the basic quad Scene are GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER.
•	Texture is wrapped by importing new image to the class.
•	Cylinder top/bottom faces got texture
