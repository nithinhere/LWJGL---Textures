
/**
 * Scene class encapsulates the key elements of a Scene:
 *           ArrayList of Shape3D objects
 *           Scene transformation information
 *           
 *  For later demos need to add lights to the scene.
 *
 * @author rdb
 * Created 11/03/15
 */

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

import java.lang.Math;
import java.util.*;
import java.nio.FloatBuffer;

import org.joml.*;
import org.lwjgl.system.MemoryUtil;

public class Scene {
	// --------------------- class variables ---------------------------------
	static private int sceneCounter = 0;
	// --------------------- instance variables ------------------------------
	protected ArrayList<Shape3D> shapes;
	protected Matrix4f viewMatrix = new Matrix4f();
	protected Matrix4f sceneMatrix = new Matrix4f();

	protected int shaderProgram; // shader program id
	private boolean sceneTransformChanged = true;
	private boolean viewChanged = true;
	private String sceneTitle = null;
	private int sceneIndex = -1;

	// ------- transformation parameters
	protected float xRadians = 0;
	protected float yRadians = 0;
	protected float zRadians = 0;
	protected Vector3f location = new Vector3f();
	protected Vector3f scale = new Vector3f(1, 1, 1);

	// --------- viewing parameters
	private Vector3f eye = new Vector3f(0, 0, 4);
	private Vector3f at = new Vector3f(0, 0, 0);
	private Vector3f up = new Vector3f(0, 1, 0);

	// --------- Viewing matrices
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f vsMatrix = new Matrix4f(); // view*scene
	private Matrix4f pvsMatrix = new Matrix4f(); // projection*view*scene
	private boolean usePerspective = true;

	FloatBuffer pvsBuf = null; // buffer for p*v*s

	// -------- projection parameters
	private float fovyDegrees = 90;
	private float fovy = (float) java.lang.Math.toRadians(fovyDegrees);
	private float aspect = 1;
	private float near = 0.1f;
	private float far = 20;

	private float left = -1;
	private float right = 1;
	private float bottom = -1;
	private float top = 1;

	// ----------------------- constructor ----------------------------------
	/**
	 * Create a Scene with no title.
	 */
	public Scene() {
		this(null);
	}

	/**
	 * Create a Scene with a title.
	 */
	public Scene(String title) {
		this.sceneIndex = sceneCounter++;
		if (title == null)
			sceneTitle = "Scene " + sceneIndex;
		else
			sceneTitle = title;
		shapes = new ArrayList<Shape3D>();
		pvsBuf = MemoryUtil.memAllocFloat(16); // buffer for pvs
		shaderProgram = LWJGL.shaderProgram;

		// set up default view settings
		updateAllTransforms();
	}

	// ------------------- addShape( Shape3D ) --------------------------------
	/**
	 * Add a shape to this scene.
	 * 
	 * @param shape
	 *            Shape3D
	 */
	public void addShape(Shape3D shape) {
		shapes.add(shape);
	}

	// ------------------- redraw( ) --------------------------------
	/**
	 * Update scene-related uniform variables and regenerate the Scene.
	 */
	public void redraw() {
		if (sceneTransformChanged || viewChanged)
			updateAllTransforms();
		for (Shape3D shape : shapes)
			shape.redraw();
	}

	// ------------------- usePerspective( boolean) ---------------------------
	/**
	 * SceneManager has a toggle key so user's can switch from a perspective to
	 * an ortho view. It calls this method.
	 * 
	 * @param persp
	 *            boolean true implies use perspective proj, else ortho
	 */
	public void usePerspective(boolean persp) {
		usePerspective = persp;
		if (persp)
			setPerspective(fovyDegrees, aspect, near, far);
		else
			setOrtho(left, right, bottom, top, near, far);
		updateAllTransforms();
	}

	// ------------------ update() --------------------------------
	/**
	 * This function is called by SceneManager when this scene is once again the
	 * current scene. Need to update the shader variables associated with the
	 * scene. Right now this only invokes the protected method updateView, but
	 * it's possible to expect that more should or could be done in the future
	 * as part of restarting the scene.
	 */
	public void update() {
		updateSceneTransform();
		updateViewTransform();
		updateAllTransforms();
	}

