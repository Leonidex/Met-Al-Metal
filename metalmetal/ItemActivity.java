package com.leonidex.metalmetal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.leonidex.mediaplayer.ServicePlayer;
import com.leonidex.mediaplayer.ServicePlayer.LocalBinder;

/**
 * A fragment containing a list view of the items.
 * Basically a menu for choosing items.
 */
public class ItemActivity extends Activity implements OnSeekBarChangeListener {

	static final String STATE_PLAYING = "playerState";
	private boolean isPlaying;
	
	// Activity views
	private TextView titleTextView;
	private TextView dateTextView;
	private TextView descriptionTextView;
	private TextView setlistTextView;
	private ImageView posterImageView;
	
	// MediaPlayerView variables
	private ImageButton startServiceButton;
	private ImageButton stopServiceButton;
	private TextView counterTextView;
	private ProgressBar progressBar;
	
	String mediaMaxString = "";
	
	// Communication with the player service
	private ServicePlayer mService;
	private boolean mBound;
	
	// Variable for destroying activity on service disconnect
//	private boolean destroyWasCalled;
	
	// Buffer broadcast variables
	boolean mBufferBroadcastIsRegistered;
	
	// SeekBar variables
	private SeekBar seekBar;
	private int seekMax;
	private static boolean songEnded = false;
	boolean broadcastIsRegistered;
	
	// Declare broadcast action and intent
	public static final String BROADCAST_SEEKBAR = "com.leonidex.metalmetal.sendseekbar";
	Intent sendSeekbarIntent;
	
