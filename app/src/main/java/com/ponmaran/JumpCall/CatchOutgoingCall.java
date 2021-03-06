package com.ponmaran.JumpCall;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class CatchOutgoingCall extends BroadcastReceiver {
	private static final String TAG_BRD_REC = "OutgoingCallReceiver";
	SharedPreferences sharedPref;

	@Override
	public void onReceive(Context context, Intent intent) {
		Date callBroadcastTime = new Date();
		Bundle extras = intent.getExtras();
		String extrasData = "";
		for(String key : extras.keySet()) extrasData = extrasData.concat(" " + key + ": " + extras.getString(key));
		Log.d(TAG_BRD_REC, "onReceive Extras:" + extrasData);

		String orgNum = extras.getString(Intent.EXTRA_PHONE_NUMBER);

		sharedPref = context.getSharedPreferences(MainActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);

//		Phone number type identification
		String numSeq = "";
		String[][] filterSet = getSavedFilterData();
		for (String[] aFilterSet : filterSet) {
			if (orgNum.startsWith(aFilterSet[0])) {
				String number = orgNum;
				if (!aFilterSet[1].isEmpty())
				    number = aFilterSet[1] + number.substring(aFilterSet[0].length());
				numSeq = numSeqBuild(getSavedBridgeData(), number);
				break;
			}
		}

		if(numSeq.isEmpty())
			return;

		extras.putString("android.phone.extra.ORIGINAL_URI", "tel:" + numSeq);
		extras.putString("android.phone.extra.PHONE_NUMBER", numSeq);
		intent.putExtras(extras);

		this.setResultData(numSeq);

		Log.d(TAG_BRD_REC, "Number out: " + this.getResultData());

		//Setup CallLogObserver
		ContentResolver contentResolver = context.getContentResolver();

		CallLogObserver callLogObserver = new CallLogObserver(new Handler());
		callLogObserver.setContentResolver(contentResolver);
		callLogObserver.setCallBroadcastTime(callBroadcastTime);
		callLogObserver.setBridgeNumber(PhoneNumberUtils.extractNetworkPortion(numSeq));
		callLogObserver.setOriginalNumber(orgNum);
		Log.d(TAG_BRD_REC, callLogObserver.toString());

		contentResolver.registerContentObserver(Uri.parse("content://call_log/calls"),true,callLogObserver);

		//Notify user by Toast
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(orgNum));
		Cursor contactSetCursor = context.getContentResolver().query(uri, new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup.LABEL, PhoneLookup.TYPE}, null, null, null);

		String contactName = "";
		String contactLabel = "";
		String contactType = "";
		if(contactSetCursor != null && contactSetCursor.moveToFirst()){
			contactName = contactSetCursor.getString(contactSetCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			contactLabel = contactSetCursor.getString(contactSetCursor.getColumnIndex(PhoneLookup.LABEL));
			contactType = (String) Phone.getTypeLabel(context.getResources(), contactSetCursor.getInt(contactSetCursor.getColumnIndex(PhoneLookup.TYPE)), (CharSequence)"OTHER");
			contactSetCursor.close();
		}

		Toast.makeText(context, "Calling\n" +
				(contactName.length() > 0 && contactType.length() > 0 ?
						contactName + " " + (contactType.equals("OTHER") && contactLabel != null ? contactLabel : contactType) + " "
						:"") +
				orgNum + "\nusing " + context.getString(R.string.app_name), Toast.LENGTH_LONG).show();
	}

	private String numSeqBuild(String[][] set, String dialedNum){
		String numSeq = "";
		for (String[] aSet : set) {
			int delayTimeNum = aSet[1].equals("") ? 0 : Integer.parseInt(aSet[1]);
			String pauses = "";
			for (int j = 0; j < delayTimeNum; j += 2)
				pauses = pauses + ",";
			numSeq = numSeq + aSet[0] + pauses;
		}
        return numSeq + dialedNum;
    }

	private String[][] getSavedFilterData() {
		Log.d(TAG_BRD_REC, "Read saved filter data");
		String a = sharedPref.getString(MainActivity.SHARED_PREF_KEY_FILTER_SETS, "");
		String[] pairs = StringUtils.split(a,":");
		String[][] set = new String[pairs.length][2];
		for(int i=0;i<pairs.length;i++)
			set[i] = pairs[i].split("~");
		return set;
	}

	private String[][] getSavedBridgeData(){
		Log.d(TAG_BRD_REC, "Read saved bridge data");
		String a = sharedPref.getString(MainActivity.SHARED_PREF_KEY_BRIDGE_SETS, "");
		String[] pairs = StringUtils.split(a,":");
		String[][] set = new String[pairs.length][2];
		for(int i=0;i<pairs.length;i++)
			set[i] = StringUtils.split(pairs[i],"~");
		return set;
	}
}