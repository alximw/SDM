package es.uc3m.setichat.utils;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.Cipher;

import es.uc3m.sdm.setichat2013.GR82.NIA100276882.NIA100276690.MainActivity;
import es.uc3m.setichat.contactsHandling.Contact;
import es.uc3m.setichat.contactsHandling.User;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DataBaseHelper extends SQLiteOpenHelper {

	private final static String CONTACTS_TABLE="contacts";
	private final static String UNREADMESSAGES_TABLE="unreadmessages";
	private final static String MESSAGES_TABLE="messages";
	private final static String PUBLIC_KEYS_TABLE="pubkeys";
	private final static String KEYPAIRS_TABLE="keypairs";
	private final static String USERS_TABLE="user";


				

	public DataBaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		//create all the DB tables

		//users table
		database.execSQL("CREATE TABLE "+USERS_TABLE+"(nick TEXT,number TEXT PRIMARYKEY,token TEXT,public_key TEXT,private_key TEXT,salt TEXT,hash TEXT);");
		//contacts table
		database.execSQL("CREATE TABLE "+CONTACTS_TABLE+"(nick TEXT,number TEXT PRIMARY KEY);");
		//messages table
		database.execSQL("CREATE TABLE "+MESSAGES_TABLE+"(_id INTEGER PRIMARY KEY, sender TEXT, receiver TEXT, date TIMESTAMP DEFAULT current_timestamp, message TEXT);");
		//public keys
		database.execSQL("CREATE TABLE "+PUBLIC_KEYS_TABLE+"(number TEXT,key TEXT,isValid INTEGER, PRIMARY KEY(number,isValid));");
		

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int vold	, int vnew) {

		//if existe drop all tables
		database.execSQL("DROP TABLE IF EXISTS "+CONTACTS_TABLE);
		database.execSQL("DROP TABLE IF EXISTS "+UNREADMESSAGES_TABLE);
		database.execSQL("DROP TABLE IF EXISTS "+MESSAGES_TABLE);

		//create all tables again
		//users table
		database.execSQL("CREATE TABLE "+USERS_TABLE+"(nick TEXT,number TEXT PRIMARYKEY,token TEXT,public_key TEXT,private_key TEXT,hash TEXT);");
		//contacts table
		database.execSQL("CREATE TABLE "+CONTACTS_TABLE+"(nick TEXT,number TEXT PRIMARY KEY);");
		//messages table
		database.execSQL("CREATE TABLE "+MESSAGES_TABLE+"(_id INTEGER PRIMARY KEY, sender TEXT, receiver TEXT, date INT, message TEXT);");
		//public keys
		database.execSQL("CREATE TABLE "+PUBLIC_KEYS_TABLE+"(number TEXT,key TEXT,isValid INTEGER, PRIMARY KEY(number,isValid));");
		//keyPairs
		
	}


	
  //////////////////////////////////////////
 ////		CONTACTS METHOD			   ////
