package es.uc3m.setichat.activity;

import java.security.KeyPair;

import es.uc3m.setichat.R;
import es.uc3m.setichat.service.SeTIChatService;
import es.uc3m.setichat.utils.DataBaseHelper;
import es.uc3m.setichat.utils.SecurityHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class SettingsFragment extends Fragment implements OnClickListener{
	SeTIChatService mService;
	Button rvk,sv;
	CheckBox chk;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
			View fragmentview=inflater.inflate(R.layout.settings_layout, container,false);
	
	//view elements
	rvk=(Button)fragmentview.findViewById(R.id.bt_rvk);
	sv=(Button)fragmentview.findViewById(R.id.bt_sv);
	chk=(CheckBox)fragmentview.findViewById(R.id.cb_scmd);

	//set on click event listeners
	sv.setOnClickListener(this);
	rvk.setOnClickListener(this);
	
	//set checkbox state
	chk.setChecked(MainActivity.myPrefs.getBoolean("SEC_MODE", true));


		
	return fragmentview;
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		 mService = ((MainActivity)activity).getService();
	}

	
	
	private Dialog showDialog(){
		AlertDialog.Builder diagBuilder = new AlertDialog.Builder(getActivity());
		
		
		diagBuilder.setTitle("Hey,Listen!");
		diagBuilder.setMessage("This will destroy your current keypair (if any) and will generate a new one. Continue?");
		diagBuilder.setPositiveButton("Sure!", new  DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				
				SQLiteDatabase db=MainActivity.helper.getWritableDatabase();
				
				if(db!=null){
					
					DataBaseHelper.deleteKeyPair(db);
					KeyPair pair=SecurityHelper.generateRSAKeyPair(SecurityHelper.RSAPAIR_KEY_SIZE);
					DataBaseHelper.saveKeyPair(pair, db);
					mService.sendMessage(SignUpActivity.createKeyUploadMessage(pair.getPublic()));
					Toast.makeText(getActivity(), "Key Pair successfully revoked. New one has been generated and on-server public key has been updated..", Toast.LENGTH_LONG).show();
				}else{
					throw (new SQLiteException("NULL DATABASE"));
				}

				db.close();
				dialog.dismiss();
				
			}

			
		
		});		
		
			diagBuilder.setNegativeButton("Nope!", new  DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
			}

			
		
		});	
		return diagBuilder.create();
	}
	
	
	@Override
	public void onClick(View v) {
		
		if(v.getId()==sv.getId()){
			//save shared preferences
			
			MainActivity.myPrefs.edit().putBoolean("SEC_MODE", chk.isChecked()).commit();
			Toast.makeText(getActivity(), "Your preferences has been saved", Toast.LENGTH_SHORT).show();

		}else if(v.getId()==rvk.getId()){
			
			showDialog().show();
			
			
		
		}
		
	}

	
	
	
}
