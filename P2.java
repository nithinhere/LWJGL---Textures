
/**
 * TextureDemo.java - Demonstrates simple smooth shading, by coloring Box corners
 *            differently and shading across faces.
 *            
 * @author rdb
 * 10/20/15 version 1.0 derived from ViewDemo
 *          The only changes are in the Box class.
 *          This version still keeps the face oriented coloring which can be
 *             restored by compile time edit 
 *          and defines normals that are not used.
 *             
 * This program makes use of code from demos found at lwjgl.org accessed as
 * lwjgl3-demo-master and downloaded in late August 2015. It also uses a
 * slightly modified complete class from that package, UtilsLWJGL.
 *
 * 01/02/16 rdb version 1.5
 *          Added support for stb images
 *          Added command line arg (and class variable) to choose between
 *              old AWT version and STB version. STB is version desired
 *              for future. 
 *          This version will not be used in S17; want to add more 
 *              command line options and to split off the pure texture
 *              mapping and the blending: have a separate blending demo.
 *              Should also move the buffer testing code to UtilsLWJGL.
 * 01/03/16 rdb version 2.0
 *          Remove awt option
 *          Move blending features to separate demo
 *          Add command line features to specify images to load.
 */
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.glfw.*;

import java.io.*;

public class P2 {
	// ---------------------- class variables -------------------------
	// ---------------------- instance variables ----------------------
	// window size parameters
	int windowW = 780;
	int windowH = 800; // Header is about 20 pixels high.

	private SceneManager sceneManager; // scene manager class

	// We need to strongly reference callback instances.
	private long windowId;
	private GLFWErrorCallback errorCallback; // Eclipse incorrectly says unused
	private GLFWKeyCallback keyCallback; // Eclipse incorrectly says unused

	// -------------- View and Scene transformation parameters

	// --------------- init ------------------------------------------
	public P2() {
		// Setup error callback to print to System.err.
		// Make this call prior to openWindow.
		errorCallback = GLFWErrorCallback.createPrint(System.err).set();

		windowId = UtilsLWJGL.openWindow("TextureDemo", windowW, windowH);
		LWJGL.windowId = windowId;

		try {
			LWJGL.shaderProgram = UtilsLWJGL.makeShaderProgram("texture.vsh", "texture.fsh");
			glUseProgram(LWJGL.shaderProgram);
		} catch (IOException iox) {
			System.err.println("Shader construction failed.");
			System.exit(-1);
		}

		sceneManager = new SceneManager();
		setupKeyHandler();

		// set up common opengl characteristics that don't change
		glEnable(GL_DEPTH_TEST);
		glEnable(GL_CULL_FACE);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClearDepth(1.0f);

		/* Enable blending */
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		renderLoop();

		// Release window and window callbacks
		glfwFreeCallbacks(windowId);
		glfwDestroyWindow(windowId);
		glfwSetErrorCallback(null).free(); // free old errorCallback
		glfwTerminate();
	}

	// --------------------- setupKeyHandler ----------------------
	/**
	 * void setupKeyHandler
	 */
	private void setupKeyHandler() {
		// Setup a key callback. It is called every time a key is pressed,
		// repeated or released.
		glfwSetKeyCallback(LWJGL.windowId, keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long win, int key, int code, int action, int mods) {
				sceneManager.keyHandler(win, key, code, action, mods);
			}
		});
	}

	// -------------------------- renderLoop ----------------------------
	/**
	 * Loop until user closes the window or kills the program.
	 */
	private void renderLoop() {
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		// old while ( glfwWindowShouldClose( windowId ) == GL_FALSE )
		while (!glfwWindowShouldClose(windowId)) {
			// clear the framebuffer
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			// redraw the frame
			sceneManager.redraw();

			glfwSwapBuffers(windowId); // swap the color buffers

			// Wait for window events. The key callback above will only be
			// invoked during this call.
			// lwjgl demos use glfwPollEvents(), which uses nearly 2X
			// the cpu time for simple demos as glfwWaitEvents.
			glfwWaitEvents();
		}
	}

	// ------------------------- main ----------------------------------
	/**
	 * main checks for command line arguments to set up scenes.
	 */
	public static void main(String args[]) {
		new P2();
	}
}
