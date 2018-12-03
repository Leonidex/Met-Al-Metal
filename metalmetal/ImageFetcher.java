package com.leonidex.metalmetal;

import java.io.InputStream;

import com.leonidex.db.Item;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageFetcher {
	
	ImageView imageView;
	Context inContext;
	Item item;
	
	int numOfTries;
	boolean success;
	boolean resize;
	
	public ImageFetcher(Context inContext, ImageView imageView) {
		this.imageView = imageView;
		this.inContext = inContext;
	}
	
	public void getImageLocation(String posterLink, boolean resize) {
		this.resize = resize;
		Item m = new Item();
		m.setPosterLink(posterLink);
		getImageLocation(m);
	}
	
	public void getImageLocation(String posterLink) {
		getImageLocation(posterLink, false);
	}
	
	public void getImageLocation(Item newItem) {
		numOfTries = 0;
		success = false;
		
		this.item = newItem;
		tryFetch();
	}
	
	private void tryFetch() {
		DownloadImageTask taskSmallImage = new DownloadImageTask(imageView);
		taskSmallImage.execute(item.getPosterLink());
	}
	
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;

	    public DownloadImageTask(ImageView bmImage) {
	        this.bmImage = bmImage;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon = BitmapFactory.decodeStream(in);
	            success = true;
	        } catch (Exception e) {
	            Log.e("DownloadImageTask", "Error: " + e.getMessage());
	            e.printStackTrace();
	            success = false;
	        }
	        
	        return mIcon;
	    }

	    protected void onPostExecute(Bitmap result) {
			
	    	if(success) {
	    		if(resize) {
		    	    double width = result.getWidth();
		    	    double height = result.getHeight();
		    	    double ratio = width/height;
		    	    int newWidth = 60;
		    	    int newHeight = (int) (newWidth/ratio);
		    	    Log.d("ImageFetcher", "old size: " + width + ", " + height);
		    	    Log.d("ImageFetcher", "ratio: " + ratio);
		    	    Log.d("ImageFetcher", "new size: " + newWidth + ", " + newHeight);
		    	    Bitmap resizedBitmap = Bitmap.createScaledBitmap(result, newWidth, newHeight, false);//Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, false);
	    			bmImage.setImageBitmap(resizedBitmap);
	    		}
	    		else {
	    			bmImage.setImageBitmap(result);
	    		}
	    	} else {
	    		if(numOfTries < 3) {
	    			tryFetch();
	    			numOfTries++;
	    		} else {
	    			Toast.makeText(inContext, "Couldn't load image", Toast.LENGTH_SHORT).show();
	    		}
	    	}
	    }
	}
}
