/*
* World.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * [Insert class description here]
 *
 * @author corey
 */
public class World extends Observable {
	// tone operator
	private ToneOperator op;
	
	// list of objects in scene
	private ArrayList<Object> objects;
	
	// list of lamps in scene (currently only one is supported)
	private ArrayList<Lamp> lamps;
	
	// Sky is blue
	private final Vector sky;
	
	// camera
	private Camera camera;
	
	// options
	private boolean supersample, dull, post;
	
	// refractive index, reflection ray jitter, 1/jitter
	private double n, jitter, jinv;
	
	// recursion depth, reflection rays, max luminance
	private int depth, rays;
	
	// projection boundaries, object ID counter
	public static int left, right, top, bottom, near, far, ID;
	
	// Array of pixels
	private int[] world;
	
	// Multiple threads
	public static final int STRIP_SIZE = 5;
	private int numWorkers;
	private int onePercentOfRows;
	
	// Dummy vectors
	private Vector v1, v2;
	private final String TAG = "RayTracer";
	
	/**
	 * Constructor
	 *
	 * @param x width of scene
	 * @param y height of scene
	 * @param canvas canvas to draw to
	 * @param ss supersample?
	 * @param pb Phong-Blinn?
	 * @param dull dull reflections?
	 * @param post post processing?
	 * @param reinhard Reinhard tone reproduction?
	 * @param depth recursion depth?
	 * @param rays reflection rays?
	 * @param lmax max luminance?
	 * @param jitter reflection ray jitter?
	 */
	public World(int x, int y, boolean ss, boolean pb,
			boolean dull, boolean post, boolean reinhard, int depth,
			int rays, int lmax, double jitter, int numWorkers) {
		// Bitmap
		world = null;
		
		if (post) {
			// Define tone operator if post-processing is enabled
			if (reinhard) op = new Reinhard(lmax);
			else op = new Ward(lmax);
		} else {
			world = new int[x * y];
		}
		
		// ID
		ID = -1;
		
		// Number of workers (need to know to divide work
		this.numWorkers = numWorkers;
		onePercentOfRows = (int) (0.01 * x);
		if (onePercentOfRows == 0) {
			onePercentOfRows = 1;
		}
		
		// options
		supersample = ss;
		this.dull = dull;
		this.post = post;
		this.depth = depth;
		this.rays = rays;
		this.jitter = jitter;
		
		// clipping planes
		left = 0;
		right = x;
		bottom = 0;
		top = y;
		near = 100;
		far = 1100;
		
		// n
		n = 1.0;
		
		// Sky
		sky = new Vector(0.05, 0.66, 0.82);
		
		objects = new ArrayList<Object>();
		lamps = new ArrayList<Lamp>();
		
		// lights
		// one above and in front of the scene
		Lamp l = new Lamp(0.688 * right, 2.5 * top, -1200.0, 1,1,1);
		lamps.add(l);
		
		//textures
		Texture t1 = new FlatTexture(0.7,0.7,0.7, 0.7,0.7,0.7, 1,1,1);
		Texture t2 = new FlatTexture(1,1,1, 1,1,1, 1,1,1);
		Texture t3 = new TiledTexture(1,0,0, 1,1,0, 1,0,0, 1,1,0, 1,1,1, 1,1,1, (int)(2.09 * right), 3200);
		
		//objects
		// front, transmissive sphere
		Sphere s1 = new Sphere(0.525 * right, 0.533 * top, 50.0, 0.233 * top,
							   0.075, 0.075, 0.6, 40, 0.01, 0.85, 0.95, t1, pb);
		// back, reflective sphere
		Sphere s2 = new Sphere(0.275 * right, 0.4 * top, 350.0, 0.283 * top,
							   0.15, 0.25, 1.0, 20, 0.75, 0, 0, t2, pb);
		// floor
		Floor f = new Floor(0,1,0, 0, -0.033 * top, 0,
						    3000.0, -200.0, -1.00 * right, 1.09 * right,
						    0.25, 0.65, 0.1, 2, 0, 0, 0, t3, pb);
		// add the objects
		objects.add(s1); objects.add(s2); objects.add(f);
		
		// camera
		// centered, looking straight at the scene
		camera = Camera.getInstance();
		camera.setEye(0.5 * right, 0.5 * top, -500.0);
		camera.setLookAt(0.5 * right, 0.5 * top, 0.0);
		
		// find some values		
		jinv = 1.0 / rays;
		
		v1 = new Vector();
		v2 = new Vector();
	}
	
	/**
	 * Return a new ID for an object
	 *
	 * @return ID
	 */
	public static int newID() {
		ID++;
		return(ID);
	}
	
	public void observe(Observer observer) {
		addObserver(observer);
	}
	
	private double isObjectLit(Vector origin, Vector direction, int id) {
		// Percentage of light reaching object
		double lit = 1;
		
		// find shadow status
		// check if the ray intersects any objects (other than the source object)
		for (Object o : objects) {
			if (o.getID() != id && o.intersect(origin, direction, v1, v2) < Double.MAX_VALUE) {
				if (o.getKT() <= 0) {
					// Object is opaque; no light reaches the point
					lit = 0;
					break;
				} else {
					// Reduce the amount of light reaching the point
					lit -= 1 - o.kt * o.kt;
				}
			}
		}
		
		return lit;
	}
	
