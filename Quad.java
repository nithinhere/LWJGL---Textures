/**
 * Quad.java - this class supports a quad object with 2 independent sides.
 *
 * Created by rdb; Fall 2015
 *
 * 12/28/16 rdb: minor edits. 01/08/17 rdb: correct color handling and merge it
 * with Box model, which now uses some shared code in Shape3D that quad can use.
 * One big difference between Box and Quad, however, is that using the "face"
 * normal and color for each vertex makes more sense than with Box. Quads are 2D
 * objects in 3D with 2 separate sides. So, for Quads, the default is to use
 * face colors and face normals.
 */

public class Quad extends Shape3D {
	// --------- class variables -----------------

	// --------- instance variables -----------------
	// Quad position coordinates expressed as 3-element homog coordinates;
	// 3 letter codes identify quad corners as viewed from outside front
	// [lr][bt][nf] --> left/right bottom/top near/far
	private float[] vertices = {
			// front face 2 triangles: lbn, rbn, rtn and lbn, rtn, ltn
			// lbn rbn rtn
			-0.5f, -0.5f, 0, 0.5f, -0.5f, 0, 0.5f, 0.5f, 0,
			// lbn rtn ltn
			-0.5f, -0.5f, 0, 0.5f, 0.5f, 0, -0.5f, 0.5f, 0,
			// back face 2 triangles: rbf, lbf, ltf and rbf, ltf, rtf
			// rbf lbf ltf
			0.5f, -0.5f, 0, -0.5f, -0.5f, 0f, -0.5f, 0.5f, 0,
			// rbf ltf rtf
			0.5f, -0.5f, 0, -0.5f, 0.5f, 0f, 0.5f, 0.5f, 0, };
	// Quad normals data in same order as position data; all normals on
	// same side are the same.
	private float[] normals = {
			// front face 2 triangles: lbn, rbn, rtn and lbn, rtn, ltn
			// lbn rbn rtn
			0, 0, 1, 0, 0, 1, 0, 0, 1,
			// lbn rtn ltn
			0, 0, 1, 0, 0, 1, 0, 0, 1,
			// back face 2 triangles: rbf, lbf, ltf and rbf, ltf, rtf
			// rbf lbf ltf
			0, 0, -1, 0, 0, -1, 0, 0, -1,
			// rbf ltf rtf
			0, 0, -1, 0, 0, -1, 0, 0, -1, };
	// Quad texture coordinates use same notation as position coords
	private float[] texCoords = { // 2-element texture coordinates;
									// front face 2 triangles: lbn, rbn, rtn and
									// lbn, rtn, ltn
									// lbn rbn rtn lbn rtn ltn
			0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1,
			// back face 2 triangles: lbn, rbn, rtn and lbn, rtn, ltn
			// lbn rbn rtn lbn rtn ltn
			0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1 };

	// ------------- constructor -----------------------
	/**
	 * Construct the data for this box object.
	 */
	public Quad() {
		// and the coordinate data and texture coordinate data
		setCoordData(vertices, 3);
		setNormalData(normals, 3);
		setTextureCoordData(texCoords, 2);
	}
}
