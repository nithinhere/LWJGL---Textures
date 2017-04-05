
/**
 * SceneManager - encapsulates the creation and management of the Scenes.
 *      This class could use the Singleton pattern.
 *          
 *      This version's primary goal is to demonstrate texture mapping.
 *      Many features in other demo SceneManager classes are not present here.
 *
 * The checkerboard texture came from:
 *  https://docs.gimp.org/en/images/filters/examples/render-checkerboard1.jpg
 *           
 * @author rdb
 * Created 11/08/15 from LightDemo.SceneManager.
 *
 * Jan 2017: modifications to deal with changed code in Shape3D classes
 *           added auto cycling through textures and texture weights
 *           transformation changes: separate object transformation into
 *              a separate matrix that is loaded into shader for each object
 *              by the Shape3D class. This doesn't matter much for this demo
 *              that has just 1 object per scene, but should be more
 *              efficient for multi-object scenes especially if the view
 *              specification remains mostly constant.
 */
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.glfw.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;

public class SceneManager {
	// ---------------------- class variables --------------------------
	// ---------------------- instance variables --------------------------
	private final float deltaRotate = 2.0f; //

	// --------- Texture image files to use
	static String[] imageFiles = { "Sunrise.jpg", "checkerboard.jpg", "cut_teapot.jpg", "taj_mahal.jpg"
			// , "mapImage.jpg"
	};

	// objects of the Scene class inorder to show different variations
	private Scene quadScene = null;
	private Scene boxScene = null;
	private Scene cylinderScene = null;
	private Scene cylinderScene1 = null;

	private Scene curScene = null;
	private Scene quad2Scene = null;
	private Scene quad3Scene = null;
	private Scene quad4Scene = null;
	private Scene quad5Scene = null;
	private Scene quad6Scene = null;
	private Scene quad7Scene = null;
	private Scene quad8Scene = null;
	private Scene quad9Scene = null;
	private Scene box10Scene = null;
	private Scene box11Scene = null;
	private Scene box12Scene = null;

	// --------- viewing parameters
	private Vector3f eye = new Vector3f(0, 0, 1);
	private Vector3f at = new Vector3f(0, 0, 0);
	private Vector3f up = new Vector3f(0, 1, 0);

	// -------- perspective parameters
	private float fovyDegrees = 80;
	private float aspect = 1;
	private float near = 0.1f;
	private float far = 20;

	// ----------- Color Factor Handling-------
	private float c = 0.0f;
	private float C = 1.0f;

	// -------- ortho parameters
	private float left = -1;
	private float right = 1;
	private float bottom = -1;
	private float top = 1;

	private boolean usePerspective = true;

	// --------- buffers/textures
	private FloatBuffer pvsBuf; // buffer for Proj*View*Scene matrix

	private Texture textures[] = null;
	private Texture mytextures[] = null;
	// Zoom in factors
	private float zoomInUnit = 0.01f; // Zoom unit factor starts with 2 till 6
	private float zoomOutUnit = 2.02f; // Zoom out Unit starts with -2
	private float da = 0.05f; // fov da
	private float smaller_fovy_angle = (fovyDegrees / 10) - da; // Fovy
																// calculation
	private float larger_fovy_angle = (fovyDegrees / 10) + da; // Fovy
																// calculation
	private float panUnits = 0.2f; // Pan Left Units
	
	int index = 0; // Screen index

	// ------------------ constructor ------------------------
	/**
	 * Build scenes and handle interaction.
	 */
	public SceneManager() {
		UtilsLWJGL.glError("--->SceneManager"); // clean out any old glerrors

		buildTextures();
		// buildScenes();
		buildMyTextures(); // Same work as buildScenes - mainly to show texture
							// parameter specification
		clampScenes();
		// perspectiveScenes();
		curScene = quadScene;

		glfwSetWindowTitle(LWJGL.windowId, curScene.getTitle());
		UtilsLWJGL.glError("<---SceneManager"); // any errors in ctor?
	}

	// ------------------ finalize() ---------------------------
	/**
	 * Deallocate memory allocated by the lwjgl.system.MemoryUtil package.
	 * Strictly speaking, this particular class doesn't really need to do that
	 * since there is only one instance of the class created and it is only
	 * destroyed when the application ends. However, it's cood practice to do
	 * it!
	 */
	public void finalize() {
		MemoryUtil.memFree(pvsBuf);
	}

