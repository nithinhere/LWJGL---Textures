
/**
 * Shape3D.java - an abstract class representing an OpenGL graphical object
 *
 * 10/16/13 rdb derived from Shape3D.cpp
 * 09/28/15 rdb Revised for lwjgl              
 # 12/28/16 rdb Revised to better match lwjgl3.1, esp. use of MemoryUtil
 *              Revised to match interface to other Shape3D demo classes
 *              Replaced java.math with joml
 * 02/05/16 rdb Refactored to do as much here for children as possible.
 */
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.*;

import org.lwjgl.system.MemoryUtil;
import org.joml.*;

abstract public class Shape3D {
	// ------------------ class variables ------------------------------
	static final int MAX_COLORS = 20; // arbitrary number change it if need more

	// ---------------------- instance variables ------------------------------
	// ------ vertex data and variables
	private boolean updateBuffer = true;
	private FloatBuffer coordBuffer = null;
	private int nCoords = 0;
	protected int coordSize = 0; // 3 for xyz, 4 for xyzw

	private FloatBuffer normalBuffer = null;
	private int nNormals = 0;
	protected int normalSize = 0; // 3 for xyz, 4 for xyzw
	private FloatBuffer texCoordBuffer = null;
	private int nTexCoords = 0;
	protected int texCoordSize = 0; // 3 for xyz, 4 for xyzw

	// ------------------ GLSL-related instance variables
	// ----------------------------
	// these uniform variable specs need not be used by every object or
	// every shader program; but it makes sense for the class of
	// objects to share their uniform variables.
	//
	protected static int unifModel = -1; // uniform id for model matrix
	protected static int unifColor = -1; // uniform id for color value

	private int shaderPgm = -1;

	private int vaoId = -1;

	private int coordVBO = -1; // buffer for coords
	private int vPosition = -1; // vertex attrib id for position
	private int normalVBO = -1; // buffer for normals
	private int vNormal = -1; // vertex attrib id for normal
	protected int texCoordVBO = -1; // buffer for texture
	protected int vTexCoord = -1;// vertex attrib id for tex coords

	private int nTriangles = -1;
	private int nVertices = -1; // #vertices in buffer (3*nTriangles)
	private boolean updateBuffers = true;

	// ------------------ object instance variables ----------------------------
	// ----- transformation variables
	protected float xLoc, yLoc, zLoc; // location (origin) of object
	protected float xSize, ySize, zSize; /// get size of the object
	protected Matrix4f rotationMatrix = new Matrix4f();
	protected Matrix4f modelMatrix = new Matrix4f();
	protected FloatBuffer modelBuf = MemoryUtil.memAllocFloat(16);

	protected Color[] colors = new Color[MAX_COLORS];
	protected FloatBuffer[] colorBufs = new FloatBuffer[MAX_COLORS];

	// ---------- Material properties (could have a Material class)
	protected float ka = 0.3f; // 30% of color is ambient
	protected float kd = 0.7f; // 70% is diffuse.

	// --------- Texture related variables
	// This code supports only 1 texture per object
	protected Texture texture = null;
	protected FloatBuffer textureBuf = null;
	protected static float textureWeight = 1; // default is all texture
	protected boolean hasTexture = false; // passes info to shader

	protected int unifTexWt = -1; // textureWeight
	protected int unifHasTexture = -1; // hasTexture

	// ------------------ Constructors ----------------------------------
	/**
	 * Create a new object3D at position 0,0,0 of size 1,1,1
	 */
	public Shape3D() {
		shaderPgm = LWJGL.shaderProgram;
		for (int i = 0; i < colors.length; i++) // fill arrays with null
		{
			colors[i] = null;
			colorBufs[i] = null;
		}
		setColor(1, 0, 0);
		setLocation(0, 0, 0);
		setSize(1, 1, 1);

		// ------------- Setup GLSL interface variables -------------
		createUniforms(); // uniform variables needed by Shapes

		this.vaoId = glGenVertexArrays(); // create VAO

		// Create VBO and VAO location objects for vertex coord position
		this.coordVBO = glGenBuffers(); // for coord position
		vPosition = glGetAttribLocation(shaderPgm, "vPosition");

		// Create a VBO and VAO location for the normal data
		this.normalVBO = glGenBuffers();
		vNormal = glGetAttribLocation(shaderPgm, "vNormal");

		// Create VBO and VAO location for texture coord positions
		this.texCoordVBO = glGenBuffers();
		vTexCoord = glGetAttribLocation(shaderPgm, "vTexCoord");
	}

