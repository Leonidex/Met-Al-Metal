package com.leonidex.mediaplayer;

import java.io.IOException;

import com.leonidex.metalmetal.ItemActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

/**
 * This example was created by Sciatis Technologies and belongs to.
 * 
 * Using this samples for teaching/training or distribution requires written
 * approval from Sciatis Technologies.
 * 
 * Sciatis Technologies will not allow the use of this examples besides than
 * development.
 * 
 * For any questions please contact Gabriel@Proto-Mech.com
 * 
 * @author Gabriel@Proto-Mech.com
 */
public class ServicePlayer extends Service implements OnPreparedListener,
		OnCompletionListener, OnSeekCompleteListener, OnErrorListener,
		OnBufferingUpdateListener, OnInfoListener {

	private final IBinder mBinder = new LocalBinder();

	Bundle bundle;

	private MediaPlayer player = null;
	private boolean isReady = false;

	private String icastLink = "";

	// SeekBar Variables
	String sntSeekPos;
	int intSeekPos;
	int mediaPosition;
	int mediaMax;
	private final Handler handler = new Handler();
	private static boolean songEnded;
	public static final String BROADCAST_ACTION = "com.leonidex.metalmetal.seekprogress";

	public static final String BROADCAST_BUFFER = "com.leonidex.metalmetal.broadcastbuffer";
	Intent seekIntent;
	Intent bufferIntent;
	
	boolean broadcastIsRegistered = false;
	
	// Receive seekbar position if it has been changed by the user
	
	private BroadcastReceiver broadcastReciever = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			 updateSeekPos(intent);
		}
	};

	public ServicePlayer() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("ServicePlayer", "OnCreate");
		
		seekIntent = new Intent(BROADCAST_ACTION);
		bufferIntent = new Intent(BROADCAST_BUFFER);
		
		registerReceiver(broadcastReciever, new IntentFilter(ItemActivity.BROADCAST_SEEKBAR));
		broadcastIsRegistered = true;
		
		createNewPlayer();
	}

	private void createNewPlayer() {
		
		try {
			player = new MediaPlayer();
			player.setWakeMode(getApplicationContext(),
					PowerManager.PARTIAL_WAKE_LOCK);
			player.setLooping(false);
			player.setOnPreparedListener(this);
			player.setOnCompletionListener(this);

			isReady = true;
		} catch (Throwable e) {
			Log.e(getClass().getName(), "Fail:", e);
			e.printStackTrace();
		}

		player.setOnSeekCompleteListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
		player.setOnBufferingUpdateListener(this);

		player.setOnInfoListener(this);
	}

	private void destroyPlayer() {
		if (player.isPlaying()) {
			player.stop();
		}
		player.release();
		player = null;
	}

	private boolean playerIsPaused = false;
	private int length;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// bundle = intent.getExtras();
		// if (bundle != null) {
		// startPlayback();
		// }
		
		createNewPlayer();
		return START_STICKY;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		
		bufferIntent.putExtra("buffering_complete", true);
		sendBroadcast(bufferIntent);
		
		player.start();
	}
	
	@Override
	public void onCompletion(MediaPlayer mp) {
//		if (mp != null) {
//			mp.release();
//			mp = null;
//		}
		// stopSelf();
		
		songEnded = true;
		playerIsPaused = true;
		
		seekIntent.putExtra("counter", 0);
		seekIntent.putExtra("media_max", mediaMax);
		seekIntent.putExtra("song_ended", songEnded);
		sendBroadcast(seekIntent);
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(sendUpdatesToUI);
		if (player != null) {
			destroyPlayer();
		}
		
		// Unregister broadcast receiver
		if (broadcastIsRegistered) {
			try {
				unregisterReceiver(broadcastReciever);
				broadcastIsRegistered = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Log.d("ServicePlayer, onDestroy", "Player destroyed");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {

		return mBinder;
	}

	public class LocalBinder extends Binder {
		public ServicePlayer getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return ServicePlayer.this;
		}
	}

	public void startPlayback(String newIcastLink) {

		songEnded = false;
		if (player.isPlaying()) {
			Log.d("startPlayback", "player IS playing");
		}

		if (icastLink.isEmpty() || icastLink != newIcastLink) {

//			destroyPlayer();
//			createNewPlayer();

			icastLink = newIcastLink;
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				
				bufferIntent.putExtra("buffering_complete", false);
				bufferIntent.putExtra("error", false);
				sendBroadcast(bufferIntent);
				
				player.setDataSource(icastLink);
				player.prepareAsync();
			} catch (IllegalArgumentException | SecurityException
					| IllegalStateException | IOException e) {
				e.printStackTrace();
			}
		} else if (playerIsPaused) {
			player.seekTo(length);
			player.start();
			playerIsPaused = false;
		} else {
			Toast.makeText(this, "Failed to init the Player", Toast.LENGTH_LONG)
					.show();
		}

		setupHandler();
	}

	private void setupHandler() {
		handler.removeCallbacks(sendUpdatesToUI);
		handler.postDelayed(sendUpdatesToUI, 1000);
	}

	private Runnable sendUpdatesToUI = new Runnable() {

		@Override
		public void run() {
			logMediaPosition();
			handler.postDelayed(this, 500);
		}
	};

	private void logMediaPosition() {
		if (player.isPlaying()) {
			mediaPosition = player.getCurrentPosition();

			mediaMax = player.getDuration();
			seekIntent.putExtra("counter", mediaPosition);
			seekIntent.putExtra("media_max", mediaMax);
			seekIntent.putExtra("song_ended", songEnded);
			sendBroadcast(seekIntent);
		}
	}
	
	public void updateSeekPos(Intent intent) {
		int seekPos = intent.getIntExtra("seekPos", 0);
		if(player.isPlaying()) {
			handler.removeCallbacks(sendUpdatesToUI);
			player.seekTo(seekPos);
			setupHandler();
		}
	}
	
	public void pausePlayback() {
		if (player.isPlaying()) {
			player.pause();
			length = player.getCurrentPosition();
			playerIsPaused = true;
		}
	}

	public void stopPlayback() {
		if (player.isPlaying()) {
			player.pause();
			playerIsPaused = true;
		}
		length = 0;
	}

//	public boolean isPlayerPlaying() {
//		return player.isPlaying();
//	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		
		bufferIntent.putExtra("buffering_complete", false);
		bufferIntent.putExtra("error", true);
		sendBroadcast(bufferIntent);
		
		return false;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}
}