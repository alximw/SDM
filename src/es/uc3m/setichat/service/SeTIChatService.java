package es.uc3m.setichat.service;



import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.conn.scheme.PlainSocketFactory;

import android.R.raw;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelService;
import es.uc3m.sdm.setichat2013.GR82.NIA100276882.NIA100276690.MainActivity;
import es.uc3m.sdm.setichat2013.GR82.NIA100276882.NIA100276690.SeTIChatConversationActivity;
import es.uc3m.sdm.setichat2013.GR82.NIA100276882.NIA100276690.R;
import es.uc3m.setichat.contactsHandling.Contact;
import es.uc3m.setichat.contactsHandling.User;
import es.uc3m.setichat.utils.Base64;
import es.uc3m.setichat.utils.DataBaseHelper;
import es.uc3m.setichat.utils.SecurityHelper;
import es.uc3m.setichat.utils.SystemHelper;
import es.uc3m.setichat.utils.XMLParser;

/**
 * This service is used to connecto to the SeTIChat server. 
 * It should remain running even if the app is not in the foreground
 *  
 * 
 * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
 * @author Jorge Blasco Al’s <jbalis@inf.uc3m.es>
 */


public class SeTIChatService extends Service implements ChannelService {

	private static final String GAE="http://setichat.appspot.com";
	private static NotificationManager notificationManager;
	private static Notification notif;


	ActivityManager am;
	// Used to communicate with the server
	ChannelAPI channel;


	public static boolean channelIsOpen=false;
	// Used to bind activities
	private final SeTIChatServiceBinder binder=new SeTIChatServiceBinder();




	public SeTIChatService() {
		Log.i("SeTIChat Service", "Service constructor");
	}


	public void connectService(){

		channel = new ChannelAPI();
		this.connect(MainActivity.myPrefs.getString("number", ""));  
		channelIsOpen=true;

	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("SeTIChat Service", "Service created");


		if(MainActivity.myPrefs==null){		MainActivity.myPrefs = getSharedPreferences("es.uc3m.setichat", MODE_PRIVATE);}

		if(!MainActivity.myPrefs.getString("number", "").equals("")){

			channel = new ChannelAPI();
			this.connect(MainActivity.myPrefs.getString("number", ""));  
			channelIsOpen=true;
		}


		binder.onCreate(this);

	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("SeTIChat Service", "Service binded");



		return(binder);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("SeTIChat Service", "Service destrotyed");
		// When the service is destroyed, the connection is closed


		try {
			channel.close();
		} catch (Exception e){
			System.out.println("Problem Closing Channel");
		}
		binder.onDestroy();    
	}


	//Methods exposed to service binders
	// Login user, send message, update public key, etc.

	// All of them are implemented with AsyncTask examples to avoid UI Thread blocks.
	public void connect(String key){
		final SeTIChatService current = this;
		class ChannelConnect extends AsyncTask<String, String, String> {

			protected String doInBackground(String... keys) {
				Log.i("Service connect", "Connect test");
				String key = keys[0];
				try {

					channel = new ChannelAPI(GAE, key, current); //Production Example

					channel.open();

				} catch (Exception e){
					System.out.println("Something went wrong...");
					Log.i("Service connect", "Error connecting..."+e.getLocalizedMessage());
				}
				return "ok";
			}

			protected void onProgressUpdate(String... progress) {
				//setProgressPercent(progress[0]);
			}

			protected void onPostExecute(String result) {
				//
			}
		}
		new ChannelConnect().execute(key,key,key);
	}


	public void sendMessage(String message){


		class SendMessage extends AsyncTask<String, String, String> {
			protected String doInBackground(String... messages) {
				Log.i("SendMessage", "send message test");

				String message = messages[0];
				Log.i("SendMessage", "Message Sent: "+message );
				try {

					channel.send(message, "/chat");
				} catch (IOException e) {
					System.out.println("Problem Sending the Message");
				}
				return "ok";
			}

			protected void onProgressUpdate(String... progress) {
				//setProgressPercent(progress[0]);
			}

			protected void onPostExecute(String result) {
				// TODO Auto-generated method stub

			}


		}
		new SendMessage().execute(message,message,message);
	}


	// Callback method for the Channel API. This methods are called by ChannelService when some kind 
	// of event happens