	// ------------------- finalize() ------------------------------------
	/**
	 * This method gets called whenever an its object is "garbage-collected";
	 * It's similar to C++ destructor, except with Java we don't have to
	 * explicitly destruct stuff that Java knows about.
	 * 
	 * However, Java does NOT know about the memory we allocated via the
	 * MemoryUtil class, so we should free up that memory.
	 * 
	 * In reality, it doesn't matter for this demo program because we never stop
	 * using any Shape3D objects we create until the program terminates -- then
	 * it doesn't matter. Even so, it's important to understand the implications
	 * of using MemoryUtil, so we will clean up.
	 */
	public void finalize() {
		MemoryUtil.memFree(coordBuffer);
		MemoryUtil.memFree(normalBuffer);
		MemoryUtil.memFree(modelBuf);
		for (FloatBuffer buf : colorBufs) {
			if (buf != null)
				MemoryUtil.memFree(buf);
		}
	}

	// ------------------------- createUniforms ---------------------------
	/**
	 * Shape3D objects need some uniform variables in the shader program to help
	 * control the shader execution. These include: unif_hasTexture: int: Not
	 * zero identifies that the sampler2D texture mapping should be invoked.
	 * This defaults in the shader program to 0, but is set to 1 if a texture is
	 * defined for the object. unif_hasObjColor: int: Not 0 indicates that all
	 * vertices stored in the coordinate buffer share one color; that color is
	 * stored in the sharedColor variable. This avoids the need for a color
	 * buffer. unif_objXform : matrix4f defining the object's transform to Scene
	 * coordinates. unif_texWt : float: % color to come from texture [0.0 to
	 * 1.0]
	 *
	 */
	protected void createUniforms() {
		UtilsLWJGL.glError("--->Shape3D.createUniforms"); // clear old glerrors

		unifModel = glGetUniformLocation(shaderPgm, "uModel");
		unifColor = glGetUniformLocation(shaderPgm, "uColor");

		unifTexWt = glGetUniformLocation(shaderPgm, "uTexWt");
		glUniform1f(unifTexWt, textureWeight);

		unifHasTexture = glGetUniformLocation(shaderPgm, "uHasTexture");

		float[] rgba = colors[0].get4f(); // get rgba as an array
		glUniform4fv(unifColor, rgba);

		UtilsLWJGL.glError("<---Shape3D.createUniforms"); // check for glerrors
	}

	// ------------------ public methods -------------------------------
	// ------------------------- redraw ----------------------------
	/**
	 * Update the specifications for the shape. The parent class does
	 * transformation setup and the uniform variable settings expected of all
	 * children. The child redraw() must call this first.
	 */
	protected void redraw() {
		// Simple modeling: only size and location.
		// we can write down the desired matrix.
		// This specification and the FloatBuffer we create does NOT have
		// to be done on every re-draw; it only needs to be done when
		// the location or size changes: setLocation or setSize.
		// The model matrix and modelBuf should be instance variables.
		//
		modelMatrix.identity().translate(xLoc, yLoc, zLoc).scale(xSize, ySize, zSize);

		glUniform1i(unifHasTexture, hasTexture ? 1 : 0);

		modelMatrix.get(modelBuf);

		glUniformMatrix4fv(unifModel, false, modelBuf);

		glUniform4fv(unifColor, colors[0].get4f()); // update color uniform
		glUniform1f(unifTexWt, textureWeight);

		// identify which VAO specification needs to be drawn.
		glBindVertexArray(vaoId);
		glEnableVertexAttribArray(vPosition);

		if (updateBuffer)
			loadBuffers();
		if (hasTexture) {
			texture.bind(); // if there is a texture bind it.
			// glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
			// GL_CLAMP_TO_EDGE );
			// glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
			// GL_CLAMP_TO_BORDER );
			// glGenerateMipmap(GL_TEXTURE_2D);
		}

		// last parameter is # vertexes defining the triangles
		// verts.length is the number of floats in the verts array, which
		// is 4 * the number of triangles
		glDrawArrays(GL_TRIANGLES, 0, nVertices);

		if (hasTexture)
			texture.unbind();

		// unbind the vao, we are done with it for now.
		glBindVertexArray(0);
	}

