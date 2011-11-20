package fi.iki.kuikka.BTMessenger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;


public class BTMessenger {
	private static final String TAG = "BTMSG";
	private static List<BTClientThread> mClients = new ArrayList<BTClientThread>();
	private static Context mContext;

	public static final Uri MMS_SMS_CONTENT_URI = Uri.parse("content://mms-sms/");
	public static final Uri THREAD_ID_CONTENT_URI =
		Uri.withAppendedPath(MMS_SMS_CONTENT_URI, "threadID");
	public static final Uri CONVERSATION_CONTENT_URI =
        Uri.withAppendedPath(MMS_SMS_CONTENT_URI, "conversations");
	
	public static final String SMS_ID = "_id";
	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");

	public static void setContext(Context context) {
		mContext = context;
	}
	
	public static void addClient(BluetoothSocket socket) {
		BTClientThread thread = null;
		try {
			thread = new BTClientThread(socket);
			Log.d(TAG, "Adding new client thread. Thread count: " + mClients.size());
			synchronized(mClients) {
				mClients.add(thread);
			}
			thread.start();
		} catch (IOException e) {
			synchronized(mClients) {
				mClients.remove(thread); // Just to be sure
			}
			Log.d(TAG, "Cannot create client thread:" + e.getMessage());
			Log.d(TAG, Log.getStackTraceString(e));
		}
	}
	
	public static void removeClient(BTClientThread thread) {
		synchronized(mClients) {
			mClients.remove(thread);
		}
	}
	
	public static void closeClients() {
		Object[] threads;
		
		synchronized(mClients) {
			threads = mClients.toArray();
			mClients.clear();
		}
		for (int i = 0; i < threads.length; i++) {
			((BTClientThread)threads[i]).stopThread();
			try {
				((BTClientThread)threads[i]).join();
			} catch (Exception e) {
			}
		}
	}
	
	public static int sendSms(String msg, String number) {
		Log.d(TAG, "sendSms(\"" + msg + "\" to " + number + "\n");
		return sendSMS(mContext, number, msg);
	}
	
	public static BTSMS[] getMessagesInThread(long threadId, int maxMsgs, long since) {
		String SORT_ORDER = "date DESC";
		String DATE_LIMIT = "date > " + since;
		BTSMS[] messages = null;
		Uri.Builder uriBuilder = CONVERSATION_CONTENT_URI.buildUpon().appendPath(Long.toString(threadId));
		
		if (threadId == -1)
			return null;

		try {
			Cursor cursor = mContext.getContentResolver().query(
					uriBuilder.build(),
					new String[] { "_id", "thread_id", "address", "person", "date", "body" },
					DATE_LIMIT,
					null,
					SORT_ORDER);

			if (cursor != null) {
				try {
					int i = 0;
					int count = cursor.getCount();
					if (count == 0)
						return null;
					if (maxMsgs > 0) {
						if(count > maxMsgs)
							count = maxMsgs;
					}

					cursor.moveToFirst();
					messages = new BTSMS[count];
					while (i < count) {
						messages[i] = new BTSMS();
						messages[i].messageId = cursor.getLong(0);
						messages[i].threadId = cursor.getLong(1);
						messages[i].address = cursor.getString(2);
						messages[i].contactId = cursor.getLong(3);
						messages[i].timestamp = cursor.getLong(4);
						messages[i].body = cursor.getString(5);
						cursor.moveToNext();
						i++;
					}
				} finally {
					cursor.close();
				}
			}
		} catch (Exception e) {
			Log.d(TAG, "Cannot access SMS database:" + e.getMessage());
			Log.d(TAG, Log.getStackTraceString(e));
			messages = null;
		}
		return messages;
	}
	
	public static BTSMS[] getMessagesFrom(String address, int maxMsgs, long since) {
		return getMessagesInThread(getThreadId(address), maxMsgs, since);
	}

	public static long getThreadId(String number) {
		String THREAD_RECIPIENT_QUERY = "recipient";
		Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
		uriBuilder.appendQueryParameter(THREAD_RECIPIENT_QUERY, number);

		long threadId = -1;

		Cursor cursor = mContext.getContentResolver().query(
				uriBuilder.build(), 
				new String[] { SMS_ID },
				null, null, null);
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					threadId = cursor.getLong(0);
				}
			} finally {
				cursor.close();
			}
		}
		return threadId;			
	}

	static public int sendSMS(Context _context, String phoneNumber, String message) {
		class Result {
			public void setResult(int r) { mResult = r; }
			public int getResult() { return mResult; }
			protected int mResult;
		}
        final Context context = _context;
		String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        final Object completion = new Object();
        final Result result = new Result();
 
        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
            new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
            new Intent(DELIVERED), 0);
 
        //---when the SMS has been sent---
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
            	result.setResult(getResultCode());
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off", 
                                Toast.LENGTH_SHORT).show();
                        break;
                }
                completion.notifyAll();
            }
        }, new IntentFilter(SENT));
 
        //---when the SMS has been delivered---
        context.registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(context, "SMS not delivered", 
                                Toast.LENGTH_SHORT).show();
                        break;                        
                }
            }
        }, new IntentFilter(DELIVERED));        
 
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
        try {
        	completion.wait();
        } catch (InterruptedException e) {
        	return -1;
        }
        return result.getResult();
    }
}