	public ItemActivity() {
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Check whether we're recreating a previously destroyed instance
	    if (savedInstanceState != null) {
	        // Restore value of members from saved state
	        isPlaying = savedInstanceState.getBoolean(STATE_PLAYING);
	    } else {
	        // Initialize members with default values for a new instance
	    	isPlaying = false;
	    }
	    
//	    destroyWasCalled = false;
	    
	    sendSeekbarIntent = new Intent(BROADCAST_SEEKBAR);
	    
		setContentView(R.layout.activity_item);
		
		Bundle bundle = getIntent().getExtras();

//		View rootView = inflater.inflate(R.layout.fragment_item, container,
//				false);
//		parentActivity = getActivity();
		
		titleTextView = (TextView) findViewById(R.id.item_title);
		dateTextView = (TextView) findViewById(R.id.item_date);
		descriptionTextView = (TextView) findViewById(R.id.item_description);
		setlistTextView = (TextView) findViewById(R.id.item_setlist);
		posterImageView = (ImageView) findViewById(R.id.item_poster);

		startServiceButton = (ImageButton) findViewById(R.id.item_player_startbutton);
		stopServiceButton = (ImageButton) findViewById(R.id.item_player_stopbutton);
		counterTextView = (TextView) findViewById(R.id.item_player_counter);
		progressBar = (ProgressBar) findViewById(R.id.item_player_progressbar);
		seekBar = (SeekBar) findViewById(R.id.item_player_seekbar);
		
		seekBar.setOnSeekBarChangeListener(this);
		
		// Changing pic of start button according to state retrieved from the saved bundle
		if (isPlaying == false) {
			startServiceButton.setImageResource(R.drawable.start_button_white);
		} else {
			startServiceButton.setImageResource(R.drawable.pause_button_white);
		}
		
		if(bundle != null) {
			Log.d("Bundle", "Bundle isn't null");
			String title = bundle.getString("title");
			String date = bundle.getString("date");
			String description = bundle.getString("description");
			String setlist = bundle.getString("setlist");
			String posterLink = bundle.getString("poster_link");
			final String icastLink = bundle.getString("icast_link");
			
			// Setting the text views
			titleTextView.setText(title);
			dateTextView.setText(date);
			descriptionTextView.setText(description);
			
			// Setting the setlist text view
			setlistTextView.setText(setlist);
			
			// Setting the poster image
			ImageFetcher imgFetch = new ImageFetcher(this, posterImageView);
			imgFetch.getImageLocation(posterLink);
			
			// Setting and downloading the icast audio file
			startServiceButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					if (isPlaying == false) {
						startPlayerService(icastLink);
						startServiceButton.setImageResource(R.drawable.pause_button_white);
					} else {
						pausePlayerService();
						startServiceButton.setImageResource(R.drawable.start_button_white);
					}
				}
			});

			stopServiceButton.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {

					stopPlayerService();
				}
			});
			
		} else {
			Log.d("ItemFragment onCreate", "Bundle is empty");
		}

		if(!mBound) {
			Intent intent = new Intent(this, ServicePlayer.class);
//			intent.putExtra("icastLink", icastLink);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}
	
	private BroadcastReceiver broadcastBufferReciever = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent serviceIntent) {
			Bundle bundle = serviceIntent.getExtras();
			if(bundle.getBoolean("buffering_complete")) {	// When the song has been loaded
				startServiceButton.setImageResource(R.drawable.pause_button_white);
				startServiceButton.setAlpha(1F);
				startServiceButton.setEnabled(true);
				progressBar.setAlpha(0F);
			} else if(bundle.getBoolean("error")){	// When an error has occurred
				startServiceButton.setImageResource(R.drawable.start_button_white);
			} else {	// When the song has started loading
				startServiceButton.setAlpha(0F);
				startServiceButton.setEnabled(false);
				progressBar.setAlpha(1F);
			}
		}
	};
	
	private BroadcastReceiver broadcastReciever = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent serviceIntent) {
			updatePlayerUI(serviceIntent);
		}
	};
	
	private void updatePlayerUI(Intent serviceIntent) {
		int seekProgress = serviceIntent.getIntExtra("counter", 0);
		seekMax = serviceIntent.getIntExtra("media_max", 0);
		songEnded = serviceIntent.getBooleanExtra("song_ended", false);
				
		if(mediaMaxString == "") {
			mediaMaxString = getTime(seekMax);
		}
		
		String counterString = getTime(seekProgress) + "/" + mediaMaxString;
		
		counterTextView.setText(counterString);
		
		seekBar.setMax(seekMax);
		seekBar.setProgress(seekProgress);
		
		if(songEnded) {
			startServiceButton.setImageResource(R.drawable.start_button_white);
			stopPlayerService();
		}
	}
	
	private String getTime(int millis) {
		long second = (millis / 1000) % 60;
		long minute = (millis / (1000 * 60)) % 60;
		long hour = (millis / (1000 * 60 * 60)) % 24;

		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	@Override
	public void onDestroy() {
		mService.stopPlayback();
		unbindService(mConnection);
		Log.d("ItemFragment, onDestroy", "Playback stopped");
		super.onDestroy();
	}

	protected void startPlayerService(String icastLink) {
		isPlaying = true;
		
		mService.startPlayback(icastLink);

		registerReceiver(broadcastBufferReciever, new IntentFilter(mService.BROADCAST_BUFFER));
		mBufferBroadcastIsRegistered = true;
		registerReceiver(broadcastReciever, new IntentFilter(mService.BROADCAST_ACTION));
		broadcastIsRegistered = true;
//		parentActivity.startService(intent);
	}

	protected void pausePlayerService() {
//		Intent intent = new Intent(parentActivity, ServicePlayer.class);
//		intent.putExtra("pause", true);
//		parentActivity.startService(intent);
		mService.pausePlayback();
		isPlaying = false;
	}
	
	protected void stopPlayerService() {
//		Intent intent = new Intent(parentActivity, ServicePlayer.class);
//		parentActivity.stopService(intent);
		
		startServiceButton.setImageResource(R.drawable.start_button_white);
		seekBar.setProgress(0);
		
		String counterString = "00:00:00" + "/" + mediaMaxString;
		counterTextView.setText(counterString);
		
		if(mBufferBroadcastIsRegistered) {
			try {
			unregisterReceiver(broadcastBufferReciever);
			mBufferBroadcastIsRegistered = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(broadcastIsRegistered) {
			try {
			unregisterReceiver(broadcastReciever);
			broadcastIsRegistered = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		mService.stopPlayback();
		isPlaying = false;
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    // Save the user's current game state
	    savedInstanceState.putBoolean(STATE_PLAYING, isPlaying);
	    
	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

	@Override
	protected void onResume() {
		super.onResume();
		
		// Register broadcast receiver
		if(!mBufferBroadcastIsRegistered) {
			registerReceiver(broadcastBufferReciever, new IntentFilter(mService.BROADCAST_BUFFER));
			mBufferBroadcastIsRegistered = true;
		}

		// Register seekbar broadcast receiver
		if(!broadcastIsRegistered) {
			registerReceiver(broadcastReciever, new IntentFilter(mService.BROADCAST_ACTION));
			broadcastIsRegistered = true;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		// Unregister broadcast receiver
		if (mBufferBroadcastIsRegistered) {
			try {
				unregisterReceiver(broadcastBufferReciever);
				mBufferBroadcastIsRegistered = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Unregister seekbar broadcast receiver
		if (broadcastIsRegistered) {
			try {
				unregisterReceiver(broadcastReciever);
				broadcastIsRegistered = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.item_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Intent returnIntent;
		
		switch (id) {
			case R.id.action_main:
				returnIntent = new Intent();
				returnIntent.putExtra("go_to_main",true);
				setResult(RESULT_OK,returnIntent);
				finish();
				return true;
								
			case R.id.action_list:
				returnIntent = new Intent();
				returnIntent.putExtra("go_to_main",false);
				setResult(RESULT_OK,returnIntent);
				finish();
				return true;
				
			case R.id.action_about:
				AboutDialog dialog = new AboutDialog();
				dialog.showAboutDialog(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(fromUser) {
			int seekPos = seekBar.getProgress();
			sendSeekbarIntent.putExtra("seekPos", seekPos);
			sendBroadcast(sendSeekbarIntent);
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
