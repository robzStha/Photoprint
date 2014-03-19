package jsoft.projects.photoprint;


import jsoft.projects.photoprint.libs.ConnectionMngr;
import jsoft.projects.photoprint.libs.SessionMngr;
import jsoft.projects.photoprint.libs.UserFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.Signature;
//import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
//import android.util.Base64;
//import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Login extends Activity {
	Button btnLogin, btnDBrd;
	//Button btnLinkToRegister;
	EditText inputEmail;
	EditText inputPassword;
	TextView loginErrorMsg;

	SessionMngr session;
	
	
	// JSON Response node names
	private static String KEY_SUCCESS = "success";
	private static String KEY_UID = "uid";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        
//        PackageInfo info;
//        try {
//            info = getPackageManager().getPackageInfo("jsoft.projects.photoprint", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md;
//                md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                String something = new String(Base64.encode(md.digest(), Base64.DEFAULT));
//                //String something = new String(Base64.encodeBytes(md.digest()));
//                Log.e("hash key", something);
//            }
//        } catch (NameNotFoundException e1) {
//            Log.e("name not found", e1.toString());
//        } catch (NoSuchAlgorithmException e) {
//            Log.e("no such an algorithm", e.toString());
//        } catch (Exception e) {
//            Log.e("exception", e.toString());
//        }
//        
        
		
		setContentView(R.layout.login);
		session = new SessionMngr(getApplicationContext());
		// Importing all assets like buttons, text fields
		inputEmail = (EditText) findViewById(R.id.etUsername);
		inputPassword = (EditText) findViewById(R.id.etPassword);
		btnLogin = (Button) findViewById(R.id.btnLogin);
//		btnDBrd = (Button) findViewById(R.id.btnDBrd);
//		btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
		loginErrorMsg = (TextView) findViewById(R.id.tvMsg);
		
		
		// Login button Click Event
		btnLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				String email = inputEmail.getText().toString();
				String password = inputPassword.getText().toString();
				UserFunctions userFunctions = new UserFunctions();

				ConnectionMngr cm = new ConnectionMngr(getApplicationContext());
				if(!cm.hasConnection()){
					Toast.makeText(getApplicationContext(), "no conn", Toast.LENGTH_LONG).show();
					return;
				}
				
				JSONObject json = userFunctions.loginUser(email, password);

				// check for login response
				try {
					if (json.getString(KEY_SUCCESS) != null) {
						loginErrorMsg.setText("");
						String res = json.getString(KEY_SUCCESS); 
						if(Integer.parseInt(res) == 1){
							
							// user successfully logged in
							JSONObject json_user = json.getJSONObject("user");
							// Launch Dashboard Screen
							session.setKeyValues("uid", json_user.getInt(KEY_UID));
							
							//finish();
							Intent dashboard = new Intent(getApplicationContext(), Dashboard.class);
							
							// Close all views before launching Dashboard
							dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(dashboard);
							
							// Close Login Screen
							finish();
						}else{
							// Error in login
							loginErrorMsg.setText("Incorrect username/password");
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
