package com.leonidex.xmlreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.leonidex.db.Item;
import com.leonidex.metalmetal.Constants;
import com.leonidex.metalmetal.R;

import android.os.AsyncTask;

public class CounterCheckAsyncTask extends AsyncTask<Integer, Integer, Integer> implements Constants{

	private OnTaskCompleted listener;
	ArrayList<Item> newItems;

	public CounterCheckAsyncTask(OnTaskCompleted listener) {
		this.listener = listener;
	}
	
	@Override
	protected void onPreExecute() { 
		listener.onSectionStart(R.string.update_progress_diff_check);
		
		newItems = new ArrayList<Item>();
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		
//		// Wait for the db to be copied
//		try {
//		Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		int dbCount = params[0];

		int updatedCount = -1;

		try {
			HttpClient httpclient = new DefaultHttpClient(); // Create HTTP
																// Client
			HttpGet httpget = new HttpGet(
					"http://www.metalmetal.co.il/epcounter"); // Set the
			// action
			// you want
			// to do
			HttpResponse response = httpclient.execute(httpget); // Execute it
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent(); // Create an InputStream with
													// the response
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null)
				// Read line by line
				sb.append(line);

			String resString = sb.toString(); // Result is here

			updatedCount = Integer.parseInt(resString);
			is.close(); // Close the stream

		} catch (IOException e) {
			e.printStackTrace();
			return WEB_RESULT_CODE_IO_ERROR;
		}
		
		listener.onSectionStart(R.string.update_progress_downloading_new);
		if (dbCount < updatedCount) {
			int diff = updatedCount - dbCount;

			// If the difference isn't larger than what is on the short xml file
			// then update, if not then a toast to update the app will appear
//			if (diff <= 4) {
				// Read updated XML
				xmlParser parser = new xmlParser();
				
				try {
					parser.iterate();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Put it into DB
				newItems = parser.getItemsList();
				
				if(newItems != null) {
					return WEB_RESULT_CODE_SUCCESS;
				} else {
					return WEB_RESULT_CODE_IO_ERROR;
				}
//			} else {
//				return WEB_RESULT_CODE_TOO_LARGE_DIFF;
//			}
		} else {
			return WEB_RESULT_CODE_NO_NEED_TO_UPDATE;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		listener.onTaskCompleted(result, newItems);
	}
}
