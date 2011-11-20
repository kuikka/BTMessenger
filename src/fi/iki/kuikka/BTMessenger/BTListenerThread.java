package fi.iki.kuikka.BTMessenger;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BTListenerThread extends Thread {
	private static final String TAG = "BTMSG";
	private BluetoothAdapter mAdapter;
	private BluetoothServerSocket socket;
	private boolean running = false;
	private UUID mUUID;

	public BTListenerThread(BluetoothAdapter adapter, UUID uuid) {
		mAdapter = adapter;
		mUUID = uuid;
	}

	public void run() {
		Log.d(TAG, "BTListenerThread::run()");
		try {
			socket = mAdapter.listenUsingRfcommWithServiceRecord("BTMessenger", mUUID);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		running = true;
		while(running) {
			try {
				BluetoothSocket s = socket.accept();
				Log.d(TAG, "Got client: " + s.getRemoteDevice().getName());
				BTMessenger.addClient(s);
			} catch (IOException e) {
				Log.d(TAG, Log.getStackTraceString(e));
			}
		}
	}
	public void stopListening() {
		Log.d(TAG, "BTListenerThread::stopListening()");
		if(socket != null) {
			running = false;
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
}
