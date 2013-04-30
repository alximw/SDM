package es.uc3m.sdm.setichat2013.GR82.NIA100276882.NIA100276690;


import java.math.BigInteger;

import javax.crypto.SecretKey;

import es.uc3m.sdm.setichat2013.GR82.NIA100276882.NIA100276690.R;
import es.uc3m.setichat.contactsHandling.User;
import es.uc3m.setichat.utils.Base64;
import es.uc3m.setichat.utils.DataBaseHelper;
import es.uc3m.setichat.utils.SecurityHelper;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UnlockActivity extends Activity implements OnClickListener{

	Button unlock;
	TextView psw;
	User user;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		this.setTitle("Unlock the app");
		this.setContentView(R.layout.unlock_activity_layout);
		//layout elements
		unlock=(Button)findViewById(R.id.bt_unlock);
		psw=(TextView)findViewById(R.id.tv_unlock_password);
		unlock.setOnClickListener(this);
				SQLiteDatabase db=MainActivity.helper.getReadableDatabase();
				//get user data
				user=DataBaseHelper.getUserInfo(db);
				db.close();
	}

	@Override
	public void onClick(View v) {
		
		
		
		//compare the new key with the hashed user's key
		
		if(!psw.getText().toString().matches("")){
		//get salt
		byte[] salt=Base64.decode(user.getSalt());
		//get the derived key
		SecretKey key=SecurityHelper.derivePassword(psw.getText().toString(), salt);
		//generate double SHA1 hash
		String hash=SecurityHelper.generateSHA1Hash(key.getEncoded());
		String double_hash=SecurityHelper.generateSHA1Hash(hash.getBytes());
		//compare SHA1 hashes
		if(double_hash.equals(user.getHash())){
			//the hashes math
			Toast.makeText(this,"correct unlock password",Toast.LENGTH_SHORT).show();
			MainActivity.key=key;
			Intent intent =new Intent(getApplicationContext(), MainActivity.class);
			startActivity(intent);
			finish();
		
		}else{
			//the hashes DONT math
			Toast.makeText(this,"Wrong unlock password",Toast.LENGTH_SHORT).show();
			
		}
		}else{
			Toast.makeText(this,"Password field shouldn't be empty",Toast.LENGTH_SHORT).show();
		}
		
	}

	
	
}
