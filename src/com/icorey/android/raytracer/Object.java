/*
* Object.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * Class representing an object
 *
 * @author corey
 */
public abstract class Object {
	// object's illumination model
	protected IlluminationModel im;
	
	// object texture
	protected Texture texture;
	
	// important vectors
    protected Vector origin, direction, normal;
    
    // coefficients
    protected double kr, kt, n;
    
    // object ID
    protected int ID;
	
	/**
	 * Find the intersection of an object with the ray (if there is one) and
	 * find the corresponding normal
	 *
	 * @param origin ray's origin
	 * @param direction ray's direction
	 * @param intersection empty intersection vector
	 * @param normal empty normal vector
	 * @return intersection flag
	 */
	public abstract double intersect(Vector origin, Vector direction, Vector intersection, Vector normal);
	
	/**
	 * Call illumination model's illumination model to find color at the
	 * intersection point
	 *
	 * @return color vector
	 */
	public Vector illuminate(Vector intersection, Vector normal, Vector lightSource, Vector lightColor, double lit) {
		return im.illuminate(intersection, normal, lightSource, lightColor, Camera.getInstance().getEye(), lit);
	}
	
	/**
	 * Return reflection coefficient
	 *
	 * @return kr
	 */
	public double getKR() {
		return kr;
	}
	
	/**
	 * Return transmission coefficient
	 *
	 * @return kt
	 */
	public double getKT() {
		return kt;
	}
	
	/**
	 * Return refractive index
	 *
	 * @return n
	 */
	public double getN() {
		return n;
	}
	
	/**
	 * Return ID
	 *
	 * @return ID
	 */
	public int getID() {
		return ID;
	}
}
