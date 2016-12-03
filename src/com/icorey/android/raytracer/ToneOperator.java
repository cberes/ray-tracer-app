/*
* ToneOperator.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * Abstract class for tone operators
 *
 * @author corey
 */
public abstract class ToneOperator {
	
	// Scen Lmax and device Lmax
	protected int Lmax, Ldmax;
	
	// log sum, delta, 1/Ldmax
	protected double logsum, delta, LdmaxInv;
	
	// image data
	protected Vector image[][];
	
	/**
	 * Constructor
	 *
	 * @param Lmax
	 * @param canvas
	 */
	public ToneOperator(int Lmax) {
		this.Lmax = Lmax;
		
		// device Lmax is always 100
		Ldmax = 100;
		LdmaxInv = 1.0 / Ldmax;
		
		// delta is just a small value to prevent logsum from going to infinity
		delta = 0.0001;
		logsum = 0;
		
		image = new Vector[World.right][World.top];
	}
	
	/**
	 * Add color data at a point to the image
	 *
	 * @param x x coord
	 * @param y y coord
	 * @param c color vector
	 */
	public void add(int x, int y, Vector c) {
		double abslum;
		
		// scale color by Lmax
		image[x][y] = new Vector(c.getX(), c.getY(), c.getZ());
		image[x][y] = image[x][y].scale(Lmax);
		image[x][y].cap(Lmax);
		
		// get absolute luminance
		abslum = 0.27 * image[x][y].getX() + 0.67 * image[x][y].getY() + 0.06 * image[x][y].getZ();
		
		// sum log of luminance
		logsum += Math.log(delta + abslum);
	}
	
	/**
	 * Perform tone reproduction and draw the image
	 *
	 */
	public abstract int[] drawImage();
}
