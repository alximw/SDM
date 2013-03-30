package es.uc3m.setichat.activity;

import java.math.BigInteger;
import java.sql.Time;
import java.util.Random;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.app.ListFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import es.uc3m.setichat.R;
import es.uc3m.setichat.contactsHandling.Contact;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.service.SeTIChatServiceBinder;
import es.uc3m.setichat.utils.Base64;
import es.uc3m.setichat.utils.DataBaseHelper;
import es.uc3m.setichat.utils.SecurityHelper;
import es.uc3m.setichat.utils.SystemHelper;
import es.uc3m.setichat.utils.XMLParser;

/**
 * This activity will show the conversation with a given contact. 
 * It will allow also to send him new messages and, of course, will refresh when a new message arrives.
 * 
 * If the user is viewing a different conversation when a message arrive from a third party contact,
 * then a notification should be shown. 
 * 
 * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
 * @author Jorge Blasco Alis <jbalis@inf.uc3m.es>
 */

public class SeTIChatConversationActivity extends Activity {

	private EditText edit;
	private ScrollView scroller;
	private TextView text;

	private boolean DEBUG = false;
	private XMLParser parser;
	private SeTIChatService mService;
	private static BigInteger bigIn;
	private static DataBaseHelper helper;
	SQLiteDatabase db;
	//will use this for get the message from the mainActivity
	private BroadcastReceiver receiver;

	private static NotificationManager notificationManager;
	private Notification notif;
	private boolean isNotified;
	
	
	
	String myNumber;
	Contact contact;

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mService = SeTIChatServiceBinder.getService();

			DEBUG = true;

			render();

		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.

			if (DEBUG)
				Log.d("SeTIChatConversationActivity",
						"onServiceDisconnected: un-bounding the service");

