package es.uc3m.setichat.activity;





import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import es.uc3m.setichat.R;
import es.uc3m.setichat.contactsHandling.Contact;
import es.uc3m.setichat.contactsHandling.ContactsAdapter;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.service.SeTIChatServiceBinder;
import es.uc3m.setichat.utils.DataBaseHelper;
import es.uc3m.setichat.utils.SecurityHelper;
import es.uc3m.setichat.utils.SystemHelper;
import es.uc3m.setichat.utils.XMLParser;

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
	//the contact list that will be show
	ArrayList<Contact> contacts;

	/*we need to find out if we are running the app 
	 *for 1st time, so we need a place in wich 
	 *we could save this information. 
	 */
	public static SharedPreferences myPrefs=null;

	/*
	 * this object makes easier the interaction with the DB
	 */
	public static DataBaseHelper helper;

	//database instance used on thsi activity
	private SQLiteDatabase database;

	// Receivers that wait for notifications from the SeTIChat server
	private BroadcastReceiver openReceiver;
	private BroadcastReceiver chatMessageReceiver;

	//xml parser used for extract information from setichatmessages
	private XMLParser xpp;


	/*
	 * THis method will be executed the first time the app is open
	 * and won't be executed again until the OS called the onDestroy method
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//here we initialize the shared preferences object
		myPrefs = getSharedPreferences("es.uc3m.setichat", MODE_PRIVATE);

		//here we make the connection with the DB
		helper=new DataBaseHelper(getApplicationContext(), "contactsDB", null,1);

		//create a new parser object
		xpp=new XMLParser();


		// Set up the action bar to show tabs.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText("Contacts")
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


		/*
		 *  Create and register broadcast receivers.
		 *  Will be used for "intercept" the intents
		 *  launched by the service when a setichatMessage
		 *   is received.
		 */

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

			/*
			 * this method will be executed when an intent wit 
			 * key=es.uc3m.SeTIChat.CHAT_MESSAGE
			 * be launched.
			 */

			public void onReceive(Context context, Intent intent) {
				//we have received a message! extract the content

				String message=intent.getCharSequenceExtra("message").toString();
				//do something based on the message type

				/*
				 * if type=3 -> the message is a
				 * contact request answer and 
				 * contains the registered contacts
				 * we have to get them and  save on the DB 
				 */

				if(xpp.getTagValue(message, "type").toString().equals("3")){				

					//get the contacts...
					contacts=xpp.retrieveContacts2(message);
					//create a new db instance,notice that we create it as writable
					database=MainActivity.helper.getWritableDatabase();

					//chek if database is null
					if(!(database==null)){
						//write the contacts
						DataBaseHelper.saveContacts(contacts, database);

					}else{

						throw(new SQLiteException("NULL DATABASE"));

					}
					//close the database
					database.close();

					//refresh the adapter with new contacts the same way we do on ContactFragment.java
					ContactsAdapter updated_adapter=new ContactsAdapter(getApplicationContext(),R.layout.custom_lw_layout,contacts);
					((ListFragment)(getFragmentManager().findFragmentById(R.id.container)) ).setListAdapter(updated_adapter);
				}
			}
		};

		//register the receiver
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






	protected void onResume() {
		Log.v("MainActivity", "onResume: Resuming activity...");
		super.onResume();


		if (mService == null) {
			// Binding the activity to the service to get shared objects

			bindService(new Intent(MainActivity.this,
					SeTIChatService.class), mConnection,
					Context.BIND_AUTO_CREATE);

		}

		//only for debug purposes, comment it for normal execution
		myPrefs.edit().putBoolean("first", false).commit();
		myPrefs.edit().putString("token","D29DB3F342358F9D65A7D5F12684F396").commit();
		myPrefs.edit().putString("number","100276690").commit();


		if (myPrefs.getBoolean("first", true)) {

			/* 
			 *  /¡\ we should  reach this code only once /!\
			 */

			//launch an intent, wake up SignUpActivity (should define it in the manifest too!)
			Intent toSignUp=new Intent("es.uc3m.setichat.activity.SIGNUPACTIVITY");
			this.startActivity(toSignUp);

		}else{

			if(mService!=null){

				//check for missed messages when the activity is resumed
				mService.sendMessage(createConnectionMessage());

			}
		}
		getFragmentManager().beginTransaction().replace(R.id.container, new ContactsFragment()).commit();
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
		ContactsFragment fragment = new ContactsFragment();
		getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
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

			//we have to do 2 things. First we have to check if there are new messages
			mService.sendMessage(createConnectionMessage());
			//second we have to check for new contacts
			mService.sendMessage(createContactsRequestMessage());

		}



		@Override
		public void onServiceDisconnected(ComponentName arg0) {

		}
	};



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


	/////////////////////////////////////////
	///			MESSAGE CREATION          ///
	///			 	METHODS              ///
	/////////////////////////////////////////	

	public  String  createContactsRequestMessage(){

		bigIn=new BigInteger(128, new Random());
		String header="<?xml version="+ " \"1.0\" "+ "encoding="+" \"UTF-8\" "+"?>"+
				"<message><header>"+
				"<idSource>"+myPrefs.getString("token", null)+"</idSource>"+
				"<idDestination>setichat@appspot.com</idDestination>"+
				"<idMessage>"+bigIn.toString(16)+"</idMessage>"+
				"<type>2</type>"+
				"<encrypted>false</encrypted>"+
				"<signed>false</signed>"+
				"</header>";
		String content="<content><mobileList>"+
				SystemHelper.readContacts(getApplicationContext())+
				"</mobileList></content></message>";
		//and send it

		return header+content;
	}

	public String createConnectionMessage(){

		//build connection message
		bigIn=new BigInteger(128, new Random());
		String header="<?xml version="+ " \"1.0\" "+ "encoding="+" \"UTF-8\" "+"?>"+
				"<message><header>"+
				"<idSource>"+myPrefs.getString("token", null)+"</idSource>"+
				"<idDestination>setichat@appspot.com</idDestination>"+
				"<idMessage>"+bigIn.toString(16)+"</idMessage>"+
				"<type>5</type>"+
				"<encrypted>false</encrypted>"+
				"<signed>false</signed>"+
				"</header>";
		String content="<content><connection></connection></content></message>";

		return header+content;
	}

}
