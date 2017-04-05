/**
 * Cylinder.java - a class implementation representing a Cylinder object in OpenGL Oct 16,
 * No of faces for the cylinder is provided from the 
 * constructor
 */
import java.util.ArrayList;

public class Cylinder extends Shape3D {
	// --------- instance variables -----------------
	final private int coordSize = 3;

	// configuration
	private float h = .8f; // height 
	private double r = .3; // radius of the cylinder
	private float originX = 0; // origin 
	private float originY = 0; // origin
	private float preX = 0; // set X
	private float preY = 0; // set Y
	private float x = 0;
	private float y = 0;
	private float upZ = .5f;
	private float downZ = upZ - h; // Up - height
	private ArrayList<Float> vertList = new ArrayList<Float>(); //  Arraylist stores vertex bufffers
	private ArrayList<Float> vertTextList = new ArrayList<Float>();

	// vertex coordinates
	private float[] verts = null;
	// instance variables
	int count = 0;

	// ------------- constructor -----------------------
	/**
	 * Construct the data for this box object.
	 * Cylinder constructor invoked by passing no of faces
	 * @param - faces
	 */
	public Cylinder(float faces) {

		preY = 0;
		preX = (float) r;

		double dx = Math.toRadians(360.0 / faces);

		double a = dx;
		for (int i = 0; i < faces; i++, a += dx) {
			x = (float) (r * Math.cos(a));
			y = (float) (r * Math.sin(a));

			addData(x, y);
			preX = x;
			preY = y;
		}

		int listSize = vertList.size();

		// Vertices stores vertexes
		float vert3[] = new float[36 * (int) faces];
		float vertText[] = new float[24 * (int) faces];
		count = 0;

		// upper circle
		for (int i = 0; i < listSize; i++) {
			vert3[i] = vertList.get(i);
		}
		// texts fortop Circle
		for (count = 0; count < vertTextList.size(); count++) {
			vertText[count] = vertTextList.get(count);
		}

		//bottom circle
		for (int i = 0; i < listSize; i += 9) {
			vert3[listSize + i + 0] = vertList.get(i + 0);
			vert3[listSize + i + 1] = vertList.get(i + 1);
			vert3[listSize + i + 2] = downZ;

			vert3[listSize + i + 3] = vertList.get(i + 6);
			vert3[listSize + i + 4] = vertList.get(i + 7);
			vert3[listSize + i + 5] = downZ;

			vert3[listSize + i + 6] = vertList.get(i + 3);
			vert3[listSize + i + 7] = vertList.get(i + 4);
			vert3[listSize + i + 8] = downZ;

		}

		// Texts for bottom circle
		for (int i = 0; i < listSize; i += 9) {
			vertText[count++] = 1;
			vertText[count++] = 1;

			vertText[count++] = 1;
			vertText[count++] = 0;

			vertText[count++] = 0;
			vertText[count++] = 1;
		}

		// Curved Surface - vertical face
		for (int i = 0, j = listSize * 2, k = 0; i < faces; i++, j += 18, k += 9) {

			vert3[j + 0] = vert3[k + 3];
			vert3[j + 1] = vert3[k + 4];
			vert3[j + 2] = upZ;

			vert3[j + 3] = vert3[k + 3];
			vert3[j + 4] = vert3[k + 4];
			vert3[j + 5] = downZ;

			vert3[j + 6] = vert3[k + 6];
			vert3[j + 7] = vert3[k + 7];
			vert3[j + 8] = downZ;

			vert3[j + 9] = vert3[k + 3];
			vert3[j + 10] = vert3[k + 4];
			vert3[j + 11] = upZ;

			vert3[j + 12] = vert3[k + 6];
			vert3[j + 13] = vert3[k + 7];
			vert3[j + 14] = downZ;

			vert3[j + 15] = vert3[k + 6];
			vert3[j + 16] = vert3[k + 7];
			vert3[j + 17] = upZ;

		}

		// Texts for curved Surface
		for (int i = 0; i < faces; i++) {

			vertText[count++] = 0;
			vertText[count++] = 1;

			vertText[count++] = 0;
			vertText[count++] = 0;

			vertText[count++] = 1;
			vertText[count++] = 0;

			vertText[count++] = 0;
			vertText[count++] = 1;

			vertText[count++] = 1;
			vertText[count++] = 0;

			vertText[count++] = 1;
			vertText[count++] = 1;
		}

		verts = vert3;

		setCoordData(verts, 3);
		setNormalData(verts, 3); // Normals are same as vertices!
		setTextureCoordData(vertText, 2);

	}

	// Append all the data to the Vertex list
	public void addData(float x, float y) {
		vertList.add(originX);
		vertList.add(originY);
		vertList.add(upZ);
		vertList.add(preX);
		vertList.add(preY);
		vertList.add(upZ);
		vertList.add(x);
		vertList.add(y);
		vertList.add(upZ);
		vertTextList.add(0f);
		vertTextList.add(1f);
		vertTextList.add(1f);
		vertTextList.add(0f);
		vertTextList.add(1f);
		vertTextList.add(1f);
	}
}