//////////////////////////////////////////	
	//get all the contacts on the DB
	public static ArrayList<Contact> contactsToArray(SQLiteDatabase db){

		ArrayList<Contact> list=new ArrayList<Contact>();
		Cursor resultset;

		resultset=db.query(CONTACTS_TABLE,null,null,null, null, null, null);

		if(resultset.moveToFirst()){
			//the result is not empty

			do{
				if(MainActivity.key==null){
				list.add(new Contact( resultset.getString(0),"UNAVAILABLE NUMBER"));
				}else{
					
					byte[] encrypted_contact=Base64.decode(resultset.getString(1));
					String plain_contact=new String(SecurityHelper.AES128(Cipher.DECRYPT_MODE, MainActivity.key, encrypted_contact));
					list.add(new Contact( resultset.getString(0),plain_contact));

					
				}
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
		byte[] encrypted_contact=null;
		String encoded_number="";
		for(Contact contact:contacts){
			encrypted_contact=SecurityHelper.AES128(Cipher.ENCRYPT_MODE, MainActivity.key,contact.getNumber().getBytes());
		encoded_number=Base64.encodeToString(encrypted_contact, false);
			Cursor resultSet= db.query(CONTACTS_TABLE,null,"number=? AND nick=?",new String[]{encoded_number,contact.getNick()}, null, null, null);

			if(!resultSet.moveToFirst()){
				if(MainActivity.key!=null){
				encrypted_contact=SecurityHelper.AES128(Cipher.ENCRYPT_MODE, MainActivity.key,contact.getNumber().getBytes());
				encoded_number=Base64.encodeToString(encrypted_contact, false);
				
				query="INSERT INTO contacts(number,nick) VALUES ('"+encoded_number+"', '"+contact.getNick()+"');";
				entries++;
				db.execSQL(query);	
				}else{
					query="INSERT INTO contacts(number,nick) VALUES ('"+contact.getNumber()+"', '"+contact.getNick()+"');";
					entries++;
					db.execSQL(query);	
					
				}
			}
		}

		return entries;
	}

	//get contact nick by number
	public static String getNickByNumber(String number,SQLiteDatabase db){
		String nick="";
		Cursor resultSet=null;
		if(MainActivity.key==null){
		
		resultSet= db.query(CONTACTS_TABLE,new String[]{"nick"},"number=?",new String[]{number}, null, null, null);

		
		}else{
			String encoded_number=Base64.encodeToString(SecurityHelper.AES128(Cipher.ENCRYPT_MODE, MainActivity.key, number.getBytes()), false);
			resultSet= db.query(CONTACTS_TABLE,new String[]{"nick"},"number=?",new String[]{encoded_number}, null, null, null);

		}
		if(resultSet.moveToFirst()){
			nick=resultSet.getString(0);
		}else{
			nick="Unknown Contact";
		}

		return nick;
	}
//####################################################################################################	
	
	
	  //////////////////////////////////////////
	 ////		CONVERSATION METHODS	   ////
	//////////////////////////////////////////
	
	public static void saveMessages(String sender,String sender_nick, String receiver, String message,SQLiteDatabase db){
		String message_timestamped="\n"+message+" [@"+sender_nick+" at "+new Time(System.currentTimeMillis())+"]\n";
		byte[] encrypted_message;
		String query="";
		if(MainActivity.key==null){
			query="INSERT INTO "+MESSAGES_TABLE+"(sender,receiver,message) VALUES('"+sender+"', '"+receiver+"','"+message_timestamped+"');";
	
		}else{
			encrypted_message=SecurityHelper.AES128(Cipher.ENCRYPT_MODE, MainActivity.key, message_timestamped.getBytes());
			 query="INSERT INTO "+MESSAGES_TABLE+"(sender,receiver,message) VALUES('"+sender+"', '"+receiver+"','"+Base64.encodeToString(encrypted_message, false)+"');";

		}
		
		
		db.execSQL(query);

	}
	
	//get contact full chat
	public static String retrieveChat(String contact, SQLiteDatabase db){
		String chat="";
		Cursor res=db.query(MESSAGES_TABLE, new String[]{"message"}, "sender=? OR receiver=?",new String[]{contact,contact},null,null,"_id ASC");

		while(res.moveToNext()){
			
			if(MainActivity.key==null){
			//chat+=res.getString(0);
				chat+="LOCKED DATABASE\n";
			}else{
				byte[] encrypted_message=Base64.decode(res.getString(0));
				String plainMessage=new String(SecurityHelper.AES128(Cipher.DECRYPT_MODE, MainActivity.key, encrypted_message));
				
				chat+=plainMessage;

			}
		}
	
		return chat;
	}
	

	//obtain the conversation's last message sent or received
	public static String getLastMessages(String sender,SQLiteDatabase db){

		String last="hey! wanna chat?";
		Cursor res=db.query(MESSAGES_TABLE,new String[]{"message"},"sender=? OR receiver=?",new String[]{sender,sender},null,null,"_id DESC");

		if(res.moveToFirst()){
			
			if(MainActivity.key==null){
				
				last="LOCKED DATABASE";
			}else{
				byte[] encrypted_message=Base64.decode(res.getString(0));
				String plainMessage=new String(SecurityHelper.AES128(Cipher.DECRYPT_MODE, MainActivity.key	, encrypted_message));
				last=plainMessage.substring(1);
				//last=res.getString(0).substring(1);

			}
			
		}
		
			return last;
	}
//###################################################################################################
	
	
	
	
	  //////////////////////////////////////////
	 ////		KEYS METHODS			   ////
	//////////////////////////////////////////
	
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
		
//###################################################################################################

	
	
	
	  //////////////////////////////////////////
	 ////		USER METHODS			   ////
	//////////////////////////////////////////
	
	public static void saveUser(User user, SQLiteDatabase db){
		
		String SQLQuery="INSERT INTO "+USERS_TABLE+" (nick,number,token) VALUES('"+user.getNick()+"','"+user.getNumber()+"','"+user.getToken()+"');";
		Log.i("query",SQLQuery);
		
		db.execSQL(SQLQuery);
		
	}
	
	public static void updateUser(String hash,String salt,String pubkey,String privkey, SQLiteDatabase db){
		String query="";
		
		
		if(hash!=null){
			Log.i("[DATABASE]","updating user's password hash");
			query="UPDATE "+USERS_TABLE+" SET ";
			query+="hash='"+hash+"';";
			Log.i("[DATABSE]","Update query:"+ query);
			db.execSQL(query);
		}if(salt!=null){
			Log.i("[DATABASE]","updating user's salt");
			query="UPDATE "+USERS_TABLE+" SET ";
			query+="salt='"+salt+"';";
			Log.i("[DATABSE]","Update query:"+ query);

			db.execSQL(query);


			
		}if(pubkey!=null && privkey!=null){
			Log.i("[DATABASE]","updating user's keypair");
			query="UPDATE "+USERS_TABLE+" SET ";
			query+="public_key='"+pubkey+"'";
			query+=", private_key='"+privkey+"';";
			Log.i("[DATABSE]","Update query:"+ query);

			db.execSQL(query);
			
		}

		
		
	}
	
	public static User getUserInfo(SQLiteDatabase db){
		PrivateKey privKey=null;
		PublicKey pubKeyString=null;
		KeyPair pair=null;
		User user=null;
		byte[] plain_token=null;
		byte[] encrypted_privKey;
		byte[] privKey_encoded;
		
		
		Cursor result=db.query(USERS_TABLE, null, null, null, null, null, null);
		if(result.moveToFirst()){
		
		byte[] token_raw=Base64.decode(result.getString(2));
		if(MainActivity.key!=null){
		plain_token=SecurityHelper.AES128(Cipher.DECRYPT_MODE,MainActivity.key, token_raw);
		}else{
		plain_token="TOKEN NOT AVAILABLE".getBytes();
		}
		user=new User(result.getString(0), result.getString(1), new String(plain_token));
		user.setSalt(result.getString(5));
		user.setHash(result.getString(6));
		
		try{
			//  get public key
				
			
			    X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(result.getString(3)));
			    KeyFactory kf = KeyFactory.getInstance("RSA");
			    pubKeyString = kf.generatePublic(spec);

			//get Private Key
			    encrypted_privKey =Base64.decode(result.getString(4));
			    privKey_encoded=SecurityHelper.AES128(Cipher.DECRYPT_MODE, MainActivity.key, encrypted_privKey);
			    
			    PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(privKey_encoded);
			     privKey = kf.generatePrivate(specPriv);
				}catch(Exception e){
					e.printStackTrace();
					
				}
				
				pair =new KeyPair(pubKeyString, privKey);
				user.setPair(pair);
				
			}
		
		
		
		
		
		
		return user;
	}
	
//###################################################################################################
		
	
}



