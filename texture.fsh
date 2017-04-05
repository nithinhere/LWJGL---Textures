/*
 * Initially derived from 
 * https://github.com/SilverTiger/lwjgl3-tutorial/wiki/Textures
 * downloaded and edited by rdb on 11/09/15.
 *
 * Jan 2017: significant edits done by rdb.
 */

#version 330

in vec4 vertexColor;     // vertex color with alpha
in vec2 textureCoord;

out vec4 fragColor;

uniform sampler2D texImage;   // texture to use if hasTexture != 0
uniform float uTexWt; // % output color from textureColor vs vertexColor
uniform bool uHasTexture;     // false => no texture

void main() 
{
    if ( ! uHasTexture )
        fragColor = vertexColor;
    else
    {
        vec4 textureColor = texture( texImage, textureCoord );
        fragColor = ( 1 - uTexWt) * vertexColor 
                         + uTexWt * textureColor;
    }
}
