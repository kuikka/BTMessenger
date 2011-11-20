package fi.iki.kuikka.BTMessenger;

import org.json.JSONObject;
import org.json.JSONArray;
import android.util.Log;

public class BTSMS {
	public long messageId;
	public long threadId;
	public String address;
	public long contactId;
	public long timestamp;
	public String body;
	private static final String TAG = "BTMSG";

	public String toString() {
		return new String(messageId + "," + 
				threadId + "," +
				address + "," +
				contactId + "," +
				timestamp + "," +
				body);
	}
	
	public JSONObject asJSONObject() {
		JSONObject j = new JSONObject();
		try {
			j.put("messageId", messageId);
			j.put("threadId", threadId);
			j.put("address", address);
			j.put("contactId", contactId);
			j.put("timestamp", timestamp);
			j.put("body", body);
		} catch (Exception e) {
			Log.d(TAG, Log.getStackTraceString(e));
			j = null;
		}
		return j;
	}
	
	static public JSONArray asJSONArray(BTSMS[] messages) {
		JSONArray j = new JSONArray();
		try {
			for (BTSMS sms : messages) {
				j.put(sms.asJSONObject());
			}
		} catch (Exception e) {
			Log.d(TAG, Log.getStackTraceString(e));
			j = null;
		}
		return j;
	}
}
