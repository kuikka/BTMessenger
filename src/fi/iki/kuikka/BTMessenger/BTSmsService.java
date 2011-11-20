package fi.iki.kuikka.BTMessenger;

import java.util.UUID;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class BTSmsService extends Service {
	private static final String TAG = "BTMSG";
	UUID mBtUUID = UUID.fromString("16489d38-7f3e-47de-bd80-6a319c73eb01");
	BluetoothAdapter mBtAdapter;
	BTListenerThread mBtListener;
	BTMessenger mMessenger;
	
	private void initListener() {
		Log.d(TAG, "BT adapter name:" + mBtAdapter.getName() + "\n");
		BTMessenger.setContext(this);
		mBtListener = new BTListenerThread(mBtAdapter, mBtUUID);
		mBtListener.start();
	}
	
	private void deInitListener() {
		mBtListener.stopListening();
		BTMessenger.closeClients();
	}

	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		    // Handle reciever
		    String mAction = intent.getAction();

		    if(mAction.equals("android.provider.Telephony.SMS_RECEIVED")) {
		      // Do your thing   
		    }
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "BTSmsService::onBind()");
		return null;
	}

	@Override
	public void onCreate() {
		//code to execute when the service is first created
		Log.d(TAG, "BTSmsService::onCreate()");
        SharedPreferences p = getApplicationContext().getSharedPreferences("BTMessenger", 0);
        int myProperty = p.getInt("MyProperty", -1);
        Log.d(TAG, "My Property = " + myProperty);
	}

	@Override
	public void onDestroy() {
		//code to execute when the service is shutting down
		Log.d(TAG, "BTSmsService::onDestroy()");
		deInitListener();
	}

	/* We should never be started when BT is off */
	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		//code to execute when the service is starting up
		Log.d(TAG, "BTSmsService::onStart()");
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter != null)
			initListener();
		return START_STICKY;
	}


}
