package com.icorey.android.raytracer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;

/**
 * Main class for the Ray Tracer.  Handles options and menus and starts the
 * ray tracer.
 *
 * @author corey
 */
public class RayTracer extends Activity {
    public static final String TAG = "RayTracer";
    
    // fields
    private CheckBox Supersampling, Reproduction;
    private RadioButton Phong, Phongblinn, Mirror, Dull, Ward, Reinhard;
    private EditText Depth, Rays, Jitter, Lmax, Width, Height, Workers;
    private GestureOverlayView gestures;
    private GestureLibrary mLibrary;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Load GestureLibrary
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!mLibrary.load()) {
            finish();
        }
    	
		showMenu();	// show the menu
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	// We must call through to the base implementation.
    	super.onCreateOptionsMenu(menu);
    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }
    
    /**
     * This hook is called whenever an item in your options menu is selected.
     * Derived classes should call through to the base class for it to
     * perform the default menu handling.  (True?)
     *
     * @param	item	The menu item that was selected.
     * @return	false   to have the normal processing happen.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
	    	case R.id.draw:
	    		// Ray trace now
	    		prep();
	    		break;
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    	
    	return true;
    }
    
    /**
     * Find fields and set default options
     *
     */
    private void initControls() {
    	//Point p = new Point();
    	Display display = getWindowManager().getDefaultDisplay();
    	//display.getSize(p);
    	DisplayMetrics dm = new DisplayMetrics();
    	display.getMetrics(dm);

    	Supersampling = (CheckBox)findViewById(R.id.supersampling);
    	Reproduction = (CheckBox)findViewById(R.id.tones);
    	Phong = (RadioButton)findViewById(R.id.phong);
    	Phong.setChecked(true);
    	Phongblinn = (RadioButton)findViewById(R.id.phongblinn);
    	Mirror = (RadioButton)findViewById(R.id.mirror);
    	Mirror.setChecked(true);
    	Dull = (RadioButton)findViewById(R.id.dull);
    	Ward = (RadioButton)findViewById(R.id.ward);
    	Ward.setChecked(true);
    	Reinhard = (RadioButton)findViewById(R.id.reinhard);
    	Depth = (EditText)findViewById(R.id.depth);
    	Rays = (EditText)findViewById(R.id.rays);
    	Jitter = (EditText)findViewById(R.id.jitter);
    	Lmax = (EditText)findViewById(R.id.lmax);
    	Width = (EditText)findViewById(R.id.width);
    	Height = (EditText)findViewById(R.id.height);
    	Workers = (EditText)findViewById(R.id.workers);
    	gestures = (GestureOverlayView) findViewById(R.id.gestures);
    	
    	// Preset height and width to that of the screen
    	//Width.setText(p.y + "");
    	//Height.setText(p.x + "");
    	if (dm.widthPixels < dm.heightPixels) {
    		// Portrait
        	Width.setText(dm.heightPixels + "");
        	Height.setText(dm.widthPixels + "");
    	} else {
    		// Landscape
        	Width.setText(dm.widthPixels + "");
        	Height.setText(dm.heightPixels + "");
    		
    	}
    	
    	// go to prep() function when the user swipes the screen
    	gestures.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
			public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
				ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

		        // We want at least one prediction
		        if (predictions.size() > 0) {
		            Prediction prediction = predictions.get(0);
		            // We want at least some confidence in the result
		            if (prediction.score > 1.0) {
		                // Ray trace
		                prep();
		            }
		        }
			}
		});
    	
//    	// go to prep() function when the button is pressed
//    	Raytrace.setOnClickListener(new Button.OnClickListener() {
//			public void onClick (View v) {
//				prep();
//			}
//		});
    }
    
    /**
     * Get selected options and run the ray tracer
     *
     */
    private void prep() {
    	Log.d(TAG, "button clicked");
    	
    	// declare options
    	boolean ss = false;
    	boolean pb = false;
    	boolean dull = false;
    	boolean tones = false;
    	boolean reinhard = false;
    	int depth, rays, lmax, width, height, workers;
    	double jitter;
    	
    	// read in options from the menu
    	if (Supersampling.isChecked()) ss = true;
    	if (Phongblinn.isChecked()) pb = true;
    	if (Dull.isChecked()) dull = true;
    	if (Reproduction.isChecked()) tones = true;
    	if (Reinhard.isChecked()) reinhard = true;
    	
    	try {
	    	depth = Integer.parseInt(Depth.getText().toString());
	    	rays = Integer.parseInt(Rays.getText().toString());
	    	lmax = Integer.parseInt(Lmax.getText().toString());
	    	width = Integer.parseInt(Width.getText().toString());
	    	height = Integer.parseInt(Height.getText().toString());
	    	workers = Integer.parseInt(Workers.getText().toString());
	    	jitter = Double.parseDouble(Jitter.getText().toString());
	    	
	    	if (width * height < 2500000) {
		    	// run the ray tracer
		    	runRayTracer(width, height, ss, pb, dull, tones, reinhard, depth,
		    			rays, lmax, jitter, workers);
	    	} else {
	    		// Error parsing input field
	    		Toast toast = Toast.makeText(this,
	    				getResources().getText(R.string.too_large),
	    				Toast.LENGTH_SHORT);
	    		toast.show();
	    	}
    	} catch (NumberFormatException e) {
    		// Error parsing input field
    		Toast toast = Toast.makeText(this,
    				getResources().getText(R.string.input_error),
    				Toast.LENGTH_SHORT);
    		toast.show();
    	}
    }
    
    /**
     * Display the menu (as defined in main.xml)
     *
     */
    private void showMenu() {
    	setContentView(R.layout.main);    	
    	initControls();
    }

    /**
     * Create a new world (start ray tracing) and get time and battery
     * measurements
     *
     * @param ss supersampling option
     * @param pb Phong-Blinn option
     * @param dull Dull reflections option
     * @param post tone reproduction option
     * @param reinhard Reinhard tone reproduction option
     * @param depth Recursion depth
     * @param rays Reflection rays
     * @param lmax max luminance
     * @param jitter Jitter distance
     */
    private void runRayTracer(int x, int y, boolean ss, boolean pb, boolean dull,
						boolean post, boolean reinhard, int depth, int rays, 
						int lmax, double jitter, int workers) {
    	// Run ray tracer
    	Intent intent = new Intent(this, TracingService.class);
    	intent.putExtra("x", x);
    	intent.putExtra("y", y);
    	intent.putExtra("ss", ss);
    	intent.putExtra("pb", pb);
    	intent.putExtra("dull", dull);
    	intent.putExtra("post", post);
    	intent.putExtra("reinhard", reinhard);
    	intent.putExtra("depth", depth);
    	intent.putExtra("rays", rays);
    	intent.putExtra("lmax", lmax);
    	intent.putExtra("jitter", jitter);
    	intent.putExtra("workers", workers);
		startService(intent);
    }
}