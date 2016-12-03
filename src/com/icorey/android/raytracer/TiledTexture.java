/*
* TiledTexture.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * Class for objects with tiled textures
 *
 * @author corey
 */
public class TiledTexture extends Texture {
	// color components
	private final Vector ambient0, ambient1, diffuse0, diffuse1, specular0, specular1;
	
	// number of rows and columns on 
	private final int rows, cols;
	
	private final double tileWidth, tileHeight;
	
	private final double width, height;
	
	/**
	 * Constructor
	 *
	 * @param ambient0R first ambient red
	 * @param ambient0G first ambient green
	 * @param ambient0B first ambient blue
	 * @param ambient1R second ambient red
	 * @param ambient1G second ambient green
	 * @param ambient1B second ambient blue
	 * @param diffuse0R first diffuse red
	 * @param diffuse0G first diffuse green
	 * @param diffuse0B first diffuse blue
	 * @param diffuse1R second diffuse red
	 * @param diffuse1G second diffuse green
	 * @param diffuse1B second diffuse blue
	 * @param specular0R first specular red
	 * @param specular0G first specular green
	 * @param specular0B first specular blue
	 * @param specular1R second specular red
	 * @param specular1G second specular green
	 * @param specular1B second specular blue
	 */
	public TiledTexture(double ambient0R, double ambient0G, double ambient0B,
					   double ambient1R, double ambient1G, double ambient1B,
					   double diffuse0R, double diffuse0G, double diffuse0B,
					   double diffuse1R, double diffuse1G, double diffuse1B,
					   double specular0R, double specular0G, double specular0B,
					   double specular1R, double specular1G, double specular1B,
					   double width, double height) {
		ambient0 = new Vector(ambient0R, ambient0G, ambient0B);
		ambient1 = new Vector(ambient1R, ambient1G, ambient1B);
		diffuse0 = new Vector(diffuse0R, diffuse0G, diffuse0B);
		diffuse1 = new Vector(diffuse1R, diffuse1G, diffuse1B);
		specular0 = new Vector(specular0R, specular0G, specular0B);
		specular1 = new Vector(specular1R, specular1G, specular1B);
		rows = 16;
		cols = 31;
		tileWidth = rows / width;
		tileHeight = cols / height;
		this.width = width;
		this.height = height;
	}
	
	/* (non-Javadoc)
	 * @see com.android.raytracer.Texture#getAmbientColor(double, double)
	 */
	@Override
	public Vector getAmbientColor(double x, double y) {
        if ((int)((x + width) * tileWidth) % 2 != (int)((y + height) * tileHeight) % 2)
        	return ambient1;
                
        return ambient0;
	}
	
	/* (non-Javadoc)
	 * @see com.android.raytracer.Texture#getDiffuseColor(double, double)
	 */
	@Override
	public Vector getDiffuseColor(double x, double y) {
        if ((int)((x + width) * tileWidth) % 2 != (int)((y + height) * tileHeight) % 2)
        	return diffuse1;
                
        return diffuse0;
	}
	
	/* (non-Javadoc)
	 * @see com.android.raytracer.Texture#getSpecularColor(double, double)
	 */
	@Override
	public Vector getSpecularColor(double x, double y) {
        if ((int)((x + width) * tileWidth) % 2 != (int)((y + height) * tileHeight) % 2)
        	return specular1;
                
        return specular0;
	}
}