package com.icorey.android.raytracer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;

public class TracedActivity extends Activity {
    public static final String TAG = "TracedActivity";
    
    private ImageView iv;
    private Bitmap image;
    private String status;
    private boolean initialized;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        String dataFile = this.getIntent().getDataString();
        double execTime = this.getIntent().getDoubleExtra("execTime", 0);
        File folder, file;
        FileWriter fw;

    	// Change layout
    	setContentView(R.layout.image);
    	iv = (ImageView)findViewById(R.id.ImageView01);
    	
    	// Find out if we intialized this yet
    	initialized = false;
		if (savedInstanceState != null) {
			initialized = savedInstanceState.getBoolean("initialized");
		}
    	
		// Initialize the view
    	if (dataFile != null) {
	    	Log.d(TAG, "Opening file: " + dataFile);
	    	
	    	// Get bitmap
	    	image = BitmapFactory.decodeFile(dataFile);
	    	
	    	if (image == null) {
	    		Log.e(TAG, "Bitmap is null");
	    	} else {
	    		Log.d(TAG, "Bitmap width: " + image.getWidth() + ", height: " + image.getHeight());
	        	
	        	iv.setImageBitmap(image);
	        	
	        	Log.d(TAG, "Set bitmap");
	    	}
    	}
	    	
    	if (!initialized && execTime != 0) {
    		// Display execution time
        	status = String.format(getResources().getString(R.string.result_info), execTime);

    		Toast.makeText(this, status, Toast.LENGTH_LONG).show();
    		Log.i(TAG, status);
    		
    		// Write to a file as well
    		if (isExternalStorageWriteable()) {
	    		// Open file
	    		folder = getExternalFilesDir(null);
	    		folder.mkdirs();
	    		file = new File(folder, "performance.csv");
	    		try {
	    			// Write data
					fw = new FileWriter(file, true);
		    		fw.write(execTime + "\n");
		    		fw.close();
		    		Log.d(TAG, "Saved performance data to " + file.getAbsolutePath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
		}
    	
    	stopService(new Intent(this, TracingService.class));

    	initialized = true;
    }

    @Override
    protected void onStop() {
    	super.onStop();
    	
    	image.recycle();
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
    	outState.putBoolean("initialized", initialized);
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	// We must call through to the base implementation.
    	super.onCreateOptionsMenu(menu);
    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_menu, menu);

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
	    	case R.id.redraw:
	    		// Start menu activity
	    		Intent intent = new Intent(Intent.ACTION_PICK);
	    		intent.setClassName("com.icorey.android.raytracer",
	    				"com.icorey.android.raytracer.RayTracer");
	    		startActivity(intent);
	    		break;
	    	case R.id.save_image:
	    		// Save the current image
	    		saveImage();
	    		break;
	    	case R.id.display_info:
	    		// Show the execution time
	    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    		builder.setTitle("Performance")
	    		.setMessage(status)
	    		.create().show();
	    		break;
	    	default:
	    		return super.onOptionsItemSelected(item);
    	}
    	
    	return true;
    }
    
    private boolean isExternalStorageWriteable() {
    	boolean mExternalStorageWriteable = false;
    	String state = Environment.getExternalStorageState();

    	if (Environment.MEDIA_MOUNTED.equals(state)) {
    	    // We can read and write the media
    	    mExternalStorageWriteable = true;
    	} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
    	    // We can only read the media
    	    mExternalStorageWriteable = false;
    	} else {
    	    // Something else is wrong. It may be one of many other states, but all we need
    	    //  to know is we can neither read nor write
    	    mExternalStorageWriteable = false;
    	}
    	
    	return mExternalStorageWriteable;
    }
    
    private void saveImage() {
    	boolean imageSaved = false;
    	
    	if (isExternalStorageWriteable()) {
    		// External storage is writeable
	    	File path = Environment.getExternalStoragePublicDirectory(
	                Environment.DIRECTORY_PICTURES);
	        File file = new File(path, newFileName(".png"));
	        
	        if (file != null) {
		    	try {
		    		// Make sure the Pictures directory exists.
			    	path.mkdirs();
			    	file.createNewFile();
			    	if (file.canWrite()) {
				    	// Open stream to save image
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
						boolean cmprsd = image.compress(Bitmap.CompressFormat.PNG, 100, bos);
						bos.flush();
						bos.close();
						
						updateMediaScanner(file);
						
						// Image successfully saved
						if (cmprsd) {
							imageSaved = true;
				    		reportSaveSuccess(file);
						} else {
							Log.e(TAG, "Error compressing bitmap");
						}
			    	}
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
	        }
    	} 
    	
    	if (!imageSaved) {	
			// Image could not be saved
    		reportSaveError();
    	}
    }
    
    private void reportSaveSuccess(File file) {
    	// Format the results message.
		CharSequence fmt = getResources().getText(R.string.image_saved);
		String msg = String.format((String) fmt, file.getAbsolutePath());	
		// Display the dialog.
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Log.i(TAG, "Saved image to " + file.getAbsolutePath());
    }
    
    private void reportSaveError() {
        Log.e(TAG, "Error saving image");
		Toast toast = Toast.makeText(this,
				getResources().getText(R.string.save_error),
				Toast.LENGTH_SHORT);
		toast.show();
    }
    
    private void updateMediaScanner(File file) {
    	// Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this,
                new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.i("ExternalStorage", "Scanned " + path + ":");
                Log.i("ExternalStorage", "-> uri=" + uri);
            }
        });
    }
    
    private String newFileName(String extension) {
    	Calendar cal = Calendar.getInstance();
    	String name = cal.get(Calendar.YEAR) + "-" +
    		(cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "_" +
    		cal.get(Calendar.HOUR_OF_DAY) + "-" + cal.get(Calendar.MINUTE) + "-" +
    		cal.get(Calendar.SECOND) + "_" + cal.get(Calendar.MILLISECOND)
    		+ extension;
    	return name;
    }
}