			mService = null;
			Toast.makeText(SeTIChatConversationActivity.this, "Disconnected", // R.string.local_service_disconnected,
					Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		helper=MainActivity.helper;
		db=helper.getWritableDatabase();
		myNumber=MainActivity.myPrefs.getString("number", "");

		
		isNotified=getIntent().getBooleanExtra("notified",false);
		

		contact=(Contact) getIntent().getSerializableExtra("contact");
		DataBaseHelper.getMessages(contact.getNumber(), db);

		this.setTitle("SetiChatting with "+contact.getNick());
		parser=new XMLParser();
		//lets define a filter so we can catch the intent launched on mainActivity
		IntentFilter myfilter=new IntentFilter();
		myfilter.addAction("es.uc3m.SeTIChat.CHAT_MESSAGE");

		receiver=new BroadcastReceiver(){

			@Override
			public void onReceive(Context arg0, Intent arg1) {

				String message=arg1.getCharSequenceExtra("message").toString();

				if(parser.getTagValue(message, "type").equals("4") && parser.getTagValue(message, "idSource").equals(contact.getNumber())){

					//we have received a message from the  contact
					message=parser.getTagValue(message,"chatMessage");
					Log.d("fsfsdf", "sdfsdfdf1");
					SQLiteDatabase db=helper.getWritableDatabase();
					
					if(db!=null){
					DataBaseHelper.saveMessages(contact.getNumber(),contact.getNick(),myNumber,message, db);
					//print it on the chat view
					}else{
						throw(new SQLiteException("NULL DATABASE"));
					}
					db.close();
					updateChatView();


				}else if (parser.getTagValue(message, "type").equals("4") && !parser.getTagValue(message, "idSource").equals(contact.getNumber())){
					//message from  different contact

					
					
					helper=MainActivity.helper;
					String othercontact_nick="";
					String othercontact_number=parser.getTagValue(message, "idSource");
					String pendingMessage=parser.getTagValue(message, "chatMessage");
					String destination=parser.getTagValue(message, "idDestination");

					if(db!=null){
					//save all unread mesages
					//DataBaseHelper.writeUnreadMessages(db, parser.getTagValue(message, "chatMessage"),othercontact_number);
					othercontact_nick=DataBaseHelper.getNickByNumber(othercontact_number, db);
					if(othercontact_nick.equals("Unknown Contact")){
						othercontact_nick=othercontact_number;
					}
					DataBaseHelper.saveMessages(othercontact_number,othercontact_nick,destination, pendingMessage, db);

					
					}else{						
						throw(new SQLiteException("NULL DATABASE"));
					}
					db.close();

					//create the intent used by the notification
					Intent notifIntent=new Intent(getApplicationContext(),SeTIChatConversationActivity.class);
					notifIntent.putExtra("contact", new Contact(othercontact_nick,othercontact_number));
					notifIntent.putExtra("notified", true);
					PendingIntent pending =PendingIntent.getActivity(getApplicationContext(), 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					

					//set the notification and it parameter
					notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
					notif=new Notification.Builder(getApplicationContext())
					.setContentTitle("SeTiChat")
					.setContentText("new messages from "+othercontact_nick)
					.setSmallIcon(R.drawable.custom_icon)
					.setContentIntent(pending)
					.setDefaults(Notification.DEFAULT_SOUND)
					.setDefaults(Notification.DEFAULT_VIBRATE)
					.build();


					// Hide the notification after its selected
					notif.flags |= Notification.FLAG_AUTO_CANCEL;

					//Integer.parseInt(idSource)%5000 used as notification ID, FLAG_UPDATE_CURRENT
					// will only update the pending intent and will not create other notifications for thw same contact
					int id=SystemHelper.string2Integer(othercontact_number);
					Log.i("value",String.valueOf(id));

					notificationManager.notify(id, notif);


				}

			}


		};

		//here we register the receiver as a "listener" of the intent
		registerReceiver(receiver,myfilter);


		if (mService == null) {
			// Binding the activity to the service to get shared objects
			if (DEBUG)
				Log.d("SeTIChatConversationActivity", "Binding activity");
			bindService(new Intent(SeTIChatConversationActivity.this,
					SeTIChatService.class), mConnection,
					Context.BIND_AUTO_CREATE);
		} else {
			render();
		}


	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG)
			Log.d("SeTIChatConversationActivity", "Unbinding activity");
	}



	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}


	@Override
	protected void onResume() {
		super.onResume();

		if (mService == null) {
			// Binding the activity to the service to get shared objects
			if (DEBUG)
				Log.d("SeTIChatConversationActivity", "Binding activity");
			bindService(new Intent(SeTIChatConversationActivity.this,
					SeTIChatService.class), mConnection,
					Context.BIND_AUTO_CREATE);
		} 

	}

	private void render() {
		// Tell the user about the service.
		Toast.makeText(SeTIChatConversationActivity.this, "Connected", // R.string.local_service_connected,
				Toast.LENGTH_SHORT).show();

		int index = getIntent().getIntExtra("index", -1);
		if (DEBUG)
			Log.d("SeTIChatConversationActivity",
					"onServiceConnected: Rendering conversation based on extra information provided by previous activity intention: "
							+ index);
		setContentView(conversationView(index));
	}

	public View conversationView(int index) {

		// ***************************************************************** //
		// *********************** Layouts and Views *********************** //
		// ***************************************************************** //

		int padding = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
				.getDisplayMetrics());

		// Creating a general layout
		LinearLayout background = new LinearLayout(this);
		background.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		background.setOrientation(LinearLayout.VERTICAL);
		background.setPadding(0, 0, 0, padding);

		// Creating a layout for the edit text and the bottom to be in the
		// button
		LinearLayout background_edit = new LinearLayout(this);
		background_edit.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		background_edit.setOrientation(LinearLayout.HORIZONTAL);

		// Creating the view to show the conversations
		text = new TextView(this);
		text.setLayoutParams(new FrameLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		text.setPadding(padding, padding, padding, 0);
		//text.setId(R.id.conversation);
		// Adding some scroll
		scroller = new ScrollView(this);
		scroller.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f));
		scroller.post(new Runnable() {
			public void run() {
				scroller.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});

		
		// Creating the edit text to add new chats
		edit = new EditText(this);
		edit.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		edit.requestFocus();

		// Of course a send button
		Button send = new Button(this);
		send.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 3f));
		send.setText("Send");
		updateChatView();

		// Sending messages
		send.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (DEBUG)
					Log.d("SeTIChatConversationActivity",
							"conversationView:OnClickListener: User clicked on sent button");

				Time time = new Time(System.currentTimeMillis());



				//build the chat message
				IvParameterSpec IV=SecurityHelper.generateAESIV();
				SecretKeySpec key=SecurityHelper.generateAES128Key();
				String mode=SecurityHelper.SETICHAT_AES_MODE;
				String msg=new String(SecurityHelper.encryptAES128(IV, key,mode, (String) text.getText()));
				

				//send message using the service instance
				mService.sendMessage(createChatMessage(contact.getNumber(), edit.getText().toString(),false,false));
				db=helper.getReadableDatabase();
				if(db!=null){
					
					DataBaseHelper.saveMessages(myNumber,"You",contact.getNumber(),edit.getText().toString(),db);	
				}
				db.close();
				// Refresh textview
				//write the sent message on the scroll
				updateChatView();
				edit.setText("");
			}
		});


		// ***************************************************************** //
		// ******** Configuring the Views and returning the layout ******** //
		// ***************************************************************** //

		scroller.addView(text);
		background.addView(scroller);
		background_edit.addView(edit);
		background_edit.addView(send);
		background.addView(background_edit);





		return background;
	}