	// ---------------------- loadBuffers() --------------------------
	/**
	 * Download the data buffers related to vertex coordinates. This version has
	 * separate buffers for all attributes.
	 */
	protected void loadBuffers() {
		UtilsLWJGL.glError("--->loadBuffers"); // clean out any old errors

		// load the non-color buffers
		loadCoordBuffer();
		loadNormalsBuffer();
		loadTexCoordBuffer();

		UtilsLWJGL.glError("<--loadBuffers");
	}

	// ---------------------- loadCoordBuffer() --------------------------
	/**
	 * Build VBO for Coordinate data and download to GPU. Assume the appropriate
	 * VAO is currently bound!
	 */
	protected void loadCoordBuffer() {
		UtilsLWJGL.glError("--->loadCoordBuffer"); // clean out any old errors
		glBindBuffer(GL_ARRAY_BUFFER, this.coordVBO);
		// fill it with the position data from the posBuffer
		glBufferData(GL_ARRAY_BUFFER, coordBuffer, GL_STATIC_DRAW);

		glEnableVertexAttribArray(vPosition);
		// describe how vPosition data can be found in the current buffer
		glVertexAttribPointer(vPosition, coordSize, GL_FLOAT, false, 0, 0L);
		UtilsLWJGL.glError("<---loadCoordBuffer"); // report error since enter
	}

	// ---------------------- loadTexCoordBuffer() --------------------------
	/**
	 * If textures used for this object, build VBOs for texture coordinate data;
	 * Assume the appropriate VAO is currently bound.
	 */
	protected void loadTexCoordBuffer() {
		if (!hasTexture)
			return;
		UtilsLWJGL.glError("--->loadTexCoordBuffer"); // clean out any old
														// errors
		// Bind the buffer
		glBindBuffer(GL_ARRAY_BUFFER, this.texCoordVBO);
		// Send the texCoordBuffer data to the GPU
		glBufferData(GL_ARRAY_BUFFER, texCoordBuffer, GL_STATIC_DRAW);

		glEnableVertexAttribArray(vTexCoord);

		// describe how vPosition data can be found in the current buffer
		glVertexAttribPointer(vTexCoord, texCoordSize, GL_FLOAT, false, 0, 0L);
		UtilsLWJGL.glError("<---loadTexCoordBuffer"); // report error since
														// entering
	}

	// ---------------------- loadNormalsBuffer() --------------------------
	/**
	 * If normals used for this object, build VBOs for normal data; Assume the
	 * appropriate VAO is currently bound.
	 */
	protected void loadNormalsBuffer() {
		if (nNormals == 0)
			return;
		UtilsLWJGL.glError("--->loadNormalsBuffer"); // clean out any old errors

		// make it the current buffer
		glBindBuffer(GL_ARRAY_BUFFER, this.normalVBO);
		// fill it with the position data from the posBuffer
		glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
		UtilsLWJGL.glError("glBufferData");

		glEnableVertexAttribArray(vNormal);

		// describe how vNormal data can be found in the current buffer
		glVertexAttribPointer(vNormal, normalSize, GL_FLOAT, false, 0, 0L);
		UtilsLWJGL.glError("<---loadNormalsBuffer"); // report error since enter
	}

	// ++++++++++++++++++++++ public methods ++++++++++++++++++++++++++++++=
	// ---------------------- setCoordData ----------------------------------
	/**
	 * Specify the vertex coordinate information for this object. Create and
	 * save a FloatBuffer with the information. A null first parameter discards
	 * previous vertex coord data
	 *
	 * @param coords
	 *            float[] array of coord positions to associate with each
	 *            vertex. If null is specified, deletes previous coords
	 * @param floatsPerVertex
	 *            int # coord components (2 or 3)
	 */
	protected void setCoordData(float[] coords, int floatsPerVertex) {
		if (floatsPerVertex < 2 || floatsPerVertex > 4) // only 2-4 are valid
		{
			throw new RuntimeException(
					"setCoordData: size invalid: " + floatsPerVertex + ". Only 2, 3 or 4 are valid values");
		}
		// Could make buffer static and only allocate first time.
		if (coords == null) // Unused feature for (temporarily) emptying a shape
		{
			MemoryUtil.memFree(coordBuffer);
			coordBuffer = null;
			coordSize = 0;
			nCoords = 0;
			nVertices = 0;
			nTriangles = 0;
		} else {
			coordBuffer = MemoryUtil.memRealloc(coordBuffer, coords.length);
			coordSize = floatsPerVertex;
			nVertices = coords.length / coordSize;
			nTriangles = nVertices / 3;
			nCoords = coords.length / floatsPerVertex;
			coordBuffer.put(coords).flip();
		}
	}

