/*
* Reinhard.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

/**
 * Reinhard tone operator
 *
 * @author corey
 */
public class Reinhard extends ToneOperator {

	/**
	 * Constructor
	 *
	 * @param Lmax max luminance
	 * @param canvas canvas to draw to
	 */
	public Reinhard(int Lmax) {
		super(Lmax);
	}

	/* (non-Javadoc)
	 * @see com.android.raytracer.ToneOperator#drawImage()
	 */
	@Override
	public int[] drawImage() {
		int[] colors = new int[(World.right - World.left) * (World.top - World.bottom)];
		double logave, coeff;
		Vector s, r;
		
		// get log average of luminance
		logave = Math.exp(logsum / (World.right * World.top));
		
		// coefficient used to get scale factor
		coeff = 0.18 / logave;
		
		s = new Vector();
		r = new Vector();
		
		// for each point...
		if (image != null) {
			for (int i = World.left; i < World.right; ++i) {
				for (int j = World.bottom; j < World.top; ++j) {
					// get new color (r)
					s = image[i][j].scale(coeff);
					r.setAll(s.getX() / (1 + s.getX()), s.getY() / (1 + s.getY()), s.getZ() / (1 + s.getZ()));
					r.cap(1.0);
					
					// get new color
					colors[i + j * (World.right - World.left)] = r.getColor();
				}
			}
		}
		
		image = null;
		return colors;
	}

}
