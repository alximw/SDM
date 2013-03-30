package es.uc3m.setichat.contactsHandling;

import java.util.ArrayList;

import es.uc3m.setichat.R;
import es.uc3m.setichat.activity.MainActivity;
import es.uc3m.setichat.utils.DataBaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactsAdapter extends ArrayAdapter<Contact> {
private ArrayList<Contact> contacts;
DataBaseHelper helper;
SQLiteDatabase db;
	
	
    public ContactsAdapter(Context context, int textViewResourceId, ArrayList<Contact> contacts) {
        super(context, textViewResourceId, contacts);
    this.contacts=contacts;
    }


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		helper=MainActivity.helper;
		db=helper.getReadableDatabase();
		
		
		// TODO Auto-generated method stub    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        
    if (v == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.custom_lw_layout, null);
    }
            
    Contact contact = contacts.get(position);
    if (contact != null) {
            TextView nick = (TextView) v.findViewById(R.id.nick);
            TextView lastline = (TextView) v.findViewById(R.id.lastline);
            lastline.setTypeface(null, Typeface.ITALIC);


        if (nick != null) {
                nick.setText(contact.getNick());
        }

        if(lastline != null) {
        	
                lastline.setText(DataBaseHelper.getLastMessages(contact.getNumber(), db));
        }
        
        
    }
    if((position%2)==0){
    	
    	v.setBackgroundColor(Color.parseColor("#FFFFFF"));
    }else{
    	v.setBackgroundColor(Color.parseColor("#EBEBEB"));
    }
    
    return v;
    }




    
    
    
}
