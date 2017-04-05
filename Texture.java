
/*
 * The MIT License ( MIT )

 *
 * Copyright © 2014, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files ( the "Software" ), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ------------------------
 * rdb 11/08/15 downloaded from 
 * https://github.com/SilverTiger/lwjgl3-tutorial/blob/master/src/silvertiger/tutorial/lwjgl/graphic/Texture.java
 *
 * 1. Changed static LoadTexture to an alternative constructor.
 * 2. Added support to use STB image loader as option to AWT images.
 */
//rdb package silvertiger.tutorial.lwjgl.graphic;

import java.io.*;

import java.nio.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import com.sun.net.httpserver.Filter;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.stb.STBImage.*;

/**
 * This class represents a texture.
 *
 * @author Heiko Brumme
 */
public class Texture {
	private final int id; // Stores the handle of the texture
	private int uvId; // rdb uniform variable id
	private int textureType; // GL_TEXTURE_2D or GL_TEXTURE_CUBE_MAP

	private final int width; // Width of the texture.
	private final int height; // Height of the texture.

	int flag = 0; // Flag to keep track of the allocated texture paramters for the scene
	
	/**
	 * Creates a texture with specified width, height and data.
	 *
	 * @param int
	 *            width Width of the texture
	 * @param int
	 *            height Height of the texture
	 * @param ByteBuffer
	 *            data Picture Data in RGB8 or RGBA format
	 * @param int
	 *            imageFormat tested with GL_RGB8 and GL_RGBA
	 * 
	 */
	public Texture(int width, int height, ByteBuffer data, int imageFormat) {
		UtilsLWJGL.glError("--->Texture ctor"); // clean out old glerrors
		id = glGenTextures();
		bind();

		this.width = width;
		this.height = height;

		// **************** Warning ***********************************
		// ----- This code needs to change once you add another kind of texture
		// such as CUBE_MAP. You need to be able to tell the constructor
		// which kind of texture to create. Each kind needs its own
		// active texture unit, but multiple textures can be in the same
		// unit, as long as they are all the same kind of texture..
		//
		textureType = GL_TEXTURE_2D;
		uvId = glGetUniformLocation(LWJGL.shaderProgram, "texImage");

		// GL13.glActiveTexture( GL13.GL_TEXTURE0 ); // defaults to 0
		glUniform1i(uvId, 0);

		UtilsLWJGL.glError("<--- Texture ctor"); // errors?
	}

	/**
	 * Load texture from file.
	 *
	 * @param imageFile File image file path name
	 * @param f - flag
	 *            
	 * @return Texture from specified file
	 */
	public static Texture loadTexture(String imageFile, int f) throws IOException {
		ByteBuffer fileBuffer;
		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);
		ByteBuffer image;
		int width = 256, height = 256;

		stbi_set_flip_vertically_on_load(true);

		// Read the raw file data from the file into a ByteBuffer and
		// last parameter is initial buffer size; will be increased if needed.
		fileBuffer = UtilsLWJGL.ioResourceToByteBuffer(imageFile, 8 * 1024);

		// extract image information: w, h and number of components into
		// variables.
		if (!stbi_info_from_memory(fileBuffer, w, h, comp))
			throw new IOException(stbi_failure_reason());

		// Use raw data to create wxh input data with "comp" components into
		// image data with 3 components
		image = stbi_load_from_memory(fileBuffer, w, h, comp, 3);
		if (image == null)
			throw new IOException(stbi_failure_reason());

		System.err.printf("Image: %s %dx%d\n", imageFile, w.get(0), h.get(0));