	// ---------------------- setNormalData ----------------------------------
	/**
	 * Specify the vertex normal coord information for this object. Create and
	 * save a FloatBuffer with the information. A null first parameter discards
	 * previous normal data
	 *
	 * @param normals
	 *            float[] array of coord positions to associate with each
	 *            vertex. If null is specified, deletes previous coords
	 */
	protected void setNormalData(float[] normals, int floatsPerVertex) {
		// Could make buffer static and only allocate first time.
		if (normals == null) {
			MemoryUtil.memFree(normalBuffer);
			normalBuffer = null;
			normalSize = 0;
			nNormals = 0;
		} else {
			normalBuffer = MemoryUtil.memRealloc(normalBuffer, normals.length);
			normalSize = floatsPerVertex;
			nNormals = normals.length / floatsPerVertex;
			normalBuffer.put(normals).flip();
		}
	}

	// ---------------------- setTextureCoordData ---------------------------
	/**
	 * Specify the vertex texture coord information for this object. Create and
	 * save a FloatBuffer with the information. A null first parameter discards
	 * previous texture data
	 *
	 * @param texCoords
	 *            float[] array of texture coord positions to associate with
	 *            each vertex. If null is specified, deletes previous coords
	 * @param floatsPerVertex
	 *            int # texture coord components (2 or 3)
	 */
	protected void setTextureCoordData(float[] texCoords, int floatsPerVertex) {
		if (floatsPerVertex < 1 || floatsPerVertex > 3) // only 1-3 are valid
		{
			throw new RuntimeException(
					"setCoordData: size invalid: " + floatsPerVertex + ". Only 1, 2, or 3 are valid values");
		}
		// Allow user to say: not going to use textures any more for this
		// object.
		if (texCoords == null) {
			MemoryUtil.memFree(texCoordBuffer);
			texCoordBuffer = null;
			texCoordSize = 0;
			nTexCoords = 0;
		} else {
			texCoordBuffer = MemoryUtil.memRealloc(texCoordBuffer, texCoords.length);
			texCoordSize = floatsPerVertex;
			nTexCoords = texCoords.length / floatsPerVertex;
			texCoordBuffer.put(texCoords).flip();
		}
	}

	// ----------------------- get/setLocation --------------------------------
	/**
	 * set location to the x,y,z position defined by the args
	 * 
	 * @param x
	 *            float x coordinate
	 * @param y
	 *            float y coord
	 * @param z
	 *            float z coord
	 */
	public void setLocation(float x, float y, float z) {
		xLoc = x;
		yLoc = y;
		zLoc = z;
	}

	/**
	 * return the value of the x origin of the shape
	 * 
	 * @return float
	 */
	public float getX() {
		return xLoc;
	}

	/**
	 * return the value of the y origin of the shape
	 * 
	 * @return float
	 */
	public float getY() {
		return yLoc;
	}

	/**
	 * return the value of the z origin of the shape
	 * 
	 * @return float
	 */
	public float getZ() {
		return zLoc;
	}

	/**
	 * return the location as a Point3f object
	 * 
	 * @return Vector3f location as a vector3
	 */
	public Vector3f getLocation() // return location as a Point
	{
		return new Vector3f(xLoc, yLoc, zLoc);
	}

	// -------------------- set/get Texture methods ---------------------------
	/**
	 * Set the texture to the parameter Texture. Can pass null to turn off a
	 * previously specified texture.
	 * 
	 * @return t Texture
	 */
	public void setTexture(Texture t) {
		texture = t;
		hasTexture = t != null;
	}

	/**
	 * Set the blend parameter for this object. Parameter is the % (as a
	 * fraction of 1) that the texture should contribute to the final fragment
	 * color -- the object's color will contribute 1-texWt.
	 * 
	 * @param texWt
	 *            float % of color to be taken from texture (0-1)
	 */
	public void setTextureWeight(float texWt) {
		textureWeight = texWt;
	}