	public Bitmap getBitmap(Bitmap.Config config) {
		return Bitmap.createBitmap(world, right - left, top - bottom, config);
	}
	
	/**
	 * Spawn a ray and get the resulting color
	 *
	 * @param deep current tracing depth
	 * @param object source of ray
	 * @param origin ray origin
	 * @param direction ray direction
	 * @return
	 */
	private Vector spawn(int deep, int object, Vector origin, Vector direction) {
		// default reflection/transmission coefficients
		double dot1, ni, nt, nit, dis;
		double kr = 0;
		double kt = 0;
		double q = jitter * 0.5;
		double distance = Double.MAX_VALUE;
		double newDistance;
		int nearestObject = -1;
		
		Vector color = new Vector();
		
		Vector lc = sky; 			// local color (default is sky blue)
		Vector rc = new Vector();	// reflected color
		Vector tc = new Vector();	// transmitted color
		
		// Intersection and normal vector for intersecting objects
		Vector intersection = new Vector();
		Vector normal = new Vector();
		Vector newIntersection = new Vector();
		Vector newNormal = new Vector();
		
		// Get lamp
		Lamp l = lamps.get(0);
		
		// Vector to light source
		Vector source;
		Vector lightColor = l.getColor();
		
		// Find the closest intersecting object (if there is one)
		for (Object o : objects) {
			// Ignore the source object
			if (o.getID() != object) {
				// Find (sort-of) distance to object
				newDistance = o.intersect(origin, direction, newIntersection, newNormal);
				if (newDistance < distance) {
					// Found a better distance
					intersection.copy(newIntersection);
					normal.copy(newNormal);
					distance = newDistance;
					nearestObject = o.getID();
				}
			}
		}
		
		// The ray intersects an object, so call its illuminate() method
		if (nearestObject > -1) {
			// Closest intersecting object
			Object o = objects.get(nearestObject);
			
			// Find vector to light source
			source = l.getPosition().subtract(intersection);
			source.normalize();
			
			// Get the local color
			lc = o.illuminate(intersection, normal, source, lightColor, isObjectLit(intersection, source, o.getID()));
			
			// get coefficients
			kr = o.getKR();
			kt = o.getKT();
			
			// get reflected color
			if (deep < depth && kr > 0) {
				if (dull && rays > 0 && jitter > 0) {
					// dull reflections are enabled
					Vector ct;
					
					dot1 = direction.dot(normal);
				
					// spawn many rays
					for (int k = 0; k < rays; ++k) {
						// jitter vector
						Vector j = new Vector(-q + jitter * Math.random(),
												-q + jitter * Math.random(),
												-q + jitter * Math.random());
						// get randomly offset reflection ray
						Vector reflection = direction.subtract(
									normal.scale(2.0 * dot1)).add(j);
						reflection.normalize();
						
						// get reflected color and add it to the sum
						ct = spawn(deep + 1, o.getID(), intersection, reflection);
						rc = rc.add(ct);
					}
					
					// find average reflected color
					rc = rc.scale(jinv);
				} else {
					// mirror reflections
					// get reflection vector
					Vector reflection = new Vector();
					reflection = direction.subtract(normal.scale(2.0 * direction.dot(normal)));
					reflection.normalize();
					
					// get reflected color
					rc = spawn(deep + 1, o.getID(), intersection, reflection);
				}
			}
			
			// get transmitted color
			if (deep < depth && kt > 0) {					
				// get refractive indeces depending on ray state
				if (direction.isInside()) {
					ni = o.getN();
					nt = n;
				} else {
					nt = o.getN();
					ni = n;
				}
				
				// find refractive index ratio
				nit = ni / nt;
				
				dot1 = -direction.dot(normal);
				
				// discriminant
				dis = 1.0 + (nit * nit * (dot1 * dot1 - 1.0));
				
				if (dis > 0) {
					// find transmitted vector like normal
					dis = nit * dot1 - Math.sqrt(dis);
					
					// find transmitted vector
					Vector transmission = new Vector();
					transmission = direction.scale(nit).add(normal.scale(dis));
					if (!direction.isInside()) transmission.toggle();
					
					// find transmitted color
					tc = spawn(deep + 1, o.getID(), intersection, transmission);
				} else {
					// discriminant < 0, total internal reflection
					tc = rc;
				}
			}
		}
		
		// get total color and cap at 1
		color = lc.add(rc.scale(kr)).add(tc.scale(kt));
		color.cap(1.0);
		
		return color;
	}
	
	private int nextX(int x) {
		int newX = x + 1;
		
		if (newX % STRIP_SIZE == 0) {
			newX += (numWorkers - 1) * STRIP_SIZE;
		}
		
		return newX;
	}
	
	private int firstX(int id) {
		return id * STRIP_SIZE + left;
	}

