import org.joml.*;

/**
 * LWJGL.java encapsulates key "global" LWJGL-related information for this
 * application. The class implements the Holder pattern.
 *
 * Although many simple applications can have a single set of all these
 * variables, other applications will have to associate many of them with each
 * "scene" or each object class, or even each individual object.
 * 
 * All fields are package protected.
 * 
 * @author rdb
 */
public class LWJGL {
	static int shaderProgram; // default shader program
	static long windowId; // glfw window id

	static Matrix4f glNormalMatrix = new Matrix4f(); // used to transform
														// normals
}