	public float getTextureWeight() {
		return textureWeight;

	}

	/**
	 * Return the 0-th texture.
	 * 
	 * @return Texture
	 */
	public Texture getTexture() {
		return texture;
	}

	// ----------------------- get/setColor methods ---------------------------
	/**
	 * return the base color of the object
	 * 
	 * @return Color
	 */
	public Color getColor() // return color 0
	{
		return colors[0];
	}

	/**
	 * return any color of the object
	 * 
	 * @param i
	 *            index of color to retrieve
	 * @return Color
	 */
	public Color getColor(int i) // return color i
	{
		if (i >= 0 && i < MAX_COLORS)
			return colors[i];
		else
			return null; // should throw exception
	}

	/**
	 * set the "nominal" color of the object to the specified color; this does
	 * not require that ALL components of the object must be the same color.
	 * Typically, the largest component will take on this color, but the
	 * decision is made by the child class.
	 * 
	 * @param c
	 *            Color the color
	 */
	public void setColor(Color c) {
		setColor(0, c);
	}

	/**
	 * set the nominal color (index 0) to the specified color with floats
	 * 
	 * @param r
	 *            float red component
	 * @param g
	 *            float green component
	 * @param b
	 *            float blue component
	 */
	public void setColor(float r, float g, float b) {
		setColor(0, r, g, b, 1);
	}

	/**
	 * set the nominal color (index 0) to the specified color with floats
	 * 
	 * @param r
	 *            float red component
	 * @param g
	 *            float green component
	 * @param b
	 *            float blue component
	 * @param a
	 *            float alpha component
	 */
	public void setColor(float r, float g, float b, float a) {
		setColor(0, r, g, b, a);
	}

	/**
	 * set the index color entry to the specified color with floats
	 * 
	 * @param i
	 *            int which color index
	 * @param r
	 *            float red component
	 * @param g
	 *            float green component
	 * @param b
	 *            float blue component
	 */
	public boolean setColor(int i, float r, float g, float b) {
		return setColor(i, r, g, b, 1);
	}

	/**
	 * set the i-th color entry to the specified color with Color
	 * 
	 * @param i
	 *            int which color index
	 * @param r
	 *            float red component
	 * @param g
	 *            float green component
	 * @param b
	 *            float blue component
	 * @param a
	 *            float alpha component
	 */
	public boolean setColor(int i, float r, float g, float b, float a) {
		if (i < 0 || i > MAX_COLORS) // should throw an exception!
		{
			System.err.println("*** ERROR *** Shape3D.setColor: bad index: " + i + "\n");
			return false;
		}
		if (colors[i] == null)
			colors[i] = new Color(r, g, b, a); // put color at index
		else
			colors[i].setColor(r, g, b, a);

		// make buffer!
		colorBufs[i] = MemoryUtil.memAllocFloat(4);
		colorBufs[i].put(r).put(g).put(b).put(a).flip();
		return true;
	}

	/**
	 * set the i-th color entry to the specified color with Color
	 * 
	 * @param i
	 *            int which color index
	 * @param c
	 *            Color the color
	 */
	public boolean setColor(int i, Color c) {
		return setColor(i, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}

	// ------------------ setSize ----------------------------------------
	/**
	 * set the size of the shape to be scaled by xs, ys, zs That is, the shape
	 * has an internal fixed size, the shape parameters scale that internal
	 * size.
	 * 
	 * @param xs
	 *            float x Scale factor
	 * @param ys
	 *            float y Scale factor
	 * @param zs
	 *            float z Scale factor
	 * 
	 */
	public void setSize(float xs, float ys, float zs) {
		xSize = xs;
		ySize = ys;
		zSize = zs;
	}

	/**
	 * set the rotation parameters: angle, and axis specification
	 * 
	 * @param a
	 *            float angle of rotation
	 * @param dx
	 *            float x axis direction
	 * @param dy
	 *            float y axis direction
	 * @param dz
	 *            float z axis direction
	 */
	public void setRotate(float a, float dx, float dy, float dz) {
		Vector3f axis = new Vector3f(dx, dy, dz).normalize();
		rotationMatrix.identity().rotate(a, axis);
	}
}