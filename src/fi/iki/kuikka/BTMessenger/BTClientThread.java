package fi.iki.kuikka.BTMessenger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.JSONException;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BTClientThread extends Thread {
	private BluetoothSocket mSocket;
	private InputStream mIn;
	private InputStreamReader mInReader;
	private OutputStream mOut;
	private OutputStreamWriter mOutWriter;
	private static final String TAG = "BTMSG";
	private BTMessageParser mParser;
	
	public BTClientThread(BluetoothSocket socket) throws IOException {
		mSocket = socket;
		mIn = mSocket.getInputStream();
		mInReader = new InputStreamReader(mIn, "UTF-8");
		mOut = mSocket.getOutputStream();
		mOutWriter = new OutputStreamWriter(mOut, "UTF-8");
		mParser = new BTMessageParser(this);
	}

	public void sendResponse(BTMessage reply) throws IOException, JSONException { 
		mOutWriter.write(reply.toJSONObject().toString());
	}

	public void sendResponse(String s) throws IOException { 
		mOutWriter.write(s);
	}
	
	public void stopThread() {
		try {
			mSocket.close();
			this.join();
		} catch (Exception e) {
		}
	}
	
	public void run() {
		Log.d(TAG, "BTClientThread::run()");
		while(true) {
			try {
				mParser.readJson(mInReader);
			} catch (Exception e) {
				Log.d(TAG, "BTClientThread got exception, quitting.");
				Log.d(TAG, Log.getStackTraceString(e));
				try {
					mSocket.close();
				} catch (Exception ee) {
				}
				BTMessenger.removeClient(this);
				return;
			}
		}
	}

}
