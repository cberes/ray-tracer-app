/*
* Sphere.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

import java.lang.Math;

/**
 * Sphere
 *
 * @author corey
 */
public class Sphere extends Object {
	// center (x, y, z) and radius (r)
    private final double x, y, z, rinv, r2;
    
    /**
     * Default constructor
     *
     */
    public Sphere() {
    	this(0,0,0,1,1,1,1,2, 0,0, 0, null, false);
    }
    
    /**
     * Constructor
     *
     * @param x center (x)
     * @param y center (y)
     * @param z center (z)
     * @param r radius
     * @param ka ambient coefficient
     * @param kd diffuse coefficient
     * @param ks specular coefficient
     * @param ke specular exponent
     * @param kr reflection coefficient
     * @param kt transmission coefficient
     * @param n refractive index
     * @param texture texture
     * @param pb flag to enable Phong-Blinn illumination
     */
    public Sphere(double x, double y, double z, double r,
    			  double ka, double kd, double ks, int ke,
    			  double kr, double kt, double n,
    			  Texture texture, boolean pb) {
    	// get ID
    	ID = World.newID();
    	
    	//position
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	rinv = 1.0 / r;
    	r2 = r * r;
    	
    	// coefficients
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
    	double B, C, dis, dis2, t1, t2;
    	double t = Double.MAX_VALUE;
    	
    	double ox = origin.getX() - x;
    	double oy = origin.getY() - y;
    	double oz = origin.getZ() - z;
    	
    	// values for quadratic equation
    	B = (direction.getX() * ox +
    		 direction.getY() * oy +
    		 direction.getZ() * oz) * 2.0;
    	C = ox * ox + oy * oy + oz * oz - r2;
    		
    	// discriminant
    	dis = B * B - 4.0 * C;
    	
    	// intersection depends on discriminant
    	if (dis == 0.0) {
    		// intersected with surface
    		// solve quadratic equation
    		t = B * -0.5;    	
    		
    		// get intersection vector
    		intersection.copy(origin.add(direction.scale(t)));
    		
    		// get normal vector
    		normal.copy(new Vector((intersection.getX() - x) * rinv,
    			    			(intersection.getY() - y) * rinv,
    			    			(intersection.getZ() - z) * rinv));	
    	} else if (dis > 0) {
    		// we might have intersected
    		// get solutions to quadratic equation
    		dis2 = Math.sqrt(dis);
    		t1 = (-B + dis2) * 0.5;
    		t2 = (-B - dis2) * 0.5;

    		// find correct (least positive solution)
    		if (t1 < 0 && t2 < 0) {
    			// both solutions negative (didn't intersect after all)
				return t;
    		} else if (t1 >= 0 && t2 >= 0) {
    			t = Math.min(t1, t2);
    		} else {
    			t = Math.max(t1, t2);
    		}
    		
    		// get intersection vector
    		intersection.copy(origin.add(direction.scale(t)));
    		
    		// get normal vector
    		normal.copy(new Vector((intersection.getX() - x) * rinv,
    			    			(intersection.getY() - y) * rinv,
    			    			(intersection.getZ() - z) * rinv));
    	}
    	
    	return t;
    }
}