	// ------------------ getTitle() ---------------------------
	/**
	 * Return the scene's title.
	 * 
	 * @return title
	 */
	public String getTitle() {
		return sceneTitle;
	}

	// ---------------------------------------------------------------------
	/**
	 * Set lookat parameters. Probably better to have 3 Vector3f params.
	 * 
	 */
	public void setLookAt(Vector3f eye, Vector3f at, Vector3f up) {
		this.eye = new Vector3f(eye).normalize();
		this.at = new Vector3f(at);
		this.up = new Vector3f(up).normalize();

		viewMatrix.identity().lookAt(eye, at, up);
		sceneTransformChanged = true;
	}

	// ---------------------------------------------------------------------
	/**
	 * Set perspective parameters.
	 * 
	 * @param angle
	 *            float degrees of field of view in y
	 * @param ratio
	 *            float aspect ratio of fov x to fov y
	 * @param znear
	 *            float near clipping plane
	 * @param zfar
	 *            float far clipping plane
	 */
	public void setPerspective(float angle, float ratio, float znear, float zfar) {
		this.fovyDegrees = angle; // save in both radians and degrees
		this.fovy = (float) java.lang.Math.toRadians(angle);
		this.aspect = ratio;
		this.near = znear;
		this.far = zfar;

		projectionMatrix.identity().perspective(angle, ratio, znear, zfar);
		viewChanged = true;
	}

	// ---------------------------------------------------------------------
	/**
	 * Set ortho parameters.
	 * 
	 * @param left
	 *            float left clipping plane
	 * @param right
	 *            float right clipping plane
	 * @param bottom
	 *            float bottom clipping plane
	 * @param top
	 *            float top clipping plane
	 * @param znear
	 *            float near clipping plane
	 * @param zfar
	 *            float far clipping plane
	 */
	public void setOrtho(float left, float right, float bottom, float top, float znear, float zfar) {
		this.left = left;
		this.right = right;
		this.bottom = bottom;
		this.top = top;
		this.near = znear;
		this.far = zfar;
		projectionMatrix.identity().ortho(left, right, bottom, top, znear, zfar);
		viewChanged = true;
	}

	// ------------------ setRotateX( angle ) ---------------------------
	/**
	 * Set rotation about x to specified angle.
	 * 
	 * @param alpha
	 *            rotation about x of alpha degrees.
	 */
	public void setRotateX(float alpha) {
		xRadians = (float) java.lang.Math.toRadians(alpha);
		sceneTransformChanged = true;
	}

	// ------------------ setRotateY( angle ) ---------------------------
	/**
	 * Set rotation about y to specified angle.
	 * 
	 * @param alpha
	 *            rotation about y of alpha degrees.
	 */
	public void setRotateY(float alpha) {
		yRadians = (float) java.lang.Math.toRadians(alpha);
		sceneTransformChanged = true;
	}

	// ------------------ setRotateZ( angle ) ---------------------------
	/**
	 * Set rotation about z to specified angle.
	 * 
	 * @param alpha
	 *            rotation about z of alpha degrees.
	 */
	public void setRotateZ(float alpha) {
		zRadians = (float) java.lang.Math.toRadians(alpha);
		sceneTransformChanged = true;
	}

	// ------------------ rotateX( delta ) ---------------------------
	/**
	 * Update current x rotation by specified increment/decrement.
	 * 
	 * @param alpha
	 *            rotation changed by delta degrees.
	 */
	public void rotateX(float delta) {
		xRadians += (float) java.lang.Math.toRadians(delta);
		sceneTransformChanged = true;
	}

	// ------------------ rotateY( delta ) ---------------------------
	/**
	 * Update current y rotation by specified increment/decrement.
	 * 
	 * @param alpha
	 *            rotation changed by delta degrees.
	 */
	public void rotateY(float delta) {
		yRadians += (float) java.lang.Math.toRadians(delta);
		sceneTransformChanged = true;
	}

