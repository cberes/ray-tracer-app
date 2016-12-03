/*
* FlatTexture.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * Class for objects with one color
 *
 * @author corey
 */
public class FlatTexture extends Texture {
	// color components
	private Vector ambient, diffuse, specular;
	
	/**
	 * Constructor
	 *
	 * @param ambientR ambient red
	 * @param ambientG ambient green
	 * @param ambientB ambient blue
	 * @param diffuseR diffuse red
	 * @param diffuseG diffuse green
	 * @param diffuseB diffuse blue
	 * @param specularR specular red
	 * @param specularG specular green
	 * @param specularB specular blue
	 */
	public FlatTexture(double ambientR, double ambientG, double ambientB,
					   double diffuseR, double diffuseG, double diffuseB,
					   double specularR, double specularG, double specularB) {
		ambient = new Vector(ambientR, ambientG, ambientB);
		diffuse = new Vector(diffuseR, diffuseG, diffuseB);
		specular = new Vector(specularR, specularG, specularB);
		
	}
	
	/* (non-Javadoc)
	 * @see com.android.raytracer.Texture#getAmbientColor(double, double)
	 */
	@Override
	public Vector getAmbientColor(double x, double y) {
		return ambient;
	}
	
	/* (non-Javadoc)
	 * @see com.android.raytracer.Texture#getDiffuseColor(double, double)
	 */
	@Override
	public Vector getDiffuseColor(double x, double y) {
		return diffuse;
	}
	
	/* (non-Javadoc)
	 * @see com.android.raytracer.Texture#getSpecularColor(double, double)
	 */
	@Override
	public Vector getSpecularColor(double x, double y) {
		return specular;
	}
}