	/**
	 * Trace a scene by spawning rays
	 *
	 * @param canvas canvas to draw to (only used if tone reproduction is
	 * disabled)
	 */
	public void shadeCycles(int id) {
		Vector cv;
		Vector[] cx = new Vector[4];
		Vector d = new Vector();
		Vector eye = camera.getEye();
		
		// for each pixel in the World, send a ray (or multiple rays)
		for (int i = firstX(id); i < right; i = nextX(i)) {
//			Log.d(TAG, "Worker " + id + " starting row " + i + "/" + (right - left));
			
			// Check progress
			if (id == 0 && i % onePercentOfRows == 0) {
				// Notify observers
				setChanged();
				notifyObservers((int)((double)i / right * 100));
			}
			
			for (int j = bottom; j < top; ++j) {
				if (supersample) {
					// supersampling is enabled
					// get multiple direction rays and trace them
					// find the average color
					d.setAll(i + 0.6 - eye.getX(), top - j + 0.8 - eye.getY(), near - eye.getZ());
					d.normalize();
					cx[0] = spawn(1, -1, eye, d);
					
					d.setAll(i + 0.2 - eye.getX(), top - j + 0.6 - eye.getY(), near - eye.getZ());
					d.normalize();
					cx[1] = spawn(1, -1, eye, d);
					
					d.setAll(i + 0.4 - eye.getX(), top - j + 0.2 - eye.getY(), near - eye.getZ());
					d.normalize();
					cx[2] = spawn(1, -1, eye, d);
					
					d.setAll(i + 0.8 - eye.getX(), top - j + 0.4 - eye.getY(), near - eye.getZ());
					d.normalize();
					cx[3] = spawn(1, -1, eye, d);
					
					cv = new Vector();

					// add up the colors
			        for (int k = 0; k < 4; ++k) {
		                cv = cv.add(cx[k]);
			        }
			        
			        // find the average color
			        cv = cv.scale(0.25);
				} else {
					// find the direction ray
					d.setAll(i - eye.getX(), top - j - eye.getY(), near - eye.getZ());
					d.normalize();
					
					// get the color
					cv = spawn(1, -1, eye, d);
				}
				
				cv.cap(1.0);	// cap at 1
				
				if (post) {
					// tone reproduction enabled
					// add the color to the image
					op.add(i, j, cv);
				} else {
					// tone reproduction disabled
					// draw the pixel
					world[i + j * (World.right - World.left)] = cv.getColor();
				}
			}	
		}
	}

	/**
	 * Trace a scene by spawning rays
	 *
	 * @param canvas canvas to draw to (only used if tone reproduction is
	 * disabled)
	 */
	public void shade() {
		Vector cv;
		Vector[] cx = new Vector[4];
		Vector d = new Vector();
		Vector eye = camera.getEye();
		
		// for each pixel in the World, send a ray (or multiple rays)
		for (int i = left; i < right; ++i) {
//			Log.d(TAG, "Starting row " + i + "/" + (right - left));
			
			// Check progress
			if (i % onePercentOfRows == 0) {
				// Notify observers
				setChanged();
				notifyObservers((int)((double)i / right * 100));
			}
			
			for (int j = bottom; j < top; ++j) {
				if (supersample) {
					// supersampling is enabled
					// get multiple direction rays and trace them
					// find the average color
					d.setAll(i + 0.6 - eye.getX(), top - j + 0.8 - eye.getY(), near - eye.getZ());
					d.normalize();
					cx[0] = spawn(1, -1, eye, d);
					
					d.setAll(i + 0.2 - eye.getX(), top - j + 0.6 - eye.getY(), near - eye.getZ());
					d.normalize();
					cx[1] = spawn(1, -1, eye, d);
					
					d.setAll(i + 0.4 - eye.getX(), top - j + 0.2 - eye.getY(), near - eye.getZ());
					d.normalize();
					cx[2] = spawn(1, -1, eye, d);
					
					d.setAll(i + 0.8 - eye.getX(), top - j + 0.4 - eye.getY(), near - eye.getZ());
					d.normalize();
					cx[3] = spawn(1, -1, eye, d);
					
					cv = new Vector();

					// add up the colors
					for (int k = 0; k < 4; ++k) {
				        cv = cv.add(cx[k]);
					}
					
					// find the average color
					cv = cv.scale(0.25);
				} else {
					// find the direction ray
					d.setAll(i - eye.getX(), top - j - eye.getY(), near - eye.getZ());
					d.normalize();
					
					// get the color
					cv = spawn(1, -1, eye, d);
				}
				
				cv.cap(1.0);	// cap at 1
				
				if (post) {
					// tone reproduction enabled
					// add the color to the image
					op.add(i, j, cv);
				} else {
					// tone reproduction disabled
					// draw the pixel
					world[i + j * (World.right - World.left)] = cv.getColor();
				}
			}	
		}
	}
	
	public void finalize() {
		if (post) {
			// Get scene colors if tone reproduction is enabled
			world = op.drawImage();
		}
	}
}