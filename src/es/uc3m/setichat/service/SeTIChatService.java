package es.uc3m.setichat.service;



import java.io.IOException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
import edu.gvsu.cis.masl.channelAPI.ChannelService;
import es.uc3m.setichat.R;
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
	
	private static final String GAE_test="http://setichatchannelapitest.appspot.com";
	private static final String GAE="http://setichat.appspot.com";
	private static NotificationManager notificationManager;
	private Notification notif;
	
	// Used to communicate with the server
	ChannelAPI channel;
	
	// Used to bind activities
	private final SeTIChatServiceBinder binder=new SeTIChatServiceBinder();
	
	

	
	public SeTIChatService() {
		Log.i("SeTIChat Service", "Service constructor");
	}

	
	
	  @Override
	  public void onCreate() {
	    super.onCreate();
	    Log.i("SeTIChat Service", "Service created");
	    
	    channel = new ChannelAPI();
		this.connect("100276882");  
	    binder.onCreate(this);
	    
		 
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
		  Log.i("SeTIChat Service", "Service binded");
		  
		  
		  //when the service is Binded, show a notification
		 notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		 notif=new Notification.Builder(this).setContentTitle("SetiChat").setSmallIcon(R.drawable.ic_launcher)
	 .setContentText("Setichat Service Binded").build();
		//make the notification non-removable
		 notif.flags|=Notification.FLAG_ONGOING_EVENT;
		 //set an icon since it's necessary to show the notification 
		 notif.icon=R.drawable.ic_launcher;
		 
		 //"launch" the notification
		 notificationManager.notify(0,notif);		  
		  return(binder);
	  }

	  @Override
	  public void onDestroy() {
	    super.onDestroy();
	    Log.i("SeTIChat Service", "Service destrotyed");
	    // When the service is destroyed, the connection is closed
	    
	    //and the notification should disappear
	    notificationManager.cancel(0);
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
			openIntent.setPackage("es.uc3m.setichat");
			Context context = getApplicationContext();
			context.sendBroadcast(openIntent);  
		
		
		
		}

		/**
		  *  Called when the client receives a chatMessage. In this case,
		  *  the main activity is notified with a Broadcast Intent.
		  */
		@Override
		public void onMessage(String message) {
			Log.i("onMessage", "Message received :"+message);
			// TODO Auto-generated method stub
			String intentKey = "es.uc3m.SeTIChat.CHAT_MESSAGE";
			Intent openIntent = new Intent(intentKey);
			openIntent.putExtra("message", message);
			openIntent.setPackage("es.uc3m.setichat");
			Context context = getApplicationContext();
			context.sendBroadcast(openIntent);  
			XMLParser xpp=new XMLParser();
			
			//should vibrate only when type=4 (chat message)! if commented for debugging purposes 
			//if(xpp.getTagValue(message, "type").toString().equals("4")){
				Vibrator vib=(Vibrator)getSystemService(VIBRATOR_SERVICE);
				vib.vibrate(500);
				
				
			//}
			
			
			
			
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
