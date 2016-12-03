/*
* Floor.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * Floor
 *
 * @author corey
 */
public class Floor extends Object {
	// point on the plane
    private Vector point;
    
    // floor boundaries
    private double back, front, left, right;
    
    /**
     * Constructor
     *
     * @param normalX normal (x)
     * @param normalY normal (y)
     * @param normalZ normal (z)
     * @param pointX point (x)
     * @param pointY point (y)
     * @param pointZ point (z)
     * @param back back boundary
     * @param front front boundary
     * @param left left boundary
     * @param right right boundary
     * @param ka ambient coefficient
     * @param kd diffuse coefficient
     * @param ks specular coefficient
     * @param ke specular exponent
     * @param kr reflection coefficient
     * @param kt transmission coefficient
     * @param n refractive index
     * @param texture texture
     * @param pb flag for Phong-Blinn illumination
     */
    public Floor(double normalX, double normalY, double normalZ,
    			 double pointX, double pointY, double pointZ,
    		     double back, double front, double left, double right,
    		     double ka, double kd, double ks, int ke,
    		     double kr, double kt, double n,
    		     Texture texture, boolean pb) {
    	// get ID
    	ID = World.newID();
    	
    	// set boundaries
    	this.back = back;
    	this.front = front;
    	this.left = left;
    	this.right = right;
    	
    	// set normal  and point
    	normal = new Vector(normalX, normalY, normalZ);
    	point = new Vector(pointX, pointY, pointZ);
    	
    	// get coefficients
    	this.kr = kr;
    	this.kt = kt;
    	this.n = n;

    	// set up illumination model
    	if (pb)
    		im = new PhongBlinn(ID, ka, kd, ks, ke, texture);
    	else
    		im = new Phong(ID, ka, kd, ks, ke, texture);
    }
    
    /* (non-Javadoc)
     * @see com.android.raytracer.Object#intersect(com.android.raytracer.Vector, com.android.raytracer.Vector)
     */
    @Override
	public double intersect(Vector origin, Vector direction, Vector intersection, Vector normal) {
    	double f, den, temp;
    	double t = Double.MAX_VALUE;
    	
    	// copy normal vector
    	normal.copy(this.normal);
    	
    	// change normal so the intersection method works
		normal.setY(-1.0);
    	
		// get denominator value in intersection calculations
    	den = normal.dot(direction);
    	
    	// intersect plane if den > 0
    	if (den > 0.0) {
    		// more intersection calculation values
    		f = normal.dot(point);
    		temp = normal.dot(origin);
    		
    		// vector parameter
    		temp = (-temp + f) / den;
    		
    		// get intersection points
    		intersection.copy(origin.add(direction.scale(temp)));
    		
    		// if intersection is within boundaries of the floor, then we
    		// intersected it
    		if (intersection.getX() >= left && intersection.getX() <= right &&
    			intersection.getZ() <= back && intersection.getZ() >= front) {
    			normal.setY(1.0);
    			t = temp;
    		}
    	}
    	
    	return t;
    }
}
