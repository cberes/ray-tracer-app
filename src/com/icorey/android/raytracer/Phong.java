/*
 * Phong.java
 *
 * Version:
 *     $Id$
 *
 * Revisions:
 *     $Log$
 */
package com.icorey.android.raytracer;

/**
 * Phong illumination model
 *
 * @author corey
 */
public class Phong extends IlluminationModel {

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
	public Phong(int ID, double ka, double kd, double ks, int ke, Texture texture) {
		super(ID, ka, kd, ks, ke, texture);
	}
	
	/* (non-Javadoc)
	 * @see com.android.raytracer.IlluminationModel#illuminate(com.android.raytracer.Vector, com.android.raytracer.Vector, com.android.raytracer.Vector)
	 */
	@Override
	public Vector illuminate(Vector intersection, Vector normal, Vector lightSource, Vector lightColor, Vector eye, double lit) {
		Vector v, r, color;
		double dot1, dot2;
		
		// get colors
		Vector ambient = super.texture.getAmbientColor(intersection.getX(), intersection.getZ());
		Vector diffuse = super.texture.getDiffuseColor(intersection.getX(), intersection.getZ());
		Vector specular = super.texture.getSpecularColor(intersection.getX(), intersection.getZ());
		
		// get ambient color
		color = ambient.scale(ka);
		
		// only apply diffuse/specular light if the point is lit
		if (lit > 0) {
			// get viewing vector
			v = eye.subtract(intersection);
			v.normalize();
			
			// get reflection vector
			dot1 = lightSource.dot(normal);
			r = normal.scale(2.0 * dot1).subtract(lightSource);
			r.normalize();
			
			dot2 = r.dot(v);
			
			// don't want negative dot products
			if (dot1 < 0) dot1 = 0;
			if (dot2 < 0) dot2 = 0;
			
			// apply Phong illumination equations
			color = diffuse.scale(kd * dot1).add(specular.scale(ks * Math.pow(dot2, ke))).multiply(lightColor.scale(lit)).add(color.scale(1 - Math.pow(dot2, ke)));
		}
		
		// cap color at 1 (because of Android)
		color.cap(1.0);
		
		return color;
	}	
}
