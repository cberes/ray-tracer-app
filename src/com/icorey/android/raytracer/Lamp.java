/*
* Lamp.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * Point lamp
 *
 * @author corey
 */
public class Lamp {
	// lamp's color
	private Vector color;
	
	// lamp's position
	private Vector position;
	
	/**
	 * Constructor
	 *
	 * @param x x coord
	 * @param y y coord
	 * @param z z coord
	 * @param red red component 
	 * @param green green component
	 * @param blue blue component
	 */
	public Lamp(double x, double y, double z,
			    double red, double green, double blue) {
		position = new Vector(x, y, z);
		
		color = new Vector(red, green, blue);
	}
	
	/**
	 * Return position vector
	 *
	 * @return position
	 */
	public Vector getPosition() {
		return position;
	}
	
	/**
	 * Return color vector
	 *
	 * @return color
	 */
	public Vector getColor() {
		return color;
	}
	
	/**
	 * Return red component
	 *
	 * @return red component
	 */
	public double getRed() {
		return color.getX();
	}
	
	/**
	 * Return green component
	 *
	 * @return green component
	 */
	public double getGreen() {
		return color.getY();
	}
	
	/**
	 * Return blue component
	 *
	 * @return blue component
	 */
	public double getBlue() {
		return color.getZ();
	}
}
