/**
 * Quad2 is identical to Quad except that it doubles the texture coordinate
 * mapping range in both s and t, so that it is easy to show more of the Texture
 * parameter mappings.
 * 
 * @author rdb
 *
 */
public class Quad2 extends Quad {
	// Quad texture coordinates use same notation as position coords
	private float[] texCoords = { // 2-element texture coordinates;
									// front face 2 triangles: lbn, rbn, rtn and
									// lbn, rtn, ltn
									// lbn rbn rtn lbn rtn ltn
			0, 0, 2, 0, 2, 2, 0, 0, 2, 2, 0, 2,
			// back face 2 triangles: lbn, rbn, rtn and lbn, rtn, ltn
			// lbn rbn rtn lbn rtn ltn
			0, 0, 2, 0, 2, 2, 0, 0, 2, 2, 0, 2 };

	// ------------- constructor -----------------------
	/**
	 * Construct the data for this box object.
	 */
	public Quad2() {

		super(); // Let Quad class create everything
		setTextureCoordData(texCoords, 2); // then change texture coords
	}
}
