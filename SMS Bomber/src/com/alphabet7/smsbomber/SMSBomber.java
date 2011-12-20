package com.alphabet7.smsbomber;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.Phones;
import android.telephony.gsm.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class SMSBomber extends Activity {
	AdView adView;
	Button chooseContact;

	/** Tag string for our debug logs */

	public static final String SMS_RECIPIENT_EXTRA = "com.example.android.apis.os.SMS_RECIPIENT";

	public static final String ACTION_SMS_SENT = "com.example.android.apis.os.SMS_SENT_ACTION";

	public static final int PICK_CONTACT = 123;

	EditText recipientTextEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Create the AdView
		adView = new AdView(this, AdSize.BANNER, "a14e6a384b6bf83");

		LinearLayout layout = (LinearLayout) findViewById(R.id.admob);
		// Add the adView to it
		layout.addView(adView);
		// Initiate a generic request to load it with an ad
		AdRequest request = new AdRequest();
		request.addTestDevice(AdRequest.TEST_EMULATOR);
		adView.loadAd(request);

		chooseContact = (Button) findViewById(R.id.contactsBtn);

		chooseContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK,
						Phones.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT);
			}
		});

		if (getIntent().hasExtra(SMS_RECIPIENT_EXTRA)) {
			((TextView) findViewById(R.id.recipient)).setText(getIntent()
					.getExtras().getString(SMS_RECIPIENT_EXTRA));
			((TextView) findViewById(R.id.mesage)).requestFocus();
		}

		recipientTextEdit = (EditText) findViewById(R.id.recipient);
		final EditText contentTextEdit = (EditText) findViewById(R.id.mesage);
		final EditText msgSumTextEdit = (EditText) findViewById(R.id.messageSum);
		final TextView statusView = (TextView) findViewById(R.id.msgStatus);

		// Watch for send button clicks and send text messages.
		final Button sendButton = (Button) findViewById(R.id.sendBtn);
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (TextUtils.isEmpty(recipientTextEdit.getText())) {
					Toast.makeText(SMSBomber.this,
							"Please enter a message recipient.",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (TextUtils.isEmpty(contentTextEdit.getText())) {
					Toast.makeText(SMSBomber.this,
							"Please enter a message body.", Toast.LENGTH_SHORT)
							.show();
					return;
				}

				recipientTextEdit.setEnabled(false);
				contentTextEdit.setEnabled(false);
				msgSumTextEdit.setEnabled(false);
				sendButton.setEnabled(false);

				SmsManager sms = SmsManager.getDefault();

				List<String> messages = sms.divideMessage(contentTextEdit
						.getText().toString());

				String recipient = recipientTextEdit.getText().toString();
				int msgSum = Integer.parseInt(msgSumTextEdit.getText()
						.toString());
				for (int i = 0; i < msgSum; i++) {
					for (String message : messages) {
						sms.sendTextMessage(recipient, null, message,
								PendingIntent.getBroadcast(SMSBomber.this, 0,
										new Intent(ACTION_SMS_SENT), 0), null);
					}
				}
			}
		});

		// Register broadcast receivers for SMS sent and delivered intents
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String message = null;
				boolean error = true;
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					message = "Message sent!";
					error = false;
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					message = "Error.";
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					message = "Error: No service.";
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					message = "Error: Null PDU.";
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					message = "Error: Radio off.";
					break;
				}

				recipientTextEdit.setEnabled(true);
				contentTextEdit.setEnabled(true);
				msgSumTextEdit.setEnabled(true);
				sendButton.setEnabled(true);
				contentTextEdit.setText("");

				statusView.setText(message);
				statusView.setTextColor(error ? Color.RED : Color.GREEN);
			}
		}, new IntentFilter(ACTION_SMS_SENT));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		adView.destroy();
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String phoneNumber = c.getString(c
							.getColumnIndex(Phones.NUMBER));

					recipientTextEdit.setText(phoneNumber);
				}
			}
			break;
		}
	}
}