package com.leonidex.metalmetal;

import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leonidex.db.Item;
import com.leonidex.db.ItemsDBConstants;
import com.leonidex.db.ItemsHandler;
import com.leonidex.db.MyListAdapter;
import com.leonidex.xmlreader.OnTaskCompleted;
import com.leonidex.xmlreader.CounterCheckAsyncTask;

public class MainMenuActivity extends Activity implements Constants,
		ItemsDBConstants, OnTaskCompleted {

	FragmentManager fragManager;
	FragmentTransaction fragTransaction;

	ItemsHandler handler;
	MyListAdapter listAdapter;
	Cursor itemsCursor;

	boolean isInitialized;

	public MainMenuActivity() {
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.updating_screen);
		
		try {
			int versionName = getPackageManager()
				    .getPackageInfo(getPackageName(), 0).versionCode;
			Log.d("Version: ", ""+versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		ImageView loadingImage = (ImageView) findViewById(R.id.loading_image);		
		loadingImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotation));
		
		// Creates a new DB if there is none
		handler = new ItemsHandler(this);
		Log.d("MainMenu onCreate", "After handler assigned");
		
		checkForUpdates();
	}

	public void checkForUpdates() {
		// Updates DB if there are new items available
		int dbCount = -1;
		dbCount = handler.getCount();
		CounterCheckAsyncTask webAsync = new CounterCheckAsyncTask(this);

		if (dbCount != -1) {
			webAsync.execute(dbCount);
		} else {
			webAsync.execute(ASSET_DB_COUNT);
		}

		Log.d("dBcount", dbCount + "");
	}

	// Activates on every section start of the update async task
	@Override
	public void onSectionStart(final int messageId) {
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				String message = getString(messageId);
				TextView updateTextView = (TextView) findViewById(R.id.update_text_view);
				updateTextView.setText(message);
			}
		});
	}
	
	// Activates when the update async task that calls for items count completes
	@Override
	public void onTaskCompleted(int result, ArrayList<Item> newItems) {

		String text = "";

		switch (result) {
		case WEB_RESULT_CODE_NO_NEED_TO_UPDATE:
			// text = getString(R.string.update_no_need);
			break;

		case WEB_RESULT_CODE_SUCCESS:
			// Compare new items to the last present item
			String lastItemTitle = handler.getLastItemTitle();
			int howManyNewItems = 0;

			if (newItems != null) {
				for (Item item : newItems) {
					if (!lastItemTitle.equals(item.getTitle())) {
						howManyNewItems++;
					} else {
						break;
					}
				}
			}

			for (int i = howManyNewItems - 1; i >= 0; i--) {
				handler.addItem(newItems.get(i));
			}

			text = getString(R.string.update_success);
			break;

		case WEB_RESULT_CODE_IO_ERROR:
			text = getString(R.string.update_io_error);
			break;

		case WEB_RESULT_CODE_TOO_LARGE_DIFF:
			text = getString(R.string.update_too_large_diff);
			break;

		default:
			break;
		}

		if (text != "") {
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		}

		createInterface();
	}

	public void createInterface() {

		setContentView(R.layout.activity_main_menu);

		// Prevent re-initialization of main activity interface
		if (isInitialized) {
			return;
		}

		itemsCursor = handler.getAllItemsCursor();
		listAdapter = new MyListAdapter(itemsCursor, this);

		// Main menu

		// Newest item
		TextView newestItemTitle = (TextView) findViewById(R.id.newest_item_title);
		TextView newestItemDate = (TextView) findViewById(R.id.newest_item_date);
		// TextView newestItemDescription = (TextView)
		// findViewById(R.id.newest_item_description);
		ImageView newestItemPoster = (ImageView) findViewById(R.id.newest_item_poster);
		// TextView newestItemSetlist = (TextView)
		// findViewById(R.id.newest_item_setlist);

		Cursor newestItemCursor = (Cursor) listAdapter.getItem(0);
		final Item newestItem = new Item(newestItemCursor);

		newestItemTitle.setText(newestItem.getTitle());
		newestItemDate.setText(newestItem.getDate());
		// newestItemDescription.setText(newestItem.getDescription());
		// newestItemSetlist.setText(newestItem.getSetlist());

		// Setting the poster image
		ImageFetcher imgFetch = new ImageFetcher(this, newestItemPoster);
		imgFetch.getImageLocation(newestItem.getPosterLink());

		// Clicking on an item opens an ItemFragment for it
		RelativeLayout newestItemContainer = (RelativeLayout) findViewById(R.id.newest_item_container_layout);
		newestItemContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle outputBundle = new Bundle();

				outputBundle.putString("title", newestItem.getTitle());
				outputBundle.putString("date", newestItem.getDate());
				outputBundle.putString("description",
						newestItem.getDescription());
				outputBundle.putString("setlist", newestItem.getSetlist());
				outputBundle.putString("poster_link",
						newestItem.getPosterLink());
				outputBundle.putString("icast_link", newestItem.getIcastLink());
				outputBundle.putInt("position", 0);

				goToItem(outputBundle);
			}
		});

		// 2nd item
		TextView secondItemTitle = (TextView) findViewById(R.id.second_item_title);
		TextView secondItemDate = (TextView) findViewById(R.id.second_item_date);
		// TextView secondItemDescription = (TextView)
		// findViewById(R.id.second_item_description);
		ImageView secondItemPoster = (ImageView) findViewById(R.id.second_item_poster);

		Cursor secondItemCursor = (Cursor) listAdapter.getItem(1);
		final Item secondItem = new Item(secondItemCursor);

		secondItemTitle.setText(secondItem.getTitle());
		secondItemDate.setText(secondItem.getDate());
		// secondItemDescription.setText(secondItem.getDescription());

		imgFetch = new ImageFetcher(this, secondItemPoster);
		imgFetch.getImageLocation(secondItem.getPosterLink());

		// Clicking on an item opens an ItemFragment for it
		RelativeLayout secondItemContainer = (RelativeLayout) findViewById(R.id.second_item_container_layout);
		secondItemContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle outputBundle = new Bundle();

				outputBundle.putString("title", secondItem.getTitle());
				outputBundle.putString("date", secondItem.getDate());
				outputBundle.putString("description",
						secondItem.getDescription());
				outputBundle.putString("setlist", secondItem.getSetlist());
				outputBundle.putString("poster_link",
						secondItem.getPosterLink());
				outputBundle.putString("icast_link", secondItem.getIcastLink());
				outputBundle.putInt("position", 1);

				goToItem(outputBundle);
			}
		});

		// 3rd item
		TextView thirdItemTitle = (TextView) findViewById(R.id.third_item_title);
		TextView thirdItemDate = (TextView) findViewById(R.id.third_item_date);
		// TextView thirdItemDescription = (TextView)
		// findViewById(R.id.third_item_description);
		ImageView thirdItemPoster = (ImageView) findViewById(R.id.third_item_poster);

		Cursor thirdItemCursor = (Cursor) listAdapter.getItem(2);
		final Item thirdItem = new Item(thirdItemCursor);

		thirdItemTitle.setText(thirdItem.getTitle());
		thirdItemDate.setText(thirdItem.getDate());
		// thirdItemDescription.setText(thirdItem.getDescription());
		imgFetch = new ImageFetcher(this, thirdItemPoster);
		imgFetch.getImageLocation(thirdItem);

		// Clicking on an item opens an ItemFragment for it
		RelativeLayout thirdItemContainer = (RelativeLayout) findViewById(R.id.third_item_container_layout);
		thirdItemContainer.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Bundle outputBundle = new Bundle();

				outputBundle.putString("title", thirdItem.getTitle());
				outputBundle.putString("date", thirdItem.getDate());
				outputBundle.putString("description",
						thirdItem.getDescription());
				outputBundle.putString("setlist", thirdItem.getSetlist());
				outputBundle.putString("poster_link", thirdItem.getPosterLink());
				outputBundle.putString("icast_link", thirdItem.getIcastLink());
				outputBundle.putInt("position", 2);

				goToItem(outputBundle);
			}
		});

		// // TODO: remove! V
		// Intent intent = new Intent(this, ListMenuActivity.class);
		//
		// getActivity().startActivity(intent);
		// // TODO: remove! ^

		// ListMenu button
		Button listMenuButton = (Button) findViewById(R.id.list_menu_button);
		listMenuButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				goToList();
			}
		});

		isInitialized = true;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void goToItem(Bundle outputBundle) {

		Intent intent = new Intent(this, ItemActivity.class);
		intent.putExtras(outputBundle);

		startActivityForResult(intent, REQUESTCODE_ITEM_ACTIVITY);
	}

	public void goToList() {
		Intent intent = new Intent(getApplicationContext(),
				ListMenuActivity.class);

		startActivity(intent);
	}

	public void setRowCount(int count) {

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("row_count", count);

		// Commit the edits!
		editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
			case R.id.action_list:
				goToList();
				return true;
				
			case R.id.action_about:
				AboutDialog dialog = new AboutDialog();
				dialog.showAboutDialog(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		Log.d("MainActivity", "onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUESTCODE_ITEM_ACTIVITY && resultCode == RESULT_OK) {
			if (!data.getBooleanExtra("go_to_main", false)) {
				goToList();
			}
		}
	}

	/*
	 * Catch back button events (non-Javadoc)
	 * 
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	// public boolean dispatchKeyEvent(KeyEvent event) {
	// int backCount = getFragmentManager().getBackStackEntryCount();
	// int action = event.getAction();
	// int keyCode = event.getKeyCode();
	//
	// FragmentManager fm = getFragmentManager();
	//
	// switch (event.getKeyCode()) {
	// case KeyEvent.KEYCODE_BACK:
	// if (action == KeyEvent.ACTION_DOWN && backCount == 0) {
	// // Exit App
	// onExitNotify();
	// } else if (action == KeyEvent.ACTION_DOWN){
	// // Return to MenuFragment
	// Log.d("dispatchKeyEvent", "backStackCount: " + backCount);
	// fm.popBackStack();
	// }
	// return true;
	//
	// default:
	// return super.dispatchKeyEvent(event);
	// }
	// }

	// private Toast toast;
	// private long lastBackPressTime = 0;

	// Notify user to click the back button again
	// private void onExitNotify() {
	// if (this.lastBackPressTime < System.currentTimeMillis() - 8000) {
	// toast = Toast.makeText(this, "Press back again to close this app", 8000);
	// toast.show();
	// this.lastBackPressTime = System.currentTimeMillis();
	// } else {
	// if (toast != null) {
	// toast.cancel();
	// }
	// super.onBackPressed();
	// }
	// }
}