		int tex = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, tex);

		Texture t = null;
		// Flag is compared and corresponding texture paramter is defined to the makeTexture
		if (f == 0) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_MIN_FILTER, GL_NEAREST, GL_TEXTURE_MAG_FILTER,
					GL_LINEAR);

		} else if (f == 1) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_MIN_FILTER, GL_LINEAR, GL_TEXTURE_MAG_FILTER,
					GL_LINEAR);

		} else if (f == 2) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR,
					GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		} else if (f == 3) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR,
					GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		} else if (f == 4) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST,
					GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		} else if (f == 5) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST,
					GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		} else if (f == 6) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR,
					GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		} else if (f == 7) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T,
					GL_CLAMP_TO_EDGE);
		} else if (f == 8) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER, GL_TEXTURE_WRAP_T,
					GL_CLAMP_TO_BORDER);
		} else if (f == 9) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T,
					GL_CLAMP_TO_BORDER);
		} else if (f == 10) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER, GL_TEXTURE_WRAP_T,
					GL_CLAMP_TO_EDGE);

		} else if (f == 11) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T,
					GL_REPEAT);

		} else if (f == 12) {
			t = makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T,
					GL_CLAMP_TO_BORDER);

		}

		stbi_image_free(image);

		return t;// makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_WRAP_S,
					// GL_CLAMP_TO_EDGE,GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

	}

	/**
	 * Load texture from file.
	 *
	 * @param imageFile File image file path name
	 * 
	 *            
	 * @return Call to the makeTexture and
	 * Texture from specified file
	 */
	public static Texture loadTexture(String imageFile) throws IOException {
		ByteBuffer fileBuffer;
		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);
		ByteBuffer image;
		int width = 256, height = 256;

		stbi_set_flip_vertically_on_load(true);

		// Read the raw file data from the file into a ByteBuffer and
		// last parameter is initial buffer size; will be increased if needed.
		fileBuffer = UtilsLWJGL.ioResourceToByteBuffer(imageFile, 8 * 1024);

		// extract image information: w, h and number of components into
		// variables.
		if (!stbi_info_from_memory(fileBuffer, w, h, comp))
			throw new IOException(stbi_failure_reason());

		// Use raw data to create wxh input data with "comp" components into
		// image data with 3 components
		image = stbi_load_from_memory(fileBuffer, w, h, comp, 3);
		if (image == null)
			throw new IOException(stbi_failure_reason());

		System.err.printf("Image: %s %dx%d\n", imageFile, w.get(0), h.get(0));

		int tex = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, tex);

		return makeTexture(w.get(0), h.get(0), image, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE, GL_TEXTURE_WRAP_T,
				GL_CLAMP_TO_BORDER);
	}

	/**
	 * Sets a parameter of the texture.
	 *
	 * @param name
	 *            Name of the parameter
	 * @param value
	 *            Value to set
	 */
	public void setParameter(int name, int value) {
		glTexParameteri(GL_TEXTURE_2D, name, value);
	}

	/**
	 * Uploads image data with specified internal format, width, height and
	 * image format.
	 *
	 * @param internalFormat
	 *            Internal format of the image data
	 * @param width
	 *            Width of the image
	 * @param height
	 *            Height of the image
	 * @param format
	 *            Format of the image data
	 * @param data
	 *            Pixel data of the image
	 */
	public void uploadData(int internalFormat, int width, int height, int format, ByteBuffer data) {
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, data);
	}

	 /**
     * Creates a texture with specified width, height and data.
     *
     * @param width  Width of the texture
     * @param height Height of the texture
     * @param imageFile   Picture Data in RGBA format
     *
     * @return Texture from the specified data
     */
	public static Texture makeTexture(int width, int height, ByteBuffer data, int pname1, int param1, int pname2,
			int param2) {
		Texture texture = new Texture(width, height, data, GL_RGBA);
		texture.bind();
		texture.setParameter(pname1, param1);
		texture.setParameter(pname2, param2);

		texture.uploadData(GL_RGBA8, width, height, GL_RGBA, data);
		GL30.glGenerateMipmap(GL_TEXTURE_2D);

		return texture;

	}

	// ------------------ checkBuffer -----------------
	/**
	 * Write out the bytebuffer to a file for testing purposes. This should be
	 * part of UtilsLWJGL.
	 * 
	 * @author rdb
	 * @param buf
	 * @param nRows
	 * @param nCols
	 */
	private void printByteBuffer(ByteBuffer buf, int nRows, int nCols, String fileName) {
		PrintWriter out;
		try {
			out = new PrintWriter(fileName);
		} catch (IOException ioe) {
			System.err.println("Can't open " + fileName);
			return;
		}
		buf.rewind();
		int max[] = { 0, 0, 0, 0 };
		int min[] = { 256, 256, 256, 256 };

		for (int r = 0; r < nRows; r++) {
			out.printf("%3d: ", r);
			for (int c = 0; c < nCols; c += 4) {
				for (int b = 0; b < 4; b++) {
					int ubyte = (int) buf.get() & 0xff;
					if (ubyte > max[b])
						max[b] = ubyte;
					if (ubyte < min[b])
						min[b] = ubyte;
					out.print(ubyte + ",");
				}
				out.print("   ");
			}
			out.println();
		}
		out.close();
		System.out.println("Min: " + min[0] + " " + min[1] + " " + min[2] + "  " + min[3]);
		System.out.println("Max: " + max[0] + " " + max[1] + " " + max[2] + "  " + max[3]);
		buf.rewind();
	}

	/**
	 * Binds the texture.
	 */
	public void bind() {
		glBindTexture(GL_TEXTURE_2D, id);
	}

	/**
	 * Unbinds the texture.
	 */
	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	/**
	 * Delete the texture.
	 */
	public void delete() {
		glDeleteTextures(id);
	}

	/**
	 * Gets the texture width.
	 *
	 * @return Texture width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the texture id. Added by rdb.
	 *
	 * @return Texture id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Return texture type. Added by rdb.
	 *
	 * @return texure type simple or cubemap
	 */
	public int getTextureType() {
		return this.textureType;
	}

	/**
	 * Gets the uniform variable id. Added by rdb.
	 *
	 * @return vao id
	 */
	public int getUniformVariableId() {
		return this.uvId;
	}

	/**
	 * Gets the texture height.
	 *
	 * @return Texture height
	 */
	public int getHeight() {
		return height;
	}
}
