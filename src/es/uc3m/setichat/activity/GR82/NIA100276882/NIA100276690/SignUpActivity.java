package es.uc3m.setichat.activity.GR82.NIA100276882.NIA100276690;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.Random;

import javax.crypto.Cipher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import es.uc3m.setichat.activity.GR82.NIA100276882.NIA100276690.R;
import es.uc3m.setichat.contactsHandling.User;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.service.SeTIChatServiceBinder;
import es.uc3m.setichat.utils.Base64;
import es.uc3m.setichat.utils.DataBaseHelper;
import es.uc3m.setichat.utils.SecurityHelper;
import es.uc3m.setichat.utils.XMLParser;

public class SignUpActivity extends Activity implements OnClickListener{




	//views for the GUI
	TextView Nick=null,NIA=null,psw=null;
	Button bt=null;
	String number="";
	String nick="";
	String user_psw="";
	
	
	
	/*
	 * In order to be able of know if we have received the OK  or 
	 * the FAIL message, we need to "subscribe" the activity to the 
	 * proper intent..
	 */
	private BroadcastReceiver messageReceiver;	
	private XMLParser xpp;

	//we need a service instance since we need send/receive message from this activity
	private SeTIChatService mService;

	//used for save the 16-bit randomly-generated numbers
	BigInteger b;

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mService = SeTIChatServiceBinder.getService();




		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.


			Log.d("SeTIChatSignUPActivity",
					"onServiceDisconnected: un-bounding the service");

