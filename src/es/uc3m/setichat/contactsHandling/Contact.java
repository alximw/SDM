package es.uc3m.setichat.contactsHandling;

import java.io.Serializable;

import android.database.sqlite.SQLiteDatabase;
import es.uc3m.setichat.activity.MainActivity;
import es.uc3m.setichat.utils.DataBaseHelper;


public class Contact implements Serializable{

	private String nick;
	private String number;
	
	
	public Contact(){
		
		
	}
	
	public Contact(String nick,String number){
		this.nick=nick;
		this.number=number;
		
		
	}
	public Contact(String number){
		SQLiteDatabase db=MainActivity.helper.getWritableDatabase();
		DataBaseHelper.getNickByNumber(number, db);
		
		this.number=number;
		this.nick=DataBaseHelper.getNickByNumber(number, db);

	}
	
	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
	
	
	
}
