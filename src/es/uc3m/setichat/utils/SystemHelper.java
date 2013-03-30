package es.uc3m.setichat.utils;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class SystemHelper {


	//this is a sort of "dispersion function".
	//given a  n-length string, converts it into a 32-bit 
	public static int string2Integer(String str){

		int res = 0;

		for(int i = 0; i < str.length(); i++) {
			byte b = (byte) str.charAt(i);
			if(res + b > Integer.MAX_VALUE) {
				res = b;
			}
			else {
				res += b;
			}
		}

		return Math.abs(res);
	}




	public static String readContacts(Context context){



		ArrayList<String> contacts=new ArrayList<String>();  

		String mobileList="";

		//get the contacts from phone book
		Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		while (phones.moveToNext())
		{

			String number=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			number=  number.replaceAll("-", "").replaceAll(" ", "");

			//remove duplicated entries
			if(!contacts.contains(number)){
				contacts.add(number);

			}
		}

		//build te value of <cotacts> tag XML message ( on contacts request )
		for(String contact:contacts){
			mobileList=mobileList+"<mobile>"+contact+"</mobile>";
		}
	
	return mobileList;
	}






}