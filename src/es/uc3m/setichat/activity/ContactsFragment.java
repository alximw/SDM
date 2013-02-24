package es.uc3m.setichat.activity;


import java.util.ArrayList;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	//Populate list with contacts.
    	//Ey, a more fancy layout could be used! You dare?!
		ArrayList<String> list=new ArrayList<String>();
        SQLiteDatabase database=MainActivity.helper.getReadableDatabase();
		
        
        Cursor resultSet= database.query("contacts",null,null,null, null, null, null);
        Log.i("[debug]",String.valueOf(resultSet.getCount()));
		if(resultSet.moveToFirst()){
			do{
				list.add(resultSet.getString(0)+"."+resultSet.getString(1));
				
			}while(resultSet.moveToNext());
			
		}else{
			list.add("<empty contact list>");
		}
		
		String[] contacts =new String[list.size()];
		for(int i=0;i<list.size();i++){
			contacts[i]=list.get(i);
			
			
		}
		
		
        
ArrayAdapter myContactsAdapter=new ArrayAdapter<String>(getActivity(),
android.R.layout.simple_list_item_activated_1,contacts );
setListAdapter(myContactsAdapter);
  
    
    
    
    
    
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    	// We need to launch a new activity to display
        // the dialog fragment with selected text.
        Intent intent = new Intent();
        intent.setClass(getActivity(), SeTIChatConversationActivity.class);
        intent.putExtra("index", position);           
        startActivity(intent);
    }
    
}
