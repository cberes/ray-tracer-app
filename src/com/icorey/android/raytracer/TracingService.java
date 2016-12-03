/**
 * 
 */
package com.icorey.android.raytracer;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * @author corey
 *
 */
public class TracingService extends Service {
	private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    
    // Initial notification, which we will periodically update
    private Notification notification;
    
    private TracingTask tt;
	
	private final String TAG = "RayTracer";

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        TracingService getService() {
            return TracingService.this;
        }
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        if (tt != null) {
        	tt.ts = this;
        }

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        
        // Notify the user
        Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Received start id " + startId + ": " + intent);
        
        // Get variables
        int x = intent.getIntExtra("x", 800);
        int y = intent.getIntExtra("y", 600);
        boolean ss = intent.getBooleanExtra("ss", false);
        boolean pb = intent.getBooleanExtra("pb", false);
        boolean dull = intent.getBooleanExtra("dull", false);
        boolean post = intent.getBooleanExtra("post", false);
        boolean reinhard = intent.getBooleanExtra("reinhard", false);
        int depth = intent.getIntExtra("depth", 5);
        int rays = intent.getIntExtra("rays", 25);
        int lmax = intent.getIntExtra("lmax", 1000);
        double jitter = intent.getDoubleExtra("jitter", 0.25);
        int workers = intent.getIntExtra("workers", 1);
        
        // Start ray tracing
        tt = new TracingTask(this, workers, x, y, ss, pb, dull, post, reinhard, depth, rays, lmax, jitter);
        tt.execute((Void[])null);
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);
        
        if (tt != null) {
        	tt.ts = null;
        }
    }

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void setProgress(int p) {
		notification.contentView.setTextViewText(R.id.text, p + "%");
		notification.contentView.setProgressBar(R.id.progress, 100, p, false);
		mNM.notify(NOTIFICATION, notification);
	}

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);

        // Set the icon, scrolling text and timestamp
        notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, RayTracer.class), 0);

//        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
//                       text, contentIntent);
        
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notify);
        contentView.setImageViewResource(R.id.image, R.drawable.icon);
        contentView.setTextViewText(R.id.title, "Ray Tracer");
        contentView.setTextViewText(R.id.text, "0%");
		contentView.setProgressBar(R.id.progress, 100, 0, false);
        
        notification.contentView = contentView;
        notification.contentIntent = contentIntent;
        
        // Event will remain until we remove it
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        // Send the notification.
//        mNM.notify(NOTIFICATION, notification);
        startForeground(NOTIFICATION, notification);
    }

    /**
     * Show a notification while this service is running.
     */
    public void showFinalNotification(Uri fileUri, String mimeType, double time) {
    	// Remove current notification and remove the service from the foreground
    	stopForeground(true);
    	
        // In this sample, we'll use the same text for the ticker and the expanded notification
        //CharSequence text = getText(R.string.local_service_stopped);
        String text = String.format(getResources().getString(R.string.local_service_stopped), time);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.icon, text,
                System.currentTimeMillis());
        
        // Create Intent
        Intent intent = new Intent(this, TracedActivity.class);
		intent.setDataAndType(fileUri, mimeType);
		intent.putExtra("execTime", time);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.local_service_label),
                       text, contentIntent);
        
        // Event will be canceled when the user selects it
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        // Vibrate a little
        notification.defaults = Notification.DEFAULT_VIBRATE;
        
        // LED, too. Why not?
        notification.ledARGB = 0xffffff00;
        notification.ledOnMS = 250;
        notification.ledOffMS = 1000;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }
    
    static class TracingTask extends AsyncTask<Void, Integer, Bitmap> implements Observer {
    	// Parent class
    	private TracingService ts;

        // Ray tracing World
    	private World world;
    	
    	// Number of workers
    	private int numWorkers;
    	
    	// Execution time
    	private double time;
    	
    	// Logging tag
    	private final String TAG;
    	
    	public TracingTask(TracingService ts, int numWorkers, int x, int y,
    			boolean ss, boolean pb, boolean dull, boolean post,
    			boolean reinhard, int depth, int rays, int lmax, double jitter) {
    		this.ts = ts;
    		this.numWorkers = numWorkers;

    		if (numWorkers * World.STRIP_SIZE > x) {
    			// Reduce number of workers
    			this.numWorkers = x / World.STRIP_SIZE;
    		}
    		
    		// Create world
    		world = new World(x, y, ss, pb, dull, post, reinhard, depth, rays, lmax, jitter, this.numWorkers);
    		world.observe(this);
    		
    		// Save tag
    		if (ts != null) {
    			TAG = ts.TAG;
    		} else {
    			TAG = "TracingService";
    		}
    	}

		@Override
		protected Bitmap doInBackground(Void... params) {
			long start, stop;
			
			// log some message and begin tracing scene
			Log.i(TAG, "tracing started");
			// get initial time
	    	start = SystemClock.elapsedRealtime();
	    	
			// Ray trace the scene
			if (numWorkers < 2) {
				// Single thread
				world.shade();
			} else {
				// Create thread array for ray tracing
				Thread cwt[] = new Thread[numWorkers];
				
				// Create and start the threads
				for (int i = 0; i < cwt.length; ++i) {
					cwt[i] = new Thread(new CyclesWorker(i));
					cwt[i].setPriority(Thread.MAX_PRIORITY);
					Log.d(TAG, "Created Thread with id " + cwt[i].getId());
					cwt[i].start();
				}
				
				// Wait until threads finish
				try {
					for (int i = 0; i < cwt.length; ++i) {
						cwt[i].join();
						Log.d(TAG, "Thread with id " + cwt[i].getId() + " exited");
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			world.finalize();
			
			// get final time
	    	stop = SystemClock.elapsedRealtime();
			time = (double)(stop - start) / 1000;
			
			Log.i(TAG, "tracing finished in " + time + " s");
			
			return world.getBitmap(Bitmap.Config.ARGB_8888);
		}
		
		/** The system calls this to perform work in the UI thread and delivers
	     * the result from doInBackground() */
		@Override
		protected void onPostExecute(Bitmap result) {
			Uri.Builder ub;
			BufferedOutputStream bos;
			String filename = "traced.png";
			String path = "";
			boolean compressed;
			
			if (ts != null) {
				path = ts.getFilesDir().getPath() + "/" + filename;
			
				try {
					// Save the render
					bos = new BufferedOutputStream(ts.openFileOutput(filename, Context.MODE_PRIVATE));
					compressed = result.compress(Bitmap.CompressFormat.PNG, 100, bos);
					if (!compressed) {
						Log.e(TAG, "Error compressing bitmap");
					}
					bos.flush();
					bos.close();
					result.recycle();
				} catch (FileNotFoundException e) {
					Log.e(TAG, e.getMessage());
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
				
				// Build the URI to the image
				ub = new Uri.Builder();
				ub.path(path);
				
				// Notify the user that we're done
				ts.showFinalNotification(ub.build(), "image/png", time);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			if (ts != null) {
				ts.setProgress(progress[0]);
			}
		}

		public void update(Observable arg0, java.lang.Object arg1) {
			publishProgress((Integer)arg1);
		}

		class CyclesWorker implements Runnable {
			private int id;
			
			public CyclesWorker(int id) {
				this.id = id;
			}
			
			public void run() {
				world.shadeCycles(id);
			}
		}
    }
}
