package es.uc3m.setichat.activity;

import es.uc3m.setichat.R;
import es.uc3m.setichat.service.SeTIChatService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.CheckBox;

public class SettingsFragment extends Fragment implements OnClickListener{
	SeTIChatService mService;
	Button rvk,sv;
	CheckBox chk;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
			View fragmentview=inflater.inflate(R.layout.settings_layout, container,false);
	
	
	rvk=(Button)fragmentview.findViewById(R.id.bt_rvk);
	sv=(Button)fragmentview.findViewById(R.id.bt_sv);
	chk=(CheckBox)fragmentview.findViewById(R.id.cb_scmd);

	
	sv.setOnClickListener(this);
	rvk.setOnClickListener(this);
	


		
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
		
		
		diagBuilder.setTitle("Warning!");
		diagBuilder.setMessage("This will destroy your current keypair (if any and will generate a new one. Continue?");
		diagBuilder.setPositiveButton("OK", new  DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				dialog.dismiss();
				
			}

			
		
		});		
		
			diagBuilder.setNegativeButton("Cancel", new  DialogInterface.OnClickListener() {
			
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
			
			
			
			
		}else if(v.getId()==rvk.getId()){
			
			showDialog().show();
			
			
		}
		
	}

	
	
	
}
