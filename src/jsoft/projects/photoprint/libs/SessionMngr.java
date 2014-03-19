package jsoft.projects.photoprint.libs;

import jsoft.projects.photoprint.Login;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class SessionMngr {
	
	private SharedPreferences prefs;
	//public int UID = 0;
	Context _cntx;
	// Constructor
	public SessionMngr(Context cntx){
		_cntx = cntx;
		prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
	}
	
	
//	public void setUsername(String username){
//		prefs.edit().putString("Username", username).commit();
//		prefs.edit().commit();
//	}
	
	public void setKeyValues(String key, String value){
		prefs.edit().putString(key, value).commit();
		prefs.edit().commit();
	}
	
	public void setKeyValues(String key, int value){
		prefs.edit().putInt(key, value).commit();
		prefs.edit().commit();
	}
	
	public String getStringValues(String key){
		String value = prefs.getString(key, "");
		return value;
	}
	
	
	public int getIntValues(String key){
		int nullValue = 0;
		int value = prefs.getInt(key, nullValue);
		return value;
	}
	
	public void unsetSession(String key){
		prefs.edit().remove(key).commit();
		
	}
	
	public boolean IsLoggedIn(){
		if(prefs.getInt("uid", 0) == 0){
			Intent i = new Intent(_cntx, Login.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			_cntx.startActivity(i);
			return false;
		}
		return true;
	}
}
