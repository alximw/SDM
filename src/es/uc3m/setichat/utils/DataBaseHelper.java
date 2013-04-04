package es.uc3m.setichat.utils;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
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


public class DataBaseHelper extends SQLiteOpenHelper {

	private final static String CONTACTS_TABLE="contacts";
	private final static String UNREADMESSAGES_TABLE="unreadmessages";
	private final static String MESSAGES_TABLE="messages";
	private final static String PUBLIC_KEYS_TABLE="pubkeys";
	private final static String KEYPAIRS_TABLE="keypairs";
	private final static String ID_MESSAGES_TABLE="idmessages";


				

	public DataBaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		//create all the DB tables

		//contacts table
		database.execSQL("CREATE TABLE "+CONTACTS_TABLE+"(nick TEXT,number TEXT PRIMARY KEY);");
		//messages table
		database.execSQL("CREATE TABLE "+MESSAGES_TABLE+"(_id INTEGER PRIMARY KEY, sender TEXT, receiver TEXT, date TIMESTAMP DEFAULT current_timestamp, message TEXT);");
		//public keys
		database.execSQL("CREATE TABLE "+PUBLIC_KEYS_TABLE+"(number TEXT,key TEXT,isValid INTEGER, PRIMARY KEY(number,isValid));");
		//keyPairs
		database.execSQL("CREATE TABLE "+KEYPAIRS_TABLE+"(pubKey TEXT,privKey TEXT,isValid INTEGER, PRIMARY KEY(pubKey,privKey));");
		//id message
		database.execSQL("CREATE TABLE "+ID_MESSAGES_TABLE+"(_id INTEGER PRIMARY KEY,idmessage TEXT);");

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int vold	, int vnew) {

		//if existe drop all tables
		database.execSQL("DROP TABLE IF EXISTS "+CONTACTS_TABLE);
		database.execSQL("DROP TABLE IF EXISTS "+UNREADMESSAGES_TABLE);
		database.execSQL("DROP TABLE IF EXISTS "+MESSAGES_TABLE);

		//create all tables again
		//contacts table
		database.execSQL("CREATE TABLE "+CONTACTS_TABLE+"(nick TEXT,number TEXT PRIMARY KEY);");
		//messages table
		database.execSQL("CREATE TABLE "+MESSAGES_TABLE+"(_id INTEGER PRIMARY KEY, sender TEXT, receiver TEXT, date INT, message TEXT);");
		//public keys
		database.execSQL("CREATE TABLE "+PUBLIC_KEYS_TABLE+"(number TEXT,key TEXT,isValid INTEGER, PRIMARY KEY(number,isValid));");
		//keyPairs
		database.execSQL("CREATE TABLE "+KEYPAIRS_TABLE+"(pubKey TEXT,privKey TEXT,isValid INTEGER, PRIMARY KEY(pubKey,privKey));");
		//id message
		database.execSQL("CREATE TABLE "+ID_MESSAGES_TABLE+"(_id INTEGER PRIMARY KEY,idmessage TEXT);");
	}


	//get all the contacts on the DB
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


	//copy arraylist contact into the db
	public static int saveContacts(ArrayList<Contact> contacts,SQLiteDatabase db){

		String query="";
		int entries=0;

		for(Contact contact:contacts){
			Cursor resultSet= db.query(CONTACTS_TABLE,null,"number=? AND nick=?",new String[]{contact.getNumber(),contact.getNick()}, null, null, null);

			if(!resultSet.moveToFirst()){
				query="INSERT INTO contacts(number,nick) VALUES ('"+contact.getNumber()+"', '"+contact.getNick()+"');";
				entries++;
				db.execSQL(query);	
			}
		}

		return entries;
	}

	//get contact nick by number
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



	public static void saveMessages(String sender,String sender_nick, String receiver, String message,SQLiteDatabase db){
		String message_timestamped="\n"+message+" [@"+sender_nick+" at "+new Time(System.currentTimeMillis())+"]\n";

		String query="INSERT INTO "+MESSAGES_TABLE+"(sender,receiver,message) VALUES('"+sender+"', '"+receiver+"','"+message_timestamped+"');";

		
		db.execSQL(query);

	}
	
	
	
	//get contact full chat
	public static String retrieveChat(String contact, SQLiteDatabase db){
		String chat="";
		Cursor res=db.query(MESSAGES_TABLE, new String[]{"message"}, "sender=? OR receiver=?",new String[]{contact,contact},null,null,"_id ASC");

		while(res.moveToNext()){
			
			
			chat+=res.getString(0);
			
		}
	
		return chat;
	}
	
	
	
	
	//obtain the conversation's last message sent or received
	public static String getLastMessages(String sender,SQLiteDatabase db){

		String last="hey! wanna chat?";
		Cursor res=db.query(MESSAGES_TABLE,new String[]{"message"},"sender=? OR receiver=?",new String[]{sender,sender},null,null,"_id DESC");

		if(res.moveToFirst()){
			
			last=res.getString(0).substring(1);
		}
		
			return last;
	}
	

	//save users keypair
	public static void saveKeyPair(KeyPair pair,SQLiteDatabase db){
		String pubKey=Base64.encodeToString(pair.getPublic().getEncoded(), false);
		String privKey=Base64.encodeToString(pair.getPrivate().getEncoded(), false);
		
		db.execSQL("INSERT INTO "+KEYPAIRS_TABLE+ "(pubKey,privKey,isValid) VALUES('"+pubKey+"', '"+privKey+"', '1');");
		
		
		
	}
	
	//retrieve user's keypair
	public static KeyPair retrieveKeyPair(SQLiteDatabase db) {
		Cursor result;
		PrivateKey privKey=null;
		PublicKey pubKeyString=null;
		KeyPair pair=null;
		
		result=db.query(KEYPAIRS_TABLE, new String[]{"pubKey","privKey"},"isValid=?" ,new String[]{"1"} ,null,null,null);
		
		if(result.moveToFirst()){
			
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
	
	public static RSAPublicKey getContactPubKey(SQLiteDatabase db,String number){
		
		RSAPublicKey pubKey=null;
		Cursor result=db.query(PUBLIC_KEYS_TABLE, new String[]{"key"}, "number=? AND isValid=?", new String[]{number,"1"}, null,null,null);

		if(result.moveToFirst()){
			
		    X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(result.getString(0)));
		    KeyFactory kf;
			try {
				kf = KeyFactory.getInstance("RSA");
				pubKey = (RSAPublicKey) kf.generatePublic(spec);

			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}catch (InvalidKeySpecException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
		}
		
		
		return pubKey;
	}
	
	
	public static void saveContactPubKey(String contact, String pubKey,SQLiteDatabase db){
		
		db.execSQL("INSERT INTO "+PUBLIC_KEYS_TABLE+" (number,key,isValid) VALUES('"+contact+"','"+pubKey+"','1');");
		
		

	
	}
	
	public static void deleteRevokedKey(String contact,SQLiteDatabase db){
		
		db.delete(PUBLIC_KEYS_TABLE, "number=? AND isValid=?", new String[]{contact,"1"});
		
		
	}
		
		
	public static void saveMessageID(String messageID,SQLiteDatabase db){
		
		db.execSQL("INSERT INTO "+ID_MESSAGES_TABLE+" (idmessage) VALUES('"+messageID+"');");
		
		
		
	}
	
	public static boolean messageIDExists(String messageID,SQLiteDatabase db){
		
		boolean exist=false;
		Cursor result=db.query(ID_MESSAGES_TABLE, null, "idmessage=?", new String[]{messageID}, null,null,null);
		
		if(result.moveToFirst()){
			exist=true;
		}
		
		
		return exist;
	}
	
	
	public static void deleteKeyPair(SQLiteDatabase db){
		
		db.delete(KEYPAIRS_TABLE, "isValid=?", new String[]{"1"});
		
	}
	
		
	
}



