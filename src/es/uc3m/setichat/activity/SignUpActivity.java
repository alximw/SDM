package es.uc3m.setichat.activity;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Random;

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
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import es.uc3m.setichat.R;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.service.SeTIChatServiceBinder;
import es.uc3m.setichat.utils.Base64;
import es.uc3m.setichat.utils.DataBaseHelper;
import es.uc3m.setichat.utils.SecurityHelper;
import es.uc3m.setichat.utils.XMLParser;

public class SignUpActivity extends Activity implements OnClickListener {




	//views for the GUI
	TextView Nick=null,NIA=null;
	Button bt=null;
	String number="";
	String nick="";
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
					//...and save it on the sharedPreferences object as "token"
					MainActivity.myPrefs.edit().putString("token", userToken).commit();
					Log.d("[debug]","Token received= "+userToken);
					
					
					
					new MyTask().execute(new String[0]);
					
					
					
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

			this.unregisterReceiver(messageReceiver);
			unbindService(mConnection);
	
	}
	







	@Override
	public void onClick(View v) {

		if(v.getId()==bt.getId()){
			//send the signup message
			number=NIA.getText().toString();
			nick=Nick.getText().toString();
			MainActivity.myPrefs.edit().putString("number", number ).commit();
			mService.connectService();
			mService.sendMessage(createSignUpMessage());
		}

	}
	
	
	
	private class MyTask extends AsyncTask<String, Float, Integer>{
		 
        protected void onPreExecute() {
            
         }

         protected Integer doInBackground(String... urls) {
			
        	KeyPair pair=SecurityHelper.generateRSAKeyPair(SecurityHelper.RSAPAIR_KEY_SIZE);
        	SQLiteDatabase db=MainActivity.helper.getWritableDatabase();
        	if(db!=null){
        	DataBaseHelper.saveKeyPair(pair,db);
        	}else{
        		
        		throw (new SQLiteException("NULL DATABASE"));
        	}
        	mService.sendMessage(createKeyUploadMessage(pair.getPublic()));
        	 
        	 return 0;
         }

         protected void onProgressUpdate (Float... valores) {
             
         }

         protected void onPostExecute(Integer bytes) {
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


		String header="<?xml version="+ " \"1.0\" "+ "encoding="+" \"UTF-8\" "+"?>" +
				"<message><header><idSource>"+MainActivity.myPrefs.getString("token", "")+"</idSource>"+
				"<idDestination>setichat@appspot.com</idDestination>"+
				"<idMessage>"+new BigInteger(128, new Random()).toString(16)+"</idMessage>"+
				"<type>9</type>"+
				"<encrypted>false</encrypted>"+
				"<signed>false</signed></header>";
		
		String content="<content><upload>"+
				"<key>"+ new String(Base64.encodeToString(key.getEncoded(), false))+"</key>"+
				"<type>public</type>"+
				"</upload></content></message>";


		return (header+content);

	}


}


