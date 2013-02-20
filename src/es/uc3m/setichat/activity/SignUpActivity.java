package es.uc3m.setichat.activity;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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

public class SignUpActivity extends Activity implements OnClickListener {

	
	
	
	//views for the GUI
	TextView email=null,nick=null,NIA=null;
	Button bt=null;
	
	
	/*
	 * In order to be able of know if we have received the OK  or 
	 * the FAIL message, we need to "subscribe" the activity to the 
	 * proper intent..
	 */
private BroadcastReceiver messageReceiver;	

	
	
//we need a service instance since we need send/receive message from this activity
private SeTIChatService mService;
	
//and a ServiceConection too! because we need to instantiate the server (this is CTRL+C + CTRL+V from MainActivity)
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

		
			Log.d("SeTIChatConversationActivity",
					"onServiceDisconnected: un-bounding the service");

		mService = null;
		Toast.makeText(SignUpActivity.this, "Disconnected", // R.string.local_service_disconnected,
				Toast.LENGTH_SHORT).show();
	}
};





	
	
	protected void onCreate(Bundle savedInstanceState) {
	
	/*
		IntentFilter MessageFilter = new IntentFilter();
		MessageFilter.addAction("es.uc3m.SeTIChat.CHAT_MESSAGE");
		registerReceiver(messageReceiver, MessageFilter);
		messageReceiver=new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				
				
				//TODO: check if the kind of message is the correct (OK or FAIL on the signUp process)
				intent.getExtras().getCharSequence("message");
			/*
			 * if message.type==OK
			 * 	set false some kind of app-global variable
			 MainActivity.myPrefs.edit().putBoolean("firstun", false);
*/
		
				//MainActivity.myPrefs.edit().putBoolean("firstun", false);
			Log.v("[debug]","we have received a message on  SignUpActivity");
			
			
			
			/*
			 *	launch an intent to the main activity 
			 *
			 *startActivity(new Intent("android.intent.action.main"));
			 *
			 *Log.d("[debug]","we have receive a OK message from setichat server.you are SignedUp now");
			 */
			
			
			
			
			/*
			 *else
			 *  DO NOTHING
			 */
			
		
			
			/*
			}

		};
		*/
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup_layout);
	
		Log.d("[debug]","we have reached SignUpActivity");
		
	email=(TextView)findViewById(R.id.tv_Email);
	nick=(TextView)findViewById(R.id.tv_Nick);
	NIA=(TextView)findViewById(R.id.tv_Nia);
	
	bt=(Button)findViewById(R.id.bt_Send);
	bt.setOnClickListener(this);
	

	if (mService == null) {
		// Binding the activity to the service to get shared objects
		
		bindService(new Intent(SignUpActivity.this,
				SeTIChatService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	}

	
	
	@Override
	public void onClick(View v) {

		if(v.getId()==bt.getId()){
			
			//TODO:find a way to "generate" the xml message
			
			//generate the 16-bytes random integer used for idMessage field
			//using SecureRandom would be better, find out how!!!!!!
			BigInteger b=new BigInteger(128, new Random());
			
			
			String header="<header><idSource>"+NIA.getText().toString()+"</idSource>"+
			"<idDestination>setichat@appspot.com</idDestination>"+
			"<idMessage>"+b.toString(16)+"</idMessage>"+
			"<type>1</type>"+
			"<encrypted>false</encrypted>"+
			"<signed>false</signed></header>";
			String content="<content><signup>"+
			"<nick>"+nick.getText().toString()+"</nick>"+
			"<mobile>"+NIA.getText().toString()+"</mobile>"+
			"</signup></content>";
		
			
			
			Log.d("[MESSAGE SENT]", header+content);
			mService.sendMessage("<?xml version="+ " \"1.0\" "+ "encoding="+" \"UTF-8\" "+"?>"+"<message>"+header+content+"</message>");
			
			
		}
		
		
		
	}
	












}