	// ------------------ clampScenes --------------------------
	/**
	 * Build 14 scenes; with 10 Quads,two with Boxes, two with Cylinder. 10
	 * quads show texture paramter variations compared with multiple objects in
	 * the same screen one quad show mipmap differentation
	 */
	private void clampScenes() {
		// Vectors for multiple locations
		Vector3f[] locs = { new Vector3f(0, 0, 0), new Vector3f(-0.4f, -0.4f, -0.1f), new Vector3f(0.4f, 0.4f, -0.15f),
				new Vector3f(-0.4f, 0.4f, -0.2f), new Vector3f(0.4f, -0.4f, -0.2f) };
		Vector3f[] sizes = { new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.2f, 0.2f, 0.2f),
				new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.2f, 0.2f, 0.2f) };

		Vector3f[] locations = { new Vector3f(0, 0, 0), new Vector3f(0.0f, 0.5f, -0.2f),

		};

		Vector3f[] locations1 = { new Vector3f(0.7f, 0f, -0.2f), new Vector3f(0.7f, 0.5f, -0.2f),

		};

		Vector3f[] locations2 = { new Vector3f(0, 0.7f, -0.2f), new Vector3f(0.9f, 0.5f, -0.2f),

		};

		// sizes
		Vector3f[] sizes1 = { new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.3f, 0.3f, 0.3f),

		};

		Vector3f[] sizes2 = { new Vector3f(0.6f, 0.6f, 0.6f), new Vector3f(0.4f, 0.4f, 0.4f),
				// new Vector3f( 0.2f, 0.2f, 0.2f ),
				// new Vector3f( 0.2f, 0.2f, 0.2f ),
				// new Vector3f( 0.2f, 0.2f, 0.2f )
		};

		// cylindr specific location and sizes
		Vector3f[] cylinderLocation = { new Vector3f(0, 0, 0), new Vector3f(-0.4f, -0.4f, -0.1f) };

		Vector3f[] cylinderSize = { new Vector3f(0.7f, 0.7f, 0.7f), new Vector3f(0.5f, 0.5f, 0.5f)

		};

		// textures for first three scenes
		Texture[] txture = { textures[1], textures[1], textures[1], textures[1], textures[0], textures[3] };

		float[] texWts = { 1f, 1f, 0.75f, 0.5f, 1f };

		float[] texWts1 = { 1f, 1f, 0.75f, 0.5f, 1f, 1f, 1f, 0.75f, 0.5f, 1f, 1f, 1f, 0.75f, 0.5f, 1f };

		// ----------- quad scene ------------------
		quadScene = new Scene("Quad scene; with/without textures");
		for (int q = 0; q < locs.length; q++)
			quadScene.addShape(makeQuad2(locs[q], sizes[q], txture[q], texWts[q]));
		quadScene.setLookAt(eye, at, up);
		quadScene.setOrtho(left, right, bottom, top, near, far);
		quadScene.setPerspective(fovyDegrees, aspect, near, far);

		// --------------Mipmapped and Non mipmapped image
		quad2Scene = new Scene("Scene 1 ,Left - Mipmapped image & Right - Non Mipmapped image");
		for (int q = 0; q < 2; q++) {
			quad2Scene.addShape(makeQuad2(locs[q], sizes[q], mytextures[0], texWts[q]));
			quad2Scene.addShape(makeQuad2(locations1[q], sizes1[q], mytextures[3], texWts1[q]));
		}

		quad2Scene.setLookAt(eye, at, up);
		quad2Scene.setOrtho(left, right, bottom, top, near, far);
		quad2Scene.setPerspective(fovyDegrees, aspect, near, far);

		// --------------quad3Scene ---------------------------
		// Right - Min_nearest_mipmap_Linear + MagLinear & Left - Min Linear +
		// Mag Linear -----
		quad3Scene = new Scene("Right - Min_near_mipmap_Linear + MagLinear & Left - Min Linear + Mag Linear ");
		for (int q = 0; q < locations.length; q++) {
			quad3Scene.addShape(makeQuad2(locs[q], sizes[q], mytextures[1], texWts[q]));
			quad3Scene.addShape(makeQuad2(locations1[q], sizes1[q], mytextures[2], texWts[q]));
		}
		quad3Scene.setLookAt(eye, at, up);
		quad3Scene.setOrtho(left, right, bottom, top, near, far);
		quad3Scene.setPerspective(fovyDegrees, aspect, near, far);

		// Right -Min_nearest_mipamap_nearest + magLinear & Left -
		// MinLinear_Mipmap_linear+MagLinear
		quad4Scene = new Scene("Right -Min_near_mipamap_near + magLinear & Left - MinLinear_Mipmap_linear+MagLinear ");
		for (int q = 0; q < locations.length; q++) {
			quad4Scene.addShape(makeQuad2(locs[q], sizes[q], mytextures[3], texWts[q]));
			quad4Scene.addShape(makeQuad2(locations1[q], sizes1[q], mytextures[4], texWts[q]));
		}
		quad4Scene.setLookAt(eye, at, up);
		quad4Scene.setOrtho(left, right, bottom, top, near, far);
		quad4Scene.setPerspective(fovyDegrees, aspect, near, far);

		// Right -Min_Linear_mipmapLinear + Mag Linear & Left -
		// MinLinear_Mipmap_nearest+MagLinear
		quad5Scene = new Scene("Right -Min_Linear_mipmapLinear + Mag Near & Left - MinLinear_Mipmap_near+MagLinear ");
		for (int q = 0; q < locations.length; q++) {
			quad5Scene.addShape(makeQuad2(locs[q], sizes[q], mytextures[5], texWts[q]));
			quad5Scene.addShape(makeQuad2(locations1[q], sizes1[q], mytextures[6], texWts[q]));
		}
		quad5Scene.setLookAt(eye, at, up);
		quad5Scene.setOrtho(left, right, bottom, top, near, far);
		quad5Scene.setPerspective(fovyDegrees, aspect, near, far);

		// Right -glWrapSclamtoedge+glWrapTclamptoEdge + Mag Linear & Left-
		// Min_Linear_mipmapLinear + Mag Linear
		quad6Scene = new Scene("Wrap -clamtoedge+clamptoEdge + Mag Linear & Min_Linear_mipmapLinear + Mag Linear ");
		for (int q = 0; q < locations.length; q++) {
			quad6Scene.addShape(makeQuad2(locs[q], sizes[q], mytextures[6], texWts[q]));
			quad6Scene.addShape(makeQuad2(locations1[q], sizes2[q], mytextures[7], texWts[q]));
		}
		quad6Scene.setLookAt(eye, at, up);
		quad6Scene.setOrtho(left, right, bottom, top, near, far);
		quad6Scene.setPerspective(fovyDegrees, aspect, near, far);

		// Right -glWrapSclamtoborder+glWrapTclamptoborder, Left -
		// glWrapSclamtoedge+glWrapTclamptoborder
		quad7Scene = new Scene("Wrap -clamtoborder+clamptoborder, Left - clamtoedge+clamptoborder");
		for (int q = 0; q < locations.length; q++) {
			quad7Scene.addShape(makeQuad2(locs[q], sizes[q], mytextures[8], texWts[q]));
			quad7Scene.addShape(makeQuad2(locations2[q], sizes2[q], mytextures[9], texWts[q]));
		}
		quad7Scene.setLookAt(eye, at, up);
		quad7Scene.setOrtho(left, right, bottom, top, near, far);
		// boxScene.setPerspective( fovyDegrees, aspect, near, far );
		quad7Scene.setPerspective(fovyDegrees, aspect, near, far);

		// ------------- --Right -glWrapSclamtoborder+glRepeat ---------------
		quad8Scene = new Scene("Wrap - clamtoborder+glRepeat, clamtoborder+clamtoedge");
		for (int q = 0; q < locations.length; q++) {
			quad8Scene.addShape(makeQuad2(locs[q], sizes[q], mytextures[10], texWts[q]));
			quad8Scene.addShape(makeQuad2(locations2[q], sizes2[q], mytextures[11], texWts[q]));
		}
		quad8Scene.setLookAt(eye, at, up);
		quad8Scene.setOrtho(left, right, bottom, top, near, far);
		// boxScene.setPerspective( fovyDegrees, aspect, near, far );
		quad8Scene.setPerspective(fovyDegrees, aspect, near, far);

		// ------------- --Right -glWrapSclamtoborder+glRepeat ---------------
		// ----------------Left - glWrapSclamtoborder+glWrapTclamptoedge
		// -----------------
		// ----------- quad scene ------------------
		quad9Scene = new Scene("clamtoedge+clamtoborder,   clamtoedge+repeat ");
		for (int q = 0; q < locations.length; q++) {
			quad9Scene.addShape(makeQuad2(locs[q], sizes[q], mytextures[12], texWts[q]));
			quad9Scene.addShape(makeQuad2(locations2[q], sizes2[q], mytextures[11], texWts[q]));
		}
		quad9Scene.setLookAt(eye, at, up);
		quad9Scene.setOrtho(left, right, bottom, top, near, far);
		// boxScene.setPerspective( fovyDegrees, aspect, near, far );
		quad9Scene.setPerspective(fovyDegrees, aspect, near, far);

		// ----------- box scene ------------------
		boxScene = new Scene("Box scene; with/without textures");
		for (int q = 0; q < locs.length; q++)
			boxScene.addShape(makeBox(locs[q], sizes[q], txture[q], texWts[q]));
		boxScene.setLookAt(eye, at, up);
		boxScene.setOrtho(left, right, bottom, top, near, far);
		// boxScene.setPerspective( fovyDegrees, aspect, near, far );
		boxScene.setPerspective(fovyDegrees - 1f, aspect, near, far);

		// ----------- box scene ------------------
		// --------------Mipmapped and Non mipmapped image
		// ---------------------------
		box10Scene = new Scene("Box Scene->Left - Mipmapped, Right - Non Mipmapped");
		for (int q = 0; q < 2; q++) {
			box10Scene.addShape(makeBox(locs[q], sizes[q], txture[q], texWts[q]));
			box10Scene.addShape(makeBox(locations1[q], sizes1[q], mytextures[3], texWts1[q]));
		}

		box10Scene.setLookAt(eye, at, up);
		box10Scene.setOrtho(left, right, bottom, top, near, far);
		box10Scene.setPerspective(fovyDegrees, aspect, near, far);

		// ------------CylinderScene------------------------
		cylinderScene = new Scene("cylinderScene; with customtextures");
		for (int q = 0; q < cylinderLocation.length; q++)
			cylinderScene.addShape(makeCylinder(cylinderLocation[q], cylinderSize[q], textures[3], texWts[q]));
		cylinderScene.setLookAt(eye, at, up);
		cylinderScene.setOrtho(left, right, bottom, top, near, far);
		// boxScene.setPerspective( fovyDegrees, aspect, near, far );
		cylinderScene.setPerspective(fovyDegrees, aspect, near, far);

		// ------------CylinderScene------------------------
		cylinderScene1 = new Scene("cylinderScene2; without custom textures");
		for (int q = 0; q < cylinderLocation.length; q++)
			cylinderScene1.addShape(makeCylinder(cylinderLocation[q], cylinderSize[q], textures[0], texWts[q]));
		cylinderScene1.setLookAt(eye, at, up);
		cylinderScene1.setOrtho(left, right, bottom, top, near, far);
		cylinderScene1.setPerspective(fovyDegrees, aspect, near, far);

	}

	// ------------------ buildTextures --------------------------
	/**
	 * For each file in the imageFiles array, build the associated Texture
	 * object.
	 */
	private void buildTextures() {
		textures = new Texture[imageFiles.length];
		System.out.println(imageFiles.length);

		for (int f = 0; f < imageFiles.length; f++) {
			textures[f] = createTexture(imageFiles[f]);
		}

	}

	// ------------------ buildMyTextures --------------------------
	/**
	 * For each file in the imageFiles array, build the associated Texture
	 * object.
	 */
	private void buildMyTextures() {
		mytextures = new Texture[13];

		for (int f = 0; f < 13; f++) {
			mytextures[f] = createMyTexture(imageFiles[1], f);
		}
	}

	// ------------------ makeBox( String, Texture, float -----------------
	/**
	 * Create the object that makes up the box scene.
	 */
	private Shape3D makeBox(Vector3f loc, Vector3f scale, Texture txture, float texWt) {

		Box box = new Box();
		box.setLocation(loc.x, loc.y, loc.z);
		box.setSize(scale.x, scale.y, scale.z);
		if (txture != null) {
			box.setTexture(txture);
			box.setTextureWeight(texWt);
		}
		return box;
	}

	// ------------------ makeQuad( String, Texture, float -----------------
	/**
	 * Create a quad object for a quad scene.
	 */
	private Shape3D makeQuad(Vector3f loc, Vector3f scale, Texture txture, float texWt) {
		Quad quad = new Quad();
		quad.setLocation(loc.x, loc.y, loc.z);
		quad.setSize(scale.x, scale.y, scale.z);

		if (txture != null) {
			quad.setTexture(txture);
			quad.setTextureWeight(texWt);
		}
		return quad;
	} // ------------------ makeQuad2( String, Texture, float -----------------

	/**
	 * Create a quad object for a quad scene.
	 */
	private Shape3D makeQuad2(Vector3f loc, Vector3f scale, Texture txture, float texWt) {
		Quad quad = new Quad2();
		quad.setLocation(loc.x, loc.y, loc.z);
		quad.setSize(scale.x, scale.y, scale.z);

		if (txture != null) {
			quad.setTexture(txture);
			quad.setTextureWeight(texWt);
		}
		return quad;
	}

	// ------------------ makeCylinder-----------------------------------------
	/**
	 * Create a cylinder object for a cylinder scene.
	 */

	private Shape3D makeCylinder(Vector3f loc, Vector3f scale, Texture txture, float texWt) {
		Cylinder cylinder = new Cylinder(9);
		cylinder.setLocation(loc.x, loc.y, loc.z);
		cylinder.setSize(scale.x, scale.y, scale.z);
		cylinder.setColor(1f, 0f, 1f);
		cylinder.setRotate(90, 0, 1, 0);
		if (txture != null) {
			cylinder.setTexture(txture);
			cylinder.setTextureWeight(texWt);
		}
		return cylinder;

	}

	// --------------------- keyHandler ---------------------------
	/**
	 * Make this is a full-fledged method called from the invoke method of the
	 * anonymous class created in setupKeyHandler.
	 * 
	 * @param long
	 *            win window Id
	 * @param int
	 *            key key code
	 * @param int
	 *            code "scancode" is low-level non-standard internal code
	 * @param int
	 *            action GLFW_PRESS or GLFW_RELEASE
	 * @param int
	 *            mods bits in int encode modifier keys pressed GLFW_MOD_ALT |
	 *            GLFW_MOD_SHIFT | GLFW_MOD_CONTROL | GLFW_MOD_SUPER (cmd on
	 *            mac)
	 */
	public void keyHandler(long win, int key, int code, int action, int mods) {
		if (curScene == null)
			return;
		switch (key) {
		// ------------ Perspective/Parallel projection toggle -----------------
		case GLFW_KEY_P:
			if (action == GLFW_RELEASE) // use release so user can change mind
			{
				usePerspective = !usePerspective;
				curScene.usePerspective(usePerspective);
			}
			break;
		// ------------ Key L- Polygon Line draw mode
		// ---------------------------
		// ------------Key l - Pan Left to the window --------------------------
		case GLFW_KEY_L:
			if (action == GLFW_RELEASE && ((GLFW_MOD_SHIFT & mods) == 1)) 
				glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			else {

				curScene.panLeft(panUnits);
				panUnits = panUnits + 0.01f;
			}
			break;
		// ------------Key r - Pan right to the window-----------------------

		case GLFW_KEY_R:
			if (action == GLFW_RELEASE) {
				curScene.panRight(panUnits);
				panUnits = panUnits + 0.01f;
			}
			break;
		// ------------Key U - Pan up to the window --------------------------
		case GLFW_KEY_U:
			if (action == GLFW_RELEASE) {
				curScene.panUp(panUnits);
				panUnits = panUnits + 0.01f;
			}
			break;
		// ------------Key D - Pan down to the window --------------------------
		case GLFW_KEY_D:
			if (action == GLFW_RELEASE) {
				curScene.panDown(panUnits);
				panUnits = panUnits + 0.01f;
			}
			break;

		// -------------- Polygon Fill mode -----------------------------------
		case GLFW_KEY_F:
			if (action == GLFW_RELEASE) // use release so user can change mind
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			break;

		// ------------ Color/ Texture Blend c Key will increment texture by 0.1
//		// ---------- key C will decrement texture by 0.1 for each key press
		case GLFW_KEY_C:
			if (action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_RELEASE) {
				c += 0.1f;
				if (((GLFW_MOD_SHIFT & mods) == 0) && c <= 1.0f) // lower case c
				{
					Shape3D.textureWeight = 1 - c;

				} else {
					C = C - 0.1f;
					if (C >= 0.0f) {
						Shape3D.textureWeight = 1 - C;
					}
				}
			}
			break;

		// ----------------------- Zoom -In -----------------------------------
		case GLFW_KEY_I:
			if (action == GLFW_RELEASE && zoomInUnit <= 6) {
				curScene.zoomIn(zoomInUnit);
				zoomInUnit = zoomInUnit + 0.02f;

			}
			break;
		// ----------------------- Zoom -In -----------------------------------
		case GLFW_KEY_O:
			if (action == GLFW_RELEASE) {
				curScene.zoomOut(-zoomOutUnit);
				zoomOutUnit++;
			}
			break;

		// --------------------- Back Space - Reset to Original position -----

		case GLFW_KEY_BACKSPACE:
			if (action == GLFW_RELEASE) {
				curScene.setLookAt(eye, at, up);
			}
			break;

		// --------------------- Smaller and Larger fov -----------------------
		case GLFW_KEY_A:
			if (action == GLFW_RELEASE) {
				if (((GLFW_MOD_SHIFT & mods) == 1)) {
					if(usePerspective)
					{
					curScene.setPerspectiveA(smaller_fovy_angle, aspect, near, far);
					smaller_fovy_angle--;
					}
					else
						curScene.setOrthoA(0.2f);
					

				} else {
					if(usePerspective){
					curScene.setPerspectiveA(larger_fovy_angle, aspect, near, far);
					larger_fovy_angle++;
					}
					else
					{
						curScene.setOrthoA(0.2f);
					}
					
				}
			}
			break;

		// --------------- next scene ----------------------------------
		case GLFW.GLFW_KEY_PERIOD: // next scene
			if (action == GLFW.GLFW_RELEASE) {
				// Just switch back and forth between the available scenes
				if (curScene == quadScene)
					curScene = boxScene;
				else if (curScene == boxScene)
					curScene = cylinderScene;
				else if (curScene == cylinderScene)
					curScene = quad2Scene;
				else if (curScene == quad2Scene)
					curScene = quad3Scene;
				else if (curScene == quad3Scene)
					curScene = quad4Scene;
				else if (curScene == quad4Scene)
					curScene = quad5Scene;
				else if (curScene == quad5Scene)
					curScene = quad6Scene;
				else if (curScene == quad6Scene)
					curScene = quad7Scene;
				else if (curScene == quad7Scene)
					curScene = quad8Scene;
				else if (curScene == quad8Scene)
					curScene = quad9Scene;
				else if (curScene == quad9Scene)
					curScene = box10Scene;
				else if (curScene == box10Scene)
					curScene = cylinderScene1;


				glfwSetWindowTitle(win, curScene.getTitle());
				curScene.updateAllTransforms();
			}
			break;
		// --------------- previous scene -------------------------------
		case GLFW.GLFW_KEY_COMMA: // prev scene
			if (action == GLFW.GLFW_RELEASE) {
				// Just switch back and forth between quadScene and boxScene
				if (curScene == boxScene)
					curScene = quadScene;
				else if (curScene == cylinderScene)
					curScene = boxScene;
				else if (curScene == quadScene)
					curScene = cylinderScene1;
				else if (curScene == cylinderScene1)
					curScene = box10Scene;
				else if (curScene == box10Scene)
					curScene = quad9Scene;
				else if (curScene == quad9Scene)
					curScene = quad8Scene;
				else if (curScene == quad8Scene)
					curScene = quad7Scene;
				else if (curScene == quad7Scene)
					curScene = quad6Scene;
				else if (curScene == quad6Scene)
					curScene = quad5Scene;
				else if (curScene == quad5Scene)
					curScene = quad4Scene;
				else if (curScene == quad4Scene)
					curScene = quad3Scene;
				else if (curScene == quad3Scene)
					curScene = quad2Scene;

				glfwSetWindowTitle(win, curScene.getTitle());
				curScene.updateAllTransforms();
			}
			break;

		// ------------ Quit program ------------------------------------
		case GLFW_KEY_ESCAPE:
		case GLFW_KEY_Q:
			// this is another exit key
			if (action == GLFW_RELEASE) // use release so user can change mind
				glfwSetWindowShouldClose(win, true);
			break;
		// ----------- any other keys must be rotation keys or invalid
		default:
			rotationKeyHandler(key, action, mods);
			break;
		}
	}

	// --------------------- createTexture( name, stbflag ) ------------------
	/**
	 * Create a texture with the behavior we want.
	 */
	Texture createTexture(String fileName) {
		UtilsLWJGL.glError("--->createTexture"); // clear old glerrors

		Texture texture = null;
		try {

			texture = Texture.loadTexture(fileName);
			UtilsLWJGL.glError("<---loadTexture");
		} catch (Exception exc) {
			System.err.println("Unable to create Texture from " + fileName);
			exc.printStackTrace();
			System.exit(1);
		}
		UtilsLWJGL.glError("<---createTexture"); // any glerrors in texture?

		return texture;
	}

	// --------------------- createMyTexture( name, stbflag, flag )-------------

	/**
	 * Create a custom texture with the behavior we want.
	 */
	Texture createMyTexture(String fileName, int f) {
		UtilsLWJGL.glError("--->createTexture"); // clear old glerrors

		Texture texture = null;
		try {

			texture = Texture.loadTexture(fileName, f);
			UtilsLWJGL.glError("<---loadTexture");
		} catch (Exception exc) {
			System.err.println("Unable to create Texture from " + fileName);
			exc.printStackTrace();
			System.exit(1);
		}
		UtilsLWJGL.glError("<---createTexture"); // any glerrors in texture?

		return texture;
	}

	// --------------------- rotationKeyHandler ---------------------------
	/**
	 * Handle key events that specify rotations.
	 * 
	 * @param int
	 *            key key code
	 * @param int
	 *            action GLFW_PRESS or GLFW_RELEASE
	 * @param int
	 *            mods bits in int encode modifier keys pressed GLFW_MOD_ALT |
	 *            GLFW_MOD_SHIFT | GLFW_MOD_CONTROL | GLFW_MOD_SUPER (cmd on
	 *            mac)
	 */
	private void rotationKeyHandler(int key, int action, int mods) {
		switch (key) {
		// ------------ Rotations about X axis -------------------
		// Use x, X and UP DOWN keys
		case GLFW_KEY_X:
			if (action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_RELEASE) {
				if ((GLFW_MOD_SHIFT & mods) == 0) // it's lower case
					curScene.rotateX(deltaRotate);
				else
					curScene.rotateX(-deltaRotate);
			}
			break;
		case GLFW.GLFW_KEY_UP:
			if (action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_RELEASE)
				curScene.rotateX(-deltaRotate);
			break;
		case GLFW.GLFW_KEY_DOWN:
			if (action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_RELEASE)
				curScene.rotateX(deltaRotate);
			break;
		// ------------ Rotations about Y axis -------------------
		// Use y, Y and LEFT/RIGHT keys
		case GLFW_KEY_Y:
			if (action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_RELEASE) {
				if ((GLFW_MOD_SHIFT & mods) == 0) // it's lower case
					curScene.rotateY(deltaRotate);
				else
					curScene.rotateY(-deltaRotate);
			}
			break;
		case GLFW.GLFW_KEY_RIGHT:
			if (action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_RELEASE)
				curScene.rotateY(deltaRotate);
			break;
		case GLFW.GLFW_KEY_LEFT:
			if (action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_RELEASE)
				curScene.rotateY(-deltaRotate);
			break;
		// ------------ Rotations about Z axis -------------------
		// Just z, Z keys
		case GLFW_KEY_Z:
			if (action == GLFW.GLFW_REPEAT || action == GLFW.GLFW_RELEASE) {
				if ((GLFW_MOD_SHIFT & mods) == 0) // it's lower case
					curScene.rotateZ(deltaRotate);
				else
					curScene.rotateZ(-deltaRotate);
			}
			break;
		}
	}

	// ------------------------ redraw() ----------------------------
	/**
	 * Initiate scene redraw invocations.
	 */
	void redraw() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		if (curScene != null)
			curScene.redraw();
		glFlush();
	}
}
