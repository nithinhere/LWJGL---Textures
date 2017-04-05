/*
 * Derived from 
 * https://github.com/SilverTiger/lwjgl3-tutorial/wiki/Textures
 * rdb downloaded and edited 11/09/15
 * jan 2017 additional changes to separate the pro*view*scene from 
 *          object transformation (model also)
 *          and to support the various vertexColorModels defined in Shape3D
 */
#version 330

in vec3 vPosition;
in vec3 vNormal;
in vec2 vTexCoord;

out vec4 vertexColor;
out vec2 textureCoord;

uniform bool uHasTexture = false;  // 1 implies has texture
uniform vec4 uColor;
uniform mat4 uModel;
uniform mat4 uPVS;    // this is projection * viewing * scene

//---------- local variables --------------
//   In a complete system these would be uniform variables associated
//   with this object.
float ka = 0.35f;
float kd = 0.9f;

vec3 lightedColor( vec3 objColor, vec3 vertexNorm )
{
	vec3 lightDir = normalize( vec3( 2, 3, 4 )); // In obj coord space to light 3 faces 
	vec3 lightColor = vec3( 1, 1, 1 );
    vec3 vNorm = vec3( normalize( vertexNorm ));    
    vec3 color = ka * objColor + kd * objColor * lightColor * dot( lightDir, vNorm );
    return color;
}

void main() 
{
	vec3 color3 = vec3( uColor.r, uColor.g, uColor.b );
	vec4 vPos4 = vec4( vPosition, 1 );
	vertexColor = vec4( lightedColor( color3, vNormal ), 1 );

	if ( uHasTexture )
	{
    	textureCoord = vTexCoord;
    }
    mat4 mvp = uPVS * uModel;
    gl_Position = mvp * vPos4;
}
