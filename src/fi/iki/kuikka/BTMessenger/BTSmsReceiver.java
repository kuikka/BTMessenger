package fi.iki.kuikka.BTMessenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class BTSmsReceiver extends BroadcastReceiver {
	private static final String TAG = "BTMSG";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "BTSmsReceiver onReceive()\n");
		
		if (intent.getAction().equals(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED)) {
			int current_state, previous_state;
			Log.d(TAG, "BTSmsReceiver::onReceive(ACTION_STATE_CHANGED)");
			current_state = intent.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE,
					android.bluetooth.BluetoothAdapter.ERROR);
			previous_state = intent.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_PREVIOUS_STATE,
					android.bluetooth.BluetoothAdapter.ERROR);
			Log.d(TAG, "Current state: " + current_state + "previous state: " + previous_state);
			if (current_state == android.bluetooth.BluetoothAdapter.STATE_ON) {
				context.startService(new Intent(context, BTSmsService.class));
			}
			if (current_state == android.bluetooth.BluetoothAdapter.STATE_TURNING_OFF) {
				context.stopService(new Intent(context, BTSmsService.class));
			}
		}

		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;
			String str = "";
			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];
				for (int i=0; i<msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
					str += "SMS from " + msgs[i].getOriginatingAddress();
					str += " :";
					str += msgs[i].getMessageBody().toString();
					str += "\n";
				}
				Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
				Log.d(TAG, str);
			}
		}
	}
}
