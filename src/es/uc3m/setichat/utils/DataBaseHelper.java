package es.uc3m.setichat.utils;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import es.uc3m.setichat.activity.MainActivity;
import es.uc3m.setichat.contactsHandling.Contact;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.TextView;


public class DataBaseHelper extends SQLiteOpenHelper {

	private final static String CONTACTS_TABLE="contacts";
	private final static String UNREADMESSAGES_TABLE="unreadmessages";
	private final static String MESSAGES_TABLE="messages";
	private final static String PUBLIC_KEYS_TABLE="pubkeys";
	private final static String KEYPAIRS_TABLE="keypairs";



				

	public DataBaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		//create all the DB tables

		//contacts database
		database.execSQL("CREATE TABLE "+CONTACTS_TABLE+"(nick TEXT,number TEXT PRIMARY KEY);");
		//unreadmessages database
		database.execSQL("CREATE TABLE "+UNREADMESSAGES_TABLE+"("+"_id INTEGER PRIMARY KEY, number TEXT ,message TEXT);");
		//messages table
		database.execSQL("CREATE TABLE "+MESSAGES_TABLE+"(_id INTEGER PRIMARY KEY, sender TEXT, receiver TEXT, date TIMESTAMP DEFAULT current_timestamp, message TEXT);");
		//public keys
		database.execSQL("CREATE TABLE "+PUBLIC_KEYS_TABLE+"(number TEXT,key TEXT,isValid INTEGER, PRIMARY KEY(number,isValid));");
		//keyPairs
		database.execSQL("CREATE TABLE "+KEYPAIRS_TABLE+"(pubKey TEXT,privKey TEXT,isValid INTEGER, PRIMARY KEY(pubKey,privKey));");


	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int vold	, int vnew) {

		//if existe drop all tables
		database.execSQL("DROP TABLE IF EXISTS "+CONTACTS_TABLE);
		database.execSQL("DROP TABLE IF EXISTS "+UNREADMESSAGES_TABLE);
		database.execSQL("DROP TABLE IF EXISTS "+MESSAGES_TABLE);

		//create all tables again
		//contacts database
		database.execSQL("CREATE TABLE "+CONTACTS_TABLE+"(nick TEXT,number TEXT PRIMARY KEY);");
		//messages database
		database.execSQL("CREATE TABLE "+UNREADMESSAGES_TABLE+"("+"_id INTEGER PRIMARY KEY, number TEXT ,message TEXT);");
		//messages table
		database.execSQL("CREATE TABLE "+MESSAGES_TABLE+"(_id INTEGER PRIMARY KEY, sender TEXT, receiver TEXT, date INT, message TEXT);");
		//public keys
		database.execSQL("CREATE TABLE "+PUBLIC_KEYS_TABLE+"(number TEXT,key TEXT,isValid INTEGER, PRIMARY KEY(number,isValid));");
		//keyPairs
		database.execSQL("CREATE TABLE "+KEYPAIRS_TABLE+"(pubKey TEXT,privKey TEXT,isValid INTEGER, PRIMARY KEY(pubKey,privKey));");

	}


	//this method writes the message passed on the "pendingMessage" string to the unreadmessages table

	public static  int writeUnreadMessages(SQLiteDatabase db,String pendingMessage,String NIA){

		db.execSQL("INSERT INTO "+UNREADMESSAGES_TABLE+"(number,message) VALUES ('"+NIA+"', '"+pendingMessage+"');");

		return 0;

	}


	//used for get a String array with the database.  Each entry has the format nck.number
	public static String[] contactsToStringArray( SQLiteDatabase database){

		ArrayList<String> list=new ArrayList<String>();
		database=MainActivity.helper.getReadableDatabase();


		Cursor resultSet= database.query(CONTACTS_TABLE,null,null,null, null, null, null);
		if(resultSet.moveToFirst()){
			do{
				list.add(resultSet.getString(0)+"."+resultSet.getString(1));

			}while(resultSet.moveToNext());

		}else{
			list.add("<empty contact list>");
		}

		String[] contacts =new String[list.size()];
		for(int i=0;i<list.size();i++){
			contacts[i]=list.get(i);


		}
		return contacts;
	}

	public static ArrayList<Contact> contactsToArray(SQLiteDatabase db){

		ArrayList<Contact> list=new ArrayList<Contact>();
		Cursor resultset;

		resultset=db.query(CONTACTS_TABLE,null,null,null, null, null, null);

		if(resultset.moveToFirst()){
			//the result is not empty

			do{
				list.add(new Contact( resultset.getString(0),resultset.getString(1)));

			}while(resultset.moveToNext());


		}else{
			//result is empty
			list.add(null);

		}




		return list;

	}

	public static int saveContacts(HashMap<String,String> contacts,SQLiteDatabase db){

		String query="";
		int entries=0;

		for(String key:contacts.keySet()){
			Cursor resultSet= db.query(CONTACTS_TABLE,null,"number=? AND nick=?",new String[]{key,contacts.get(key)}, null, null, null);

			if(!resultSet.moveToFirst()){
				query="INSERT INTO contacts(number,nick) VALUES ('"+key+"', '"+contacts.get(key)+"');";
				Log.i("[debug]", query);
				entries++;
				db.execSQL(query);	
			}
		}

		return entries;
	}

	public static int saveContacts(ArrayList<Contact> contacts,SQLiteDatabase db){

		String query="";
		int entries=0;

		for(Contact contact:contacts){
			Cursor resultSet= db.query(CONTACTS_TABLE,null,"number=? AND nick=?",new String[]{contact.getNumber(),contact.getNick()}, null, null, null);

			if(!resultSet.moveToFirst()){
				query="INSERT INTO contacts(number,nick) VALUES ('"+contact.getNumber()+"', '"+contact.getNick()+"');";
				Log.i("[debug]", query);
				entries++;
				db.execSQL(query);	
			}
		}

		return entries;
	}


	public static String getNickByNumber(String number,SQLiteDatabase db){
		String nick="";
		Cursor resultSet= db.query(CONTACTS_TABLE,new String[]{"nick"},"number=?",new String[]{number}, null, null, null);

		if(resultSet.moveToFirst()){
			nick=resultSet.getString(0);
		}else{
			nick="Unknown Contact";
		}

		return nick;
	}

	//retrieve unread message from the DB
	public static String retrieveUnreadMessages(SQLiteDatabase db, Contact contact){

		String messages="";
		Cursor result=db.query("unreadMessages", new String[]{"message"}, "number=?", new String[]{contact.getNumber()}, null, null, null);

		if(result.moveToFirst()){
			do{
				//if there are unread messages
				Time time = new Time(System.currentTimeMillis());

				messages+="\n"+result.getString(0)+" [@"+contact.getNick()+" at "+time+"]";


			}while(result.moveToNext());
		}
		//delete the unread messages from the DB
		db.delete("unreadMessages", "number=?", new String[]{contact.getNumber()});
		db.close();

		return messages;	
	}

	public static void saveMessages(String sender,String sender_nick, String receiver, String message,SQLiteDatabase db){
		String message_timestamped="\n"+message+" [@"+sender_nick+" at "+new Time(System.currentTimeMillis())+"]\n";
		Log.d("fsfsdf", "sdfsdfdf1_");

		String query="INSERT INTO "+MESSAGES_TABLE+"(sender,receiver,message) VALUES('"+sender+"', '"+receiver+"','"+message_timestamped+"');";
		Log.d("fsfsdf", "sdfsdfdf2_");

		Log.d("[query ]",query);
		
		db.execSQL(query);
		Log.d("fsfsdf", "sdfsdfdf33");

	}
	
	
	
	
	public static String retrieveChat(String contact, SQLiteDatabase db){
		String chat="";
		Cursor res=db.query(MESSAGES_TABLE, new String[]{"message"}, "sender=? OR receiver=?",new String[]{contact,contact},null,null,"_id ASC");

		Log.d("ads", String.valueOf(res.getCount()));
		while(res.moveToNext()){
			
			
			chat+=res.getString(0);
			
		}
	
		return chat;
	}
	
	
	
	
	
	public static String getLastMessages(String sender,SQLiteDatabase db){

		String last="hey! wanna chat?";
		Cursor res=db.query(MESSAGES_TABLE,new String[]{"message"},"sender=? OR receiver=?",new String[]{sender,sender},null,null,"_id DESC");

		if(res.moveToFirst()){
			
			last=res.getString(0).substring(1);
		}
		
			return last;
	}
	
	public static void getMessages(String sender,SQLiteDatabase db){

		Cursor res=db.query(MESSAGES_TABLE, null, "sender=?",new String[]{sender},null,null,"date ASC");

		
		while(res.moveToNext()){
			
			Log.d("[lol",res.getInt(0)+" "+res.getString(1)+" "+res.getString(2)+" "+res.getString(3)+" "+res.getString(4));
			
		}

	}
	
	public static void saveKeyPair(KeyPair pair,SQLiteDatabase db){
		String pubKey=Base64.encodeToString(pair.getPublic().getEncoded(), false);
		String privKey=Base64.encodeToString(pair.getPrivate().getEncoded(), false);
		String query="INSERT INTO "+KEYPAIRS_TABLE+ "(pubKey,privKey,isValid) VALUES('"+pubKey+"', '"+privKey+"', '1');";
		
		db.execSQL("INSERT INTO "+KEYPAIRS_TABLE+ "(pubKey,privKey,isValid) VALUES('"+pubKey+"', '"+privKey+"', '1');");
		
		
		Log.i("[save keys query]",query);
		
	}
	
	
	public static KeyPair retrieveKeyPair(SQLiteDatabase db) {
		Cursor result;
		PrivateKey privKey=null;
		PublicKey pubKeyString=null;
		KeyPair pair=null;
		
		result=db.query(KEYPAIRS_TABLE, new String[]{"pubKey","privKey"},"isValid=?" ,new String[]{"1"} ,null,null,null);
		
		if(result.moveToFirst()){
			//Log.i("[retrieved pubKey]",result.getString(0));
			//Log.i("[retrieved privKey]",result.getString(1));
			
			try{
		//  get public key
		    X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(result.getString(0)));
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    pubKeyString = kf.generatePublic(spec);

		//get Private Key
		    PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(Base64.decode(result.getString(1)));
		     privKey = kf.generatePrivate(specPriv);
			}catch(Exception e){
				e.printStackTrace();
				
			}
			
			pair =new KeyPair(pubKeyString, privKey);
			
		}
		
		
		return pair;
	}
	
	
	
	
	
	
}



