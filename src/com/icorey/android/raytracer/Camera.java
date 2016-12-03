/*
* Camera.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * The camera
 *
 * @author corey
 */
public class Camera {
	// camera instance
	private static Camera instance = null;
	
	// eye vector
	private Vector eye;
	
	// look-at vector
	private Vector lookAt;

	/**
	 * 
	 * Camera constructor
	 * protected so it can't be initialized anywhere
	 *
	 */
	protected Camera() {
	}
	
	/**
	 * 
	 * Singleton constructor
	 *
	 * @return Camera instance
	 */
	public static Camera getInstance() {
		if(instance == null) {
			instance = new Camera();
		}
		return instance;
	}
	
	/**
	 * 
	 * Set the eye vector
	 *
	 * @param x x coord
	 * @param y y coord
	 * @param z z coord
	 */
	public void setEye(double x, double y, double z) {
		eye = new Vector(x, y, z);
	}
	
	/**
	 * 
	 * Set look-at vector
	 *
	 * @param x x coord
	 * @param y y coord
	 * @param z z coord
	 */
	public void setLookAt(double x, double y, double z) {
		lookAt = new Vector(x, y, z);
	}
	
	/**
	 * 
	 * Return the eye vector
	 *
	 * @return eye vector
	 */
	public Vector getEye() {
		return eye;
	}
	
	/**
	 * 
	 * Return the look-at vector
	 *
	 * @return look-at vector
	 */
	public Vector getLookAt() {
		return lookAt;
	}
}