	// ------------------ rotateZ( delta ) ---------------------------
	/**
	 * Update current z rotation by specified increment/decrement.
	 * 
	 * @param alpha
	 *            rotation changed by delta degrees.
	 */
	public void rotateZ(float delta) {
		zRadians += (float) java.lang.Math.toRadians(delta);
		sceneTransformChanged = true;
	}

	// ------------------ updateSceneTransform --------------------------
	/**
	 * A scene transformation is a modeling transform applied to all objects in
	 * the scene prior to the viewing transformation. It is a convenient way to
	 * see a 3D scene without resorting to a complete interactive viewing
	 * transformation.
	 * 
	 * This scene transformation only applies a rotations: 1 about each axis,
	 * using joml library methods.
	 */
	public void updateSceneTransform() {
		sceneMatrix.identity();
		sceneMatrix.rotateX(xRadians).rotateY(yRadians).rotateZ(zRadians);
		sceneTransformChanged = false;
	}

	// ------------------ updateViewTransform --------------------------
	/**
	 * Make sure viewTransform is up to date.
	 */
	public void updateViewTransform() {
		viewMatrix.identity().lookAt(eye, at, up);
		viewChanged = false;
	}

	// ------------------ updateView --------------------------
	/**
	 * We have a constant viewing and projection specification. Can define it
	 * once and send the spec to the shader. In this demo it never changes,
	 * except for a new scene.
	 * 
	 * The scene transformation changes interactively, but it affects (in
	 * principle) many objects with different object modelling specifications
	 * that are not changing -- although each object has a different modelling
	 * transformation. At render time, the object modelling transform changes
	 * the most frequently, the scene transform less often, and (in this
	 * program) the proj and viewing transforms don't change at all.
	 *
	 * This code computes the composite of everything except the object
	 * transformation and loads it into a shader variable, which doesn't change
	 * throughout the rendering of a single frame. The Shade3D code updates the
	 * object transformation shader variable multiple times in a frame (in
	 * principle).
	 * 
	 * Any of the matrices recomputed here could be downloaded as uniform
	 * variables, allowing a wide variety of additional computations to take
	 * place in the shader -- such as mapping light parameters from one
	 * coordinate system to another so that all components are expressed in the
	 * same coordinate system.
	 */
	void updateAllTransforms() {
		UtilsLWJGL.glError("--->updateView"); // clean out any old glerrors
		if (sceneTransformChanged)
			updateSceneTransform();
		if (viewChanged)
			updateViewTransform();

		vsMatrix.set(viewMatrix).mul(sceneMatrix); // vs = view * scene
		pvsMatrix.set(projectionMatrix).mul(vsMatrix); // pvs = proj * vs

		// get stores this matrix into its argument -- a buffer in this case
		pvsBuf = pvsMatrix.get(pvsBuf);

		// --- now push the composite into a uniform var in vertex shader
		// this id does not need to be global since we never change
		// projection or viewing specs in this program.
		int unif_pvs = glGetUniformLocation(shaderProgram, "uPVS");
		glUniformMatrix4fv(unif_pvs, false, pvsBuf);

		UtilsLWJGL.glError("<---updateView"); // Any errors in updateView?
	}

	/*
	 * Below are the methods for performing Key interactions
	 * 
	 * @param - values are passed from the SceneManager class Handled
	 * particularly for the key handling
	 */
	// Set PerspectiveA - Handled for smaller and larger
	// fov angles respectively
	public void setPerspectiveA(float angle, float ratio, float znear, float zfar) {
		this.fovyDegrees = angle; // save in both radians and degrees
		this.fovy = (float) java.lang.Math.toRadians(angle);
		this.aspect = ratio;
		this.near = znear;
		this.far = zfar;

		projectionMatrix.identity().perspective(fovy, aspect, znear, zfar);

		viewChanged = true;
	}

