/*
* Texture.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * Describes colors of an object
 *
 * @author corey
 */
public abstract class Texture {
	
	/**
	 * Return ambient color at a point on the object
	 *
	 * @param x x coord
	 * @param y y coord
	 * @return ambient color vector
	 */
	public abstract Vector getAmbientColor(double x, double y);
	
	/**
	 * Return diffuse color at a point on the object
	 *
	 * @param x x coord
	 * @param y y coord
	 * @return diffuse color vector
	 */
	public abstract Vector getDiffuseColor(double x, double y);
	
	/**
	 * Return specular color at a point on the object
	 *
	 * @param x x coord
	 * @param y y coord
	 * @return specular color vector
	 */
	public abstract Vector getSpecularColor(double x, double y);

}
