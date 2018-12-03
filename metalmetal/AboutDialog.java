package com.leonidex.metalmetal;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutDialog {
	
	AlertDialog.Builder builder;
	TextView message;
	
	public void showAboutDialog(Context context) {
		Resources res = context.getResources();
		
		if(builder == null) {
			builder = new Builder(context);
			message = new TextView(context);
			
			builder.setTitle(R.string.about_title);
			builder.setCancelable(true);
			builder.setIcon(R.drawable.ic_launcher);
			
			SpannableString messageString = new SpannableString(res.getString(R.string.about_message_1) + "\n" + res.getString(R.string.about_message_2) + "\n" + res.getString(R.string.about_message_3));
			Linkify.addLinks(messageString, Linkify.WEB_URLS);
			message.setText(messageString);
			message.setMovementMethod(LinkMovementMethod.getInstance());
			
			builder.setView(message);
			builder.setNeutralButton(res.getString(R.string.about_dismiss), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			});
			
			builder.create();
		}
		builder.show();
	}
}