public void updateChatView(){
	
	SQLiteDatabase db=helper.getReadableDatabase();
	if(db!=null){
	text.setText("Now chatting with "+contact.getNick()+"\n"+DataBaseHelper.retrieveChat(contact.getNumber(), db));
	}else{
		throw(new SQLiteException("NULL DATABASE"));

	}
	scroller.pageScroll(ScrollView.FOCUS_DOWN);
	db.close();
}

	
	

	
	 /////////////////////////////////////////
    ///			MESSAGE CREATION          ///
   ///			 	METHODS              ///
  /////////////////////////////////////////	
	
	
	public  String createChatMessage(String destination,String msg, boolean goesEncrypted,boolean goesSigned){
			
		bigIn=new BigInteger(128, new Random());
		String idSource,idDestination,idMessage,content;
		SecretKeySpec key=SecurityHelper.generateAES128Key();
		IvParameterSpec iv=SecurityHelper.generateAESIV();
		byte[] initVector,encryptedAESK = null,encryptedMessage;
		byte[] confidentialMessage;
		byte[] messageB64;
		
		
		idSource="<idSource>"+MainActivity.myPrefs.getString("token", null)+"</idSource>";
		idDestination="<idDestination>"+destination+"</idDestination>";
		idMessage="<idMessage>"+bigIn.toString(16)+"</idMessage>";
		
		if(goesEncrypted){
			
			encryptedMessage=SecurityHelper.encryptAES128(iv, key, SecurityHelper.SETICHAT_AES_MODE, msg);
			initVector=iv.getIV();
			
			//get pair's public key and encrypt the AES key with it
			
			
			confidentialMessage=new byte[initVector.length+encryptedAESK.length+encryptedMessage.length];
			
			System.arraycopy(encryptedAESK,0,confidentialMessage, 0, encryptedAESK.length);
			System.arraycopy(initVector, 0, confidentialMessage,encryptedAESK.length, initVector.length);
			System.arraycopy(encryptedMessage, 0,confidentialMessage,encryptedAESK.length+initVector.length, encryptedMessage.length);
			messageB64=Base64.encodeToByte(confidentialMessage, false);
			
			content="<content><chatMessage>"+new String(messageB64)+"</chatMessage></content></message>";
			
		}else{
			
			content="<content><chatMessage>"+msg+"</chatMessage></content></message>";

		}
		
		
		if(goesSigned){
			
			//sign the message with my private key
			
		}
		
		

			String header="<?xml version="+ " \"1.0\" "+ "encoding="+" \"UTF-8\" "+"?>"+
					"<message><header>"+
					"<idSource>"+MainActivity.myPrefs.getString("token", null)+"</idSource>"+
					"<idDestination>"+destination+"</idDestination>"+
					"<idMessage>"+bigIn.toString(16)+"</idMessage>"+
					"<type>4</type>"+
					"<encrypted>false</encrypted>"+
					"<signed>false</signed>"+
					"</header>";
		
		



		
		return header+content;
	}


}
