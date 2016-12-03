package com.icorey.android.raytracer;

/*
 * Ward.java
 *
 * Version:
 *     $Id$
 *
 * Revisions:
 *     $Log$
 */
/**
 * Ward tone operator
 *
 * @author corey
 */
public class Ward extends ToneOperator {

	/**
	 * Constructor
	 *
	 * @param Lmax max luminance
	 * @param canvas canvas to draw to
	 */
	public Ward(int Lmax) {
		super(Lmax);
	}

	/* (non-Javadoc)
	 * @see com.android.raytracer.ToneOperator#drawImage()
	 */
	@Override
	public int[] drawImage() {
		int[] colors = new int[(World.right - World.left) * (World.top - World.bottom)];
		double logave, term2, term4, sf;
		
		Vector c = new Vector();
		
		// average of log luminance
		logave = Math.exp(logsum / (World.right * World.top));
		
		// get the scale factor
		term2 = Math.pow(Ldmax * 0.5, 0.4);
		term4 = Math.pow(logave, 0.4);
		sf = Math.pow((1.219 + term2) / (1.219 + term4), 2.5);
		
		// for each pixel
		if (image != null) {
			for (int i = World.left; i < World.right; ++i) {
				for (int j = World.bottom; j < World.top; ++j) {
					// scale the color by the scale factor and map it to Ldmax
					c = image[i][j].scale(sf).scale(LdmaxInv);
					c.cap(1.0);
					
					// get the new color
					colors[i + j * (World.right - World.left)] = c.getColor();
				}
			}
		}
		
		image = null;
		return colors;
	}

}
