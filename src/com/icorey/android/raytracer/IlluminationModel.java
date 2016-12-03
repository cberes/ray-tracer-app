/*
* IlluminationModel.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * Illumination Model
 *
 * @author corey
 */
public abstract class IlluminationModel {
	// ambient, diffuse, and specular coefficients
	protected double ka, kd, ks;
	
	// specular exponent and object's ID
	protected int ke, ID;
	
	// texture
	protected Texture texture;
	
	/**
	 * Constructor
	 *
	 * @param ID source object's ID
	 * @param ka ambient coefficient
	 * @param kd diffuse coefficient
	 * @param ks specular coefficient
	 * @param ke specular exponent
	 * @param texture texture
	 */
	public IlluminationModel(int ID, double ka, double kd, double ks, int ke, Texture texture) {
		this.ID = ID;
		
		this.ka = ka;
		this.kd = kd;
		this.ks = ks;
		this.ke = ke;
		
		this.texture = texture; 
	}
	
	/**
	 * Get the color at the point of intersection
	 *
	 * @param intersection intersection point
	 * @param normal normal vector
	 * @param eye eye vector
	 * @return color vector
	 */
	public abstract Vector illuminate(Vector intersection, Vector normal, Vector lightSource, Vector lightColor, Vector eye, double lit);
}
