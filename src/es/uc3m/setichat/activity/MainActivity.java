package es.uc3m.setichat.activity;





import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;
import es.uc3m.setichat.R;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.service.SeTIChatServiceBinder;
import es.uc3m.setichat.utils.DataBaseHelper;

/**
 * This is the main activity and its used to initialize all the SeTIChat features. 
 * It configures the three tabs used in this preliminary version of SeTIChat.
 * It also start the service that connects to the SeTIChat server.
 * 
 * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
 * @author Jorge Blasco Alis <jbalis@inf.uc3m.es>
 */

public class MainActivity extends FragmentActivity implements ActionBar.TabListener {


	// Service used to access the SeTIChat server
	private SeTIChatService mService;
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	//BigInteger used for generate a 16bytes random number used as messageID
	private BigInteger bigIn;
	

	/*we need to find out if we are running the app 
	 *for 1st time, so we need a variable. This will be 
	 *updated from SignUpActivity.java, that's why is tagged as protected.
	 * (i'm a very  elegant Java programmer and i respect the encapsulation
	 * as much as a i can :-) )
	 */
	protected static SharedPreferences myPrefs=null;
	public static DataBaseHelper helper;
	// Receivers that wait for notifications from the SeTIChat server
	private BroadcastReceiver openReceiver;
	private BroadcastReceiver chatMessageReceiver;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getApplicationContext().deleteDatabase("contactsDB");
		myPrefs = getSharedPreferences("es.uc3m.setichat", MODE_PRIVATE);
		helper=new DataBaseHelper(getApplicationContext(), "contactsDB", null,1);

		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section1)
				.setTabListener(this));

		Log.i("Activty", "onCreate");

		try{

			// Make sure the service is started.  It will continue running
			// until someone calls stopService().  The Intent we use to find
			// the service explicitly specifies our service component, because
			// we want it running in our own process and don't want other
			// applications to replace it.
			startService(new Intent(MainActivity.this,
					SeTIChatService.class));

		}catch(Exception e){

			Log.d("MainActivity", "Unknown Error", e);

			stopService(new Intent(MainActivity.this,
					SeTIChatService.class));
		}


		// Create and register broadcast receivers
		IntentFilter openFilter = new IntentFilter();
		openFilter.addAction("es.uc3m.SeTIChat.CHAT_OPEN");

		openReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Context context1 = getApplicationContext();
				CharSequence text = "SeTIChatConnected";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context1, text, duration);
				toast.show();
			}
		};

		registerReceiver(openReceiver, openFilter);

		chatMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				//do something based on the intent's action
		
					//if type=3 ->write to DB


			}
		};

		IntentFilter chatMessageFilter = new IntentFilter();
		chatMessageFilter.addAction("es.uc3m.SeTIChat.CHAT_MESSAGE");
		registerReceiver(chatMessageReceiver, chatMessageFilter);


		if (mService == null) {
			// Binding the activity to the service to get shared objects

			bindService(new Intent(MainActivity.this,
					SeTIChatService.class), mConnection,
					Context.BIND_AUTO_CREATE);
		}




	}




	@Override
	public void onDestroy() {
		super.onDestroy();
		// We stop the service if activity is destroyed
		stopService(new Intent(MainActivity.this,
				SeTIChatService.class));
		// We also unregister the receivers to avoid leaks.
		unregisterReceiver(chatMessageReceiver);
		unregisterReceiver(openReceiver);
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onResume() {
		Log.v("MainActivity", "onResume: Resuming activity...");
		super.onResume();

		//only for debug purposes
		myPrefs.edit().putBoolean("firstrun", false).commit();
		myPrefs.edit().putString("token","BD26B8225CE2F0F9B57F9E2878D29916").commit();


		if (myPrefs.getBoolean("firstrun", true)) {

			/*
			 * 
			 *  /_\ we should  reach this code only once /_\
			 *
			 */


			//launch an intent, wake up SignUpActivity (should define it in the manifest too)

			Intent toSignUp=new Intent("es.uc3m.setichat.activity.SIGNUPACTIVITY");
			this.startActivity(toSignUp);




		}else if(!myPrefs.getBoolean("firstrun", true)){





		}



		ContactsFragment fragment = new ContactsFragment();
		getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

	}




	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current tab position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, show the tab contents in the
		// container view.
		ContactsFragment fragment = new ContactsFragment();
		getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public void update(){

	}

	public void showException(Throwable t) {
		AlertDialog.Builder builder=new AlertDialog.Builder(this);

		builder
		.setTitle("Exception!")
		.setMessage(t.toString())
		.setPositiveButton("OK", null)
		.show();
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			Log.i("Service Connection", "Estamos en onServiceConnected");
			SeTIChatServiceBinder binder = (SeTIChatServiceBinder) service;
			mService = binder.getService();

			//we have to do 2 things, in first place we have to check if there are new messages
			//TODO: check for new messages


			//in second place we have to update the contacts database
			askForContacts();


		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};

	public  void askForContacts(){
		ArrayList<String> contacts=new ArrayList<String>();  
		
		String mobileList="";

		Cursor phones = getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
		while (phones.moveToNext())
		{

			String number=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			number=  number.replaceAll("-", "").replaceAll(" ", "");

			//remove duplicated entries
			if(!contacts.contains(number)){
			contacts.add(number);

			}
	
		}
		
		//build contactRequest  xml message
		
		for(String contact:contacts){
			mobileList=mobileList+"<mobile>"+contact+"</mobile>";
		}
	
		bigIn=new BigInteger(128, new Random());
		String header="<?xml version="+ " \"1.0\" "+ "encoding="+" \"UTF-8\" "+"?>"+
		"<message><header>"+
		"<idSource>D29DB3F342358F9D65A7D5F12684F396</idSource>"+
		"<idDestination>setichat@appspot.com</idDestination>"+
		"<idMessage>"+bigIn.toString(16)+"</idMessage>"+
		"<type>2</type>"+
		"<encrypted>false</encrypted>"+
		"<signed>false</signed>"+
		"</header>";
		String content="<content><mobileList>"+
		mobileList+
		"</mobileList></content></message>";

		
		//and send it
		mService.sendMessage(header+content);

	}

	public SeTIChatService getService() {
		// TODO Auto-generated method stub
		return mService;
	}

	//SeTIChatServiceDelegate Methods

	public void showNotification(String message){
		Context context = getApplicationContext();
		CharSequence text = message;
		int duration = Toast.LENGTH_SHORT;


	}


}
