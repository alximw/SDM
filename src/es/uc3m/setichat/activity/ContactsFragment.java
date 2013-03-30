package es.uc3m.setichat.activity;


import java.util.ArrayList;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import es.uc3m.setichat.R;
import es.uc3m.setichat.contactsHandling.Contact;
import es.uc3m.setichat.contactsHandling.ContactsAdapter;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.utils.DataBaseHelper;

/**
 * This activity will show the list of contacts. 
 * If a contact is clicked, a new activity will be loaded with a conversation.
 *  
 * 
 * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
 * @author Jorge Blasco Al’s <jbalis@inf.uc3m.es>
 */

public class ContactsFragment extends ListFragment {

	private DataBaseHelper helper;
	int state=0;
	// Service, that may be used to access chat features
	private SeTIChatService mService;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);



	} 


	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		mService = ((MainActivity)activity).getService();
	}

	@Override
	public void onStop(){
		super.onStop();


	}


	
	
	/*
	 *this method will be called every time the MainActivity is loaded
	 *this means,for example,that when you open the app this method will be called.
	 *
	 *we use it for get the contact list up to date
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Populate list with contacts.
		
		//will use it for save the contacts read from the DB
		ArrayList<Contact> contacts;
		
		//get the contact list from db
		helper=MainActivity.helper;
		SQLiteDatabase database=helper.getWritableDatabase();
		
		if(database!=null){
			//if the db is not null, OK, get the contacts from database
			contacts=DataBaseHelper.contactsToArray(database);		
		}else{
			//some trouble getting the DB instance! throw an exception
		throw(new SQLiteException("NULL DATABASE"));
		}
		
		/*
		 * create and set a new adapter. The adapter will put the new contacts on the list.
		 * (look at the contacts parameter on adapter constructor!)
		 * R.layout.custom_lw_layout is the id of the custom adapter layout
		 * getActivity() used to get the current context
		 */
		ContactsAdapter adapter=new ContactsAdapter(getActivity(), R.layout.custom_lw_layout, contacts);
		
		//now we update the listFragment adapter, replacing the old one with the new we've just created
		setListAdapter(adapter);
		
		//don't forget to close the DB
		database.close();
	}



	/*
	 * this method will be executed when you push a contact
	 * shown on the contact list.
	 */
	
	public void onListItemClick(ListView l, View v, int position, long id) {
		//create a new intent, this will take us from main activity to conversationactivity
		Intent intent = new Intent();
		intent.setClass(getActivity(), SeTIChatConversationActivity.class);
		
		//put the selected contact information as an extra
		intent.putExtra("contact",(Contact)getListAdapter().getItem(position));  

		//launch the intent
		startActivity(intent);
	}





}