	// setOrthoA - Handled for smaller and larger
	// fov angles respectively
	public void setOrthoA(float da) {
		// float dx = (float) (i * (right - left));
		// float dy = (float) (i * (top - bottom));
		left += da / 2;
		right -= da / 2;
		bottom += da / 2;
		top -= da / 2;

		if (left >= -0.3) {
			left = -0.3f;
			right = 0.3f;
			top = 0.3f;
			bottom = -0.3f;
		}

		projectionMatrix.identity().ortho(left, right, bottom, top, near, far);
		viewChanged = true;
	}

	// -----------------Zoom in --------------------------------------
	/*
	 * Zoom in method on the press of i key performed zoom in till 6 units. more
	 * than 6 resulted invalid value
	 */
	public void zoomIn(float units) {
		float n1, n2, n3;
		float i = units;
		n1 = at.x - eye.x;
		n2 = at.y - eye.y;
		n3 = at.z - eye.z;

		Vector3f n = new Vector3f(n1, n2, n3);

		Vector3f dz = new Vector3f(n.x, n.y, n.z + i);
		viewMatrix.identity().lookAt(dz, at, up);

		sceneTransformChanged = true;
	}

	// -----------------Zoom out --------------------------------------
	/*
	 * Zoom out method on the press of o key performed zoom out till 6 units.
	 * more than 6 resulted invalid value
	 */
	public void zoomOut(float units) {
		float n1, n2, n3;
		float i = units;
		n1 = at.x - eye.x;
		n2 = at.y - eye.y;
		n3 = at.z - eye.z;

		Vector3f n = new Vector3f(n1, n2, n3);

		Vector3f dz = new Vector3f(n1, n2, n.z - i);
		viewMatrix.identity().lookAt(dz, at, up);

		sceneTransformChanged = true;
	}

	// ---------------------- Pan Left ----------------------------------
	/*
	 * With the press of the L key Shape move towards the left side of the
	 * window incremented by 0.01units from the sceneManager Class
	 */
	public void panLeft(float units) {
		float n1, n2, n3;
		n1 = at.x;
		n2 = at.y;
		n3 = at.y;

		Vector3f n = new Vector3f(n1, n2, n3);

		Vector3f dz = new Vector3f(n.x - units, n.y, n.z);
		viewMatrix.identity().lookAt(eye, dz, up);

		sceneTransformChanged = true;
	}

	// ---------------------- Pan Right ----------------------------------
	/*
	 * With the press of the R key Shape move towards the Right side of the
	 * window incremented by 0.01units from the sceneManager Class
	 */

	public void panRight(float units) {
		float n1, n2, n3;
		n1 = at.x;
		n2 = at.y;
		n3 = at.y;

		Vector3f n = new Vector3f(n1, n2, n3);

		Vector3f dz = new Vector3f(n.x + units, n.y, n.z);
		viewMatrix.identity().lookAt(eye, dz, up);

		sceneTransformChanged = true;
	}
	// ---------------------- Pan Up ----------------------------------
	/*
	 * With the press of the U key Shape move upwards the window incremented by
	 * 0.01units from the sceneManager Class
	 */

	public void panUp(float units) {
		float n1, n2, n3;
		n1 = at.x;
		n2 = at.y;
		n3 = at.y;

		Vector3f n = new Vector3f(n1, n2, n3);

		Vector3f dz = new Vector3f(n.x, n.y + units, n.z);
		viewMatrix.identity().lookAt(eye, dz, up);

		sceneTransformChanged = true;
	}

	// ---------------------- Pan Down ----------------------------------
	/*
	 * With the press of the D key Shape move downwards the window incremented
	 * by 0.01units from the sceneManager Class
	 */

	public void panDown(float units) {
		float n1, n2, n3;
		n1 = at.x;
		n2 = at.y;
		n3 = at.y;

		Vector3f n = new Vector3f(n1, n2, n3);

		Vector3f dz = new Vector3f(n.x, n.y - units, n.z);
		viewMatrix.identity().lookAt(eye, dz, up);

		sceneTransformChanged = true;
	}

}