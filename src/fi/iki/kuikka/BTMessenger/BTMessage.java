package fi.iki.kuikka.BTMessenger;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BTMessage {
	protected HashMap<String, Object> mMsg;

	public BTMessage() {
		mMsg = new HashMap<String, Object>();
	}

	protected void verifyKey(String key) throws ParseException {
		if(!mMsg.containsKey(key))
			throw new ParseException("Message item \"" + key + "\" does not exist.");
	}

	public String getString(String key) throws ParseException {
		verifyKey(key);
		return (String) mMsg.get(key);
	}
	
	public Long getLong(String key) throws ParseException {
		verifyKey(key);
		return ((Long) mMsg.get(key)).longValue();
	}
	public void setString(String key, String value) { mMsg.put(key, value); }
	public void setLong(String key, long value) { mMsg.put(key, new Long(value)); }

	public void addSMS(String key, BTSMS[] sms) {
		JSONArray a = BTSMS.asJSONArray(sms);
		mMsg.put(key, a);
	}
	public boolean has(String key) { return mMsg.containsKey(key); }

	public void verify() throws ParseException {
		verifyKey("id");
		verifyKey("command");
	}
	
	public JSONObject toJSONObject() throws JSONException {
		JSONObject j = new JSONObject();

		for (Entry<String, Object> entry : mMsg.entrySet()) {
			j.put(entry.getKey(), entry.getValue());
		}
		return j;
	}

	public BTMessage createReply(int result) {
		BTMessage m = new BTMessage();
		m.setString("command", (String) mMsg.get("command"));
		m.setLong("id", ((Long) mMsg.get("id")).longValue());
		m.setLong("result", result);
		return m;
		
	}
}