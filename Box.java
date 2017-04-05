/**
 * Box.java - a class implementation representing a Box object in OpenGL Oct 16,
 * 2013 rdb - derived from Box.cpp
 * 
 * 10/28/14 rdb - revised to explicitly draw faces - drawPrimitives ->
 * drawObject( GL2 ) - uses glsl 11/10/14 rdb - existing rebuilds glsl buffers
 * on every redraw. should and canonly do it once.
 */

public class Box extends Shape3D {
	// --------- instance variables -----------------

	// vertex coordinates
	private float[] verts = { // 3-element vertex coordinates;
								// 3 letter codes are cube corners [lr][bt][nf]
								// left/right bottom/top near/far
								// right face 2 triangles: rbn, rbf, rtf and
								// rbn, rtf, rtn
			0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
			0.5f,
			// top face: ltn, rtn, rtf and ltn, rtf, ltf
			-0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f,
			-0.5f,
			// back face: rbf, lbf, ltf and rbf, ltf, rtf
			0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f,
			0.5f, -0.5f,
			// left face: lbf, lbn, ltn and lbf, ltn, ltf -- corrected
			-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f,
			0.5f, -0.5f,
			// bottom face: lbf, rbf, rbn and lbf, rbn, lbn
			-0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f, -0.5f,
			-0.5f, 0.5f,
			// front face 2 triangles: lbn, rbn, rtn and lbn, rtn, ltn
			-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f, -0.5f, 0.5f,
			0.5f, };
	// ------------

	// Define texture coords at each vertex
	// 2-element texture coordinates;
	// 3 letter codes are cube corners
	// [lr][bt][nf] left/right bottom/top near/far
	private float[] boxTexCoords = {
			// right: rbn rbf rtf and rbn rtf rtn
			0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1,
			// top: ltn rtn rtf and ltn rtf ltf
			0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1,
			// back: rbf lbf ltf and rbf ltf rtf
			0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1,
			// left: lbf lbn ltn and lbf ltn ltf
			0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1,
			// bottom: lbf rbf rbn and lbf rbn lbn
			0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1,
			// front: lbn rbn rtn and lbn rtn ltn
			0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, };

	// ------------- constructor -----------------------
	/**
	 * Construct the data for this box object.
	 */
	public Box() {
		setCoordData(verts, 3);
		setNormalData(verts, 3); // Normals are same as vertices!
		setTextureCoordData(boxTexCoords, 2);
	}
}
