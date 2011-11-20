package fi.iki.kuikka.BTMessenger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class BTMessengerActivity extends Activity {
	private static final String TAG = "BTMSG";
	TextView tv;
	static final int REQUEST_ENABLE_BT = 0;
	BluetoothAdapter mBtAdapter;

	private void startService() {
		startService(new Intent(this, BTSmsService.class));
	}

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate()\n");
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = new TextView(this);
        tv.setText("Hello!\n");
        setContentView(tv);
        
        SharedPreferences p = getApplicationContext().getSharedPreferences("BTMessenger", 0);
        Editor e = p.edit();
        
        e.putInt("MyProperty", 42);
        e.apply();
        
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter != null) {
        	if (!mBtAdapter.isEnabled()) {
        	    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        	} else {
        		startService();
        	}
        } else {
        	tv.append("No Bluetooth available" + "\n");
        }
        
//        BTSMS[] messages = BTMessenger.getMessagesFrom("425-894-0874");
//        BTMessage[] messages = BTMessenger.getMessagesFrom("+17145845525");
//        BTMessage[] messages = BTMessenger.getMessagesFrom("532");
/*
        if (messages != null ) {
        	for (int i = 0; i < messages.length; i++) {
        		Log.d(TAG, "Message[" + i + "]: " + messages[i].toString() + "\n");
        	}
        	Log.d(TAG, BTSMS.asJSONArray(messages).toString());
        }
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
            	Log.d(TAG, "Bluetooth turned on OK");
            	startService();
            }
            if (resultCode == RESULT_CANCELED) {
            	Log.d(TAG, "Cannot turn on BT");
            }
        }
    }
    
    public void onClose() {
    
    }
}