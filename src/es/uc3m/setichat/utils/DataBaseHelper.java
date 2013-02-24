package es.uc3m.setichat.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DataBaseHelper extends SQLiteOpenHelper {

	String create = "CREATE TABLE contacts (" +
			"nick	TEXT," +
			"number	TEXT PRIMARY KEY)";
	
	public DataBaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(create);
		

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int vold	, int vnew) {
		 database.execSQL("DROP TABLE IF EXISTS contacts");
		 
	        database.execSQL(create);

	}

}
