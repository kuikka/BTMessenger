package fi.iki.kuikka.BTMessenger;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;


public class BTMessageParser {
	private static final String TAG = "BTMSG";
	private BTClientThread mClient;
	
	public BTMessageParser(BTClientThread client) {
		mClient = client;
	}

	public void readJson(Reader input) throws IOException, ParseException, JSONException {
		JsonFactory jsonFactory = new JsonFactory();
		JsonParser jp = jsonFactory.createJsonParser(input);
		JsonToken next;
		BTMessage message;
		
		while(true) {
			if (jp.nextToken() != JsonToken.START_OBJECT) {
				throw new IOException("Expected data to start with an Object");
			}
			message = new BTMessage();
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				String fieldName = jp.getCurrentName();
				next = jp.nextToken(); // Value is next
				if (next == JsonToken.VALUE_NUMBER_INT)
					message.setLong(fieldName, jp.getLongValue());
				else if (next == JsonToken.VALUE_STRING)
					message.setString(fieldName, jp.getText());
				else
					throw new JsonParseException("Unexpected token in JSON stream", jp.getCurrentLocation());
			}
			// END_OBJECT reached
			message.verify();
			handleCommand(message);
		}
	}
	
	protected void handleCommand(BTMessage message) throws IOException, ParseException, JSONException {
		String command = message.getString("command");

		if(command.equals("send-sms")) {
			handleSendSms(message);
		} else if (command.equals("get-sms")) {
			handleGetSms(message);
		}
	}
	
	public void handleSendSms(BTMessage message) throws IOException, ParseException, JSONException {
		String msg = message.getString("sms");
		String number = message.getString("number");
		mClient.sendResponse(message.createReply(BTMessenger.sendSms(msg, number)));
	}

	public void handleGetSms(BTMessage message) throws IOException, ParseException, JSONException {
		long since = -1;
		int max = -1;
		String number = null;
		BTSMS[] messages;

		if (message.has("since")) {
			since = message.getLong("since");
		}

		if (message.has("max")) {
			max = message.getLong("max").intValue();
		}
		
		number = message.getString("number");
		messages = BTMessenger.getMessagesFrom(number, max, since);
		BTMessage reply = message.createReply(0);
		reply.addSMS("sms", messages);
		mClient.sendResponse(reply);
	}
}
