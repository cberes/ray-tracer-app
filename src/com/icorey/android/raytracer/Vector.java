/*
* Vector.java
*
* Version:
*     $Id$
*
* Revisions:
*     $Log$
*/
package com.icorey.android.raytracer;

import java.lang.Math;

import android.graphics.Color;

/**
 * Vector math class
 *
 * @author corey
 */
public class Vector {
	// coordinates
    private double x, y, z;
    
    // flag to keep track of if vectors are inside a volume
    private boolean inside;
    
    private final double maxColor = 255.0;
    
    /**
     * default constructor
     *
     */
    public Vector() {
    	this(0,0,0);
    }
    
    /**
     * Constructor
     *
     * @param x x coord
     * @param y y coord
     * @param z z coord
     */
    public Vector(double x, double y, double z) {
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	
    	inside = false; // initially outside all objects
    }
    
    public void copy(Vector other) {
    	x = other.x;
    	y = other.y;
    	z = other.z;
    }
    
    /**
     * Normalize a vector
     *
     */
    public void normalize() {
    	double m = getMagnitude();
    	if (m > 0) {
	    	x /= m;
	    	y /= m;
	    	z /= m;
    	}
    }
    
    /**
     * Return magnitude of a vector
     *
     * @return vector's magnitude
     */
    public double getMagnitude() {
    	return Math.sqrt(x * x + y * y + z * z);
    }
    
    /**
     * Return dot product of current vector and another
     *
     * @param other other vector
     * @return dot product
     */
    public double dot(Vector other) {
    	return (x * other.x + y * other.y + z * other.z);
    }
    
    /**
     * Return sum of this vector and another
     *
     * @param other other vector
     * @return sum
     */
    public Vector add(Vector other) {
    	return new Vector(x + other.x, y + other.y, z + other.z);
    }
    
    /**
     * Return difference of this vector and another
     *
     * @param other other vector
     * @return difference
     */
    public Vector subtract(Vector other) {
    	return new Vector(x - other.x, y - other.y, z - other.z);
    }
    
    /**
     * Scale a vector
     *
     * @param c scale factor
     * @return scaled vector
     */
    public Vector scale(double c) {
    	return new Vector(c * x, c * y, c * z);
    }
    
    /**
     * Multiply one vector by another
     *
     * @param other other vector
     * @return product
     */
    public Vector multiply(Vector other) {
    	return new Vector(x * other.x, y * other.y, z * other.z);
    }
    
    /**
     * Cap a vector's component's at a specified value
     *
     * @param max max value
     */
    public void cap(double max) {
    	if (x > max) x = max;
    	if (y > max) y = max;
    	if (z > max) z = max;
    }
    
    /**
     * set c coord
     *
     * @param x x coord
     */
    public void setX(double x) {
    	this.x = x;
    }
    
    /**
     * set y coord
     *
     * @param y y coord
     */
    public void setY(double y) {
    	this.y = y;
    }
    
    /**
     * set z coord
     *
     * @param z z coord
     */
    public void setZ(double z) {
    	this.z = z;
    }
    
    /**
     * set each coord at once
     *
     * @param x x coord
     * @param y y coord
     * @param z z coord
     */
    public void setAll(double x, double y, double z) {
    	this.x = x;
    	this.y = y;
    	this.z = z;
    }
    
    /**
     * Toggle a vector's inside flag
     *
     */
    public void toggle() {
    	inside = !inside;
    }
    
    /**
     * return x coord
     *
     * @return x coord
     */
    public double getX() {
    	return x;
    }
    
    /**
     * return y coord
     *
     * @return y coord
     */
    public double getY() {
    	return y;
    }
    
    /**
     * return z coord
     *
     * @return z coord
     */
    public double getZ() {
    	return z;
    }
    
    /**
     * return z coord
     *
     * @return z coord
     */
    public int getColor() {
    	return Color.rgb((int)(x * maxColor), (int)(y * maxColor), (int)(z * maxColor));
    }
    
    /**
     * Return true if the vector is inside an object; return false otherwise
     *
     * @return inside state
     */
    public boolean isInside() {
    	return inside;
    }
}