	/**
	 *  Called when the client is able to correctly establish a connection to the server. In this case,
	 *  the main activity is notified with a Broadcast Intent.
	 */
	@Override
	public void onOpen() {
		Log.i("onOpen", "Channel Opened");




		String intentKey = "es.uc3m.SeTIChat.CHAT_OPEN";
		Intent openIntent = new Intent(intentKey);
		// ÀWhy should we set a Package?
		openIntent.setPackage("es.uc3m.sdm.setichat2013.GR82.NIA100276882.NIA100276690");
		Context context = getApplicationContext();

		context.sendBroadcast(openIntent);  



	}

	/**
	 *  Called when the client receives a chatMessage. In this case,
	 *  the main activity is notified with a Broadcast Intent.
	 */
	@Override
	public void onMessage(String message) {
		if(MainActivity.helper==null){
			MainActivity.helper=new DataBaseHelper(getApplicationContext(), "contactsDB", null,1);
		}
		Log.i("onMessage", "Message received :"+message);
		// TODO Auto-generated method stub
		String intentKey = "es.uc3m.SeTIChat.CHAT_MESSAGE";
		String onForegroundActivity="";
		//create the intent and put the received message as extra
		Intent openIntent = new Intent(intentKey);
		openIntent.putExtra("message", message);
		openIntent.setPackage("es.uc3m.sdm.setichat2013.GR82.NIA100276882.NIA100276690");
		User user;

		//instance of XMLparser for parse the massage information
		XMLParser xpp=new XMLParser();
		//notification associated intents
		PendingIntent pending;
		Intent notifIntent;

		String chatMessage;
		String final_message;
		byte[] plainMessage = null;
		if(xpp.getTagValue(message, "encrypted").equals("true")){

			SQLiteDatabase db=MainActivity.helper.getReadableDatabase();
			user=DataBaseHelper.getUserInfo(db);
			PrivateKey myPriv=user.getPair().getPrivate();
			SecretKeySpec AESKey;


			byte[] base64Message=xpp.getTagValue(message, "chatMessage").getBytes();

			byte[] rawmessage=es.uc3m.setichat.utils.Base64.decode(base64Message);

			byte[] encryptedKey=new byte[SecurityHelper.RSAPAIR_KEY_SIZE/8];
			byte[] plainKey=new byte[16];
			byte[] iv=new byte[16];
			byte[] encryptedMessage=new byte[rawmessage.length-(encryptedKey.length+iv.length)];

			System.arraycopy(rawmessage,0,encryptedKey,0,encryptedKey.length);
			System.arraycopy(rawmessage,encryptedKey.length,iv,0, iv.length);
			System.arraycopy(rawmessage, (encryptedKey.length+iv.length), encryptedMessage,0,encryptedMessage.length);

			plainKey=SecurityHelper.publicCipher(Cipher.DECRYPT_MODE,encryptedKey, myPriv);
			if(plainKey!=null){

				AESKey=new SecretKeySpec(plainKey, "AES");
				plainMessage=SecurityHelper.AES128(Cipher.DECRYPT_MODE, AESKey, iv, SecurityHelper.SETICHAT_AES_MODE, encryptedMessage);
				final_message=new String(plainMessage)+"[DECRYPT OK]";
			}else{
				final_message="DECRYPT FAILED";
			}
			db.close();
		}else{
			chatMessage=xpp.getTagValue(message, "chatMessage");
			final_message=chatMessage;
		}
		
		
		if(xpp.getTagValue(message, "signed").equals("true")){
			String data2sign="";
			
		SQLiteDatabase db=MainActivity.helper.getReadableDatabase();
			RSAPublicKey key;
			byte[]  signature;
		if(db!=null){
			if(xpp.getTagValue(message, "encrypted").equals("true")){
				data2sign=("<idDestination>"+xpp.getTagValue(message, "idDestination")+"</idDestination>" +
					"<idMessage>"+xpp.getTagValue(message, "idMessage")+"</idMessage>"+"<content><chatMessage>"+new String(plainMessage)+"</chatMessage></content>");
			key=DataBaseHelper.getContactPubKey(db, xpp.getTagValue(message, "idSource"));
			}else{
			 data2sign=("<idDestination>"+xpp.getTagValue(message, "idDestination")+"</idDestination>" +
						"<idMessage>"+xpp.getTagValue(message, "idMessage")+"</idMessage>"+"<content><chatMessage>"+xpp.getTagValue(message, "chatMessage")+"</chatMessage></content>");
				key=DataBaseHelper.getContactPubKey(db, xpp.getTagValue(message, "idSource"));
			}
			
			
			
			if(key==null){

				final_message=final_message+"[CANNOT VERIFY SIGN DUE TO LACK OF KEY]";
				this.sendMessage(SeTIChatConversationActivity.createPublicKeyRequest(xpp.getTagValue(message, "idSource")));
			}else{
				//Log.i("[data]",data2sign);
				//Log.i("[signature]",xpp.getTagValue(message, "signature"));
				signature=Base64.decode(xpp.getTagValue(message, "signature"));
				boolean verified=SecurityHelper.verifySign(data2sign.getBytes(),signature, key);
				if(verified){

					final_message=final_message+"[SIGN OK]";
					
				}else{

					final_message=final_message+"[SIGN FAIL]";

					
				}
				
			}

			
			
			
			
			
		}else{
			throw (new SQLiteException("NULL DATABASE"));
		}
			
			
			
			
		}
		
		
		
		
		openIntent.putExtra("plainMessage", final_message);
		sendBroadcast(openIntent);

		//if the message contains a chat message...
		if(xpp.getFirstTagValue(message, "type").toString().equals("4")){






			//..we have to check which it's the activity on the foreground

			am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
			// get the info from the currently running task
			List< ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1); 
			onForegroundActivity=taskInfo.get(0).topActivity.getClassName();


			if(onForegroundActivity.equals("es.uc3m.sdm.setichat2013.GR82.NIA100276882.NIA100276690.SeTIChatConversationActivity")){

				//we are on a conversation, let it worry about the notification



			}else {

				//we are on contacts menu or out of the app

				String pendingMessage=final_message;
				String source=xpp.getTagValue(message, "idSource");
				String nick="";
				String destination=xpp.getTagValue(message, "idDestination");
				SQLiteDatabase db=MainActivity.helper.getWritableDatabase();
				if(db!=null){

					//DataBaseHelper.saveMessages(source, nick, destination, pendingMessage, db);
					nick=DataBaseHelper.getNickByNumber(source, db);
					if(nick.equals("Unknown Contact")){
						nick=source;
					}

					DataBaseHelper.saveMessages(source,nick,destination, pendingMessage, db);

				}else{

					throw(new SQLiteException("NULL DATABASE"));

				}
				db.close();




				//prepare the intents and the notification
				notifIntent=new Intent(getApplicationContext(),SeTIChatConversationActivity.class);

				notifIntent.putExtra("contact", new Contact(source));
				notifIntent.putExtra("notified", true);
				//FLAG_UPDATE_CURRENT because we don't want to create a new intent, only overwrites the existing
				pending =PendingIntent.getActivity(getApplicationContext(), 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
				notif=new Notification.Builder(getApplicationContext())
				.setContentTitle("SeTiChat")
				.setContentText("new messages from "+nick)
				.setSmallIcon(R.drawable.custom_icon)
				.setContentIntent(pending)
				.setDefaults(Notification.DEFAULT_SOUND)
				.setDefaults(Notification.DEFAULT_VIBRATE)
				.build();



				// Hide the notification after its selected
				notif.flags |= Notification.FLAG_AUTO_CANCEL;

				int id=SystemHelper.string2Integer(source);
				notificationManager.notify(id, notif);

			}

			//if key download message
		}else if(xpp.getFirstTagValue(message, "type").toString().equals("8")){ 

			SQLiteDatabase db=MainActivity.helper.getWritableDatabase();
			String pubKey=xpp.getTagValue(message, "key");
			String source=xpp.getTagValue(message, "mobile");


			if(db!=null){

				DataBaseHelper.saveContactPubKey(source, pubKey, db);


			}else{

				throw (new SQLiteException("NULL DATABASE"));
			}
			db.close();


			//if key revokation message
		}else if(xpp.getFirstTagValue(message, "type").toString().equals("7")){
			String contact=xpp.getTagValue(message, "revokedMobile");
			SQLiteDatabase db=MainActivity.helper.getWritableDatabase();
			if(db!=null){
				DataBaseHelper.deleteRevokedKey(contact,db);
				this.sendMessage(SeTIChatConversationActivity.createPublicKeyRequest(contact));
				db.close();
			}else{
				throw (new SQLiteException("NULL DATABASE"));
			}





		}

	}


	public ChannelAPI getOpenedChannel(){

		return this.channel;
	}

	@Override
	public void onClose() {
		// Called when the connection is closed

	}


	@Override
	public void onError(Integer errorCode, String description) {
		// Called when there is an error in the connection

	}

}