			mService = null;
			Toast.makeText(SignUpActivity.this, "Disconnected", // R.string.local_service_disconnected,
					Toast.LENGTH_SHORT).show();
		}
	};


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup_layout);	
		Log.d("[debug]","we have reached SignUpActivity");
		xpp=new XMLParser();

		
		IntentFilter MessageFilter = new IntentFilter();
		MessageFilter.addAction("es.uc3m.SeTIChat.CHAT_MESSAGE");
		messageReceiver=new BroadcastReceiver(){

			@Override

			public void onReceive(Context context, Intent intent) {
				Log.d("[debug]","we have received a message on  SignUpActivity");
				String receivedMessage=intent.getExtras().getCharSequence("message").toString();
				String msgRespCode=xpp.getTagValue(receivedMessage, "responseCode");


				if(msgRespCode.equals("201")){
					Log.d("[debug]","OK message on SingUpActivity");
					
					//change the value of firstrun control variable
					MainActivity.myPrefs.edit().putBoolean("first", false).commit();
					//get the returned user token...
					String userToken=xpp.getTagValue(receivedMessage, "responseMessage");
					Log.d("[SIGNUP]","Token received= "+userToken);
					//generate the user salt
					byte[] salt=SecurityHelper.generateSalt();
					Log.i("[CRYPTO]","generated new user salt: "+new BigInteger(salt).toString(16));
					//derive the local key from the typed password
					MainActivity.key=SecurityHelper.derivePassword(user_psw, salt);
					Log.i("[CRYPTO]","derived new user key: "+new BigInteger(MainActivity.key.getEncoded()).toString(16));
		        	//obtain the local key double sha1 hash 
					String password_hash=SecurityHelper.generateSHA1Hash(MainActivity.key.getEncoded());
		        	String password_doubleHash=SecurityHelper.generateSHA1Hash(password_hash.getBytes());
		        	Log.i("[CRYPTO]"," derived user's password double SHA1 hash "+password_doubleHash);
					
		        	//generate new keypair
		        	KeyPair pair=SecurityHelper.generateRSAKeyPair(SecurityHelper.RSAPAIR_KEY_SIZE);
		        	//encrypt the new private key
		        	PrivateKey priv_key= pair.getPrivate();		        	
		        	String encrypted_privateKey=Base64.encodeToString(SecurityHelper.AES128(Cipher.ENCRYPT_MODE, MainActivity.key, priv_key.getEncoded()), false);
					//encode the new public key as base64
		        	String plain_publicKey=Base64.encodeToString(pair.getPublic().getEncoded(), false);
					
					//encrypt the user's token using user's key
					String encryptedToken=Base64.encodeToString(SecurityHelper.AES128(Cipher.ENCRYPT_MODE,MainActivity.key, userToken.getBytes()),false);
		        	Log.i("[CRYPTO]", "Encrypted and codified token: "+encryptedToken);
		        	
		        	User user=new User(nick,number,encryptedToken);
					
					SQLiteDatabase db=MainActivity.helper.getWritableDatabase();
					
					if(db!=null){
						//create a new user register in the DB
						DataBaseHelper.saveUser(user, db);		
						//update the user data with the user salt and the key  SHA1 hash 
						DataBaseHelper.updateUser(password_doubleHash,Base64.encodeToString(salt, false),plain_publicKey,encrypted_privateKey, db);
						db.close();
						
					}
					//upload the new key
		        	mService.sendMessage(createKeyUploadMessage(pair.getPublic()));

					//write and launch the intent
					Intent toMainActivity=new Intent(getApplicationContext(), MainActivity.class);
					Log.d("[debug]","Back to MAinActivity");
					startActivity(toMainActivity);
					finish();
				
				}
				
				
			}
		};
		registerReceiver(messageReceiver, MessageFilter);


		//initialize all the UI views
		Nick=(TextView)findViewById(R.id.tv_Nick);
		NIA=(TextView)findViewById(R.id.tv_Nia);
		bt=(Button)findViewById(R.id.bt_Send);
		psw=(TextView)findViewById(R.id.tv_pasw);
		if(MainActivity.myPrefs.getBoolean("first", false)){
			bt.setClickable(false);
		}
		bt.setOnClickListener(this);

	}



	@Override
	protected void onStart() {
		
		super.onStart();
	
		bindService(new Intent(SignUpActivity.this,
				SeTIChatService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	
	
	}



	@Override
	protected void onResume() {

		super.onResume();
		if(MainActivity.myPrefs.getBoolean("first", false)){
			bt.setClickable(false);
		}
	}



	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
			unregisterReceiver(messageReceiver);
			unbindService(mConnection);
	
	}
	







	@Override
	public void onClick(View v) {

	
		if(v.getId()==bt.getId()){
			//send the signup message
			number=NIA.getText().toString();
			nick=Nick.getText().toString();
			user_psw=psw.getText().toString();
			
			if(!number.matches("") && !nick.matches("")&& !user_psw.matches("")){
			MainActivity.myPrefs.edit().putString("number", number ).commit();
			mService.connectService();
			mService.sendMessage(createSignUpMessage());
			}


		}

	}
	
	
	
	
	/////////////////////////////////////////
	///			MESSAGE CREATION          ///
	///			 	METHODS              ///
	/////////////////////////////////////////	


	//generate the 16-bytes random integer used for idMessage field


	public  String createSignUpMessage(){
		b=new BigInteger(128, new Random());


		String header="<?xml version="+ " \"1.0\" "+ "encoding="+" \"UTF-8\" "+"?>" +
				"<message><header><idSource>"+NIA.getText().toString()+"</idSource>"+
				"<idDestination>setichat@appspot.com</idDestination>"+
				"<idMessage>"+b.toString(16)+"</idMessage>"+
				"<type>1</type>"+
				"<encrypted>false</encrypted>"+
				"<signed>false</signed></header>";
		String content="<content><signup>"+
				"<nick>"+nick+"</nick>"+
				"<mobile>"+NIA.getText().toString()+"</mobile>"+
				"</signup></content></message>";


		return (header+content);

	}
	
	public  static String createKeyUploadMessage(PublicKey key){
		String header="",content="";
		SQLiteDatabase db=MainActivity.helper.getReadableDatabase();
		if(db!=null){
			User user=DataBaseHelper.getUserInfo(db);
			if(user!=null){
				Log.i("[MAINACTIVITY]",user.toString());

				header="<?xml version="+ " \"1.0\" "+ "encoding="+" \"UTF-8\" "+"?>" +
						"<message><header><idSource>"+user.getToken()+"</idSource>"+
						"<idDestination>setichat@appspot.com</idDestination>"+
						"<idMessage>"+new BigInteger(128, new Random()).toString(16)+"</idMessage>"+
						"<type>9</type>"+
						"<encrypted>false</encrypted>"+
						"<signed>false</signed></header>";

				content="<content><upload>"+
						"<key>"+ new String(Base64.encodeToString(key.getEncoded(), false))+"</key>"+
						"<type>public</type>"+
						"</upload></content></message>";
			}
			db.close();
		}




		return (header+content);

	}


}


