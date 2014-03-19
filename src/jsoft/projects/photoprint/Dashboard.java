package jsoft.projects.photoprint;

import jsoft.projects.photoprint.libs.SessionMngr;
import jsoft.projects.photoprint.libs.UserFunctions;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.facebook.Session;

//import android.view.MenuItem;

public class Dashboard extends ListActivity {

	private SessionMngr session;

	UserFunctions userFunctions = new UserFunctions();
	String[] classes = { "MyGallery", "FbLogin", "Profile",
			"Order History", "Logout" }; // "Twitter","Google","FbGallery"


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		session = new SessionMngr(getApplicationContext());

		if (session.IsLoggedIn()) {
			setListAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, classes));
		} else {
			finish();
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String menu = classes[position];
		@SuppressWarnings("rawtypes")
		Class cls;
		if (menu == "Logout") {
			
			fbLogout();
			
			session.unsetSession("uid");
			CookieSyncManager.createInstance(this);
			CookieManager cm = CookieManager.getInstance();
			cm.removeAllCookie();

			// End the current activity
			finish();
			// Start a new activity
			Intent i = new Intent(Dashboard.this, Login.class);
			// i.putExtra("urString", true);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
		}
		else{
			try {
				finish();
				cls = Class.forName("jsoft.projects.photoprint." + menu);
				Intent i = new Intent(Dashboard.this, cls);
				startActivity(i);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	public void fbLogout(){
		Session session = Session.getActiveSession();
	    if (session != null) {
	    	Log.d("fbLogout:","I am in a session with parameters");
	        if (!session.isClosed()) {
	        	Log.d("fbLogout:","I am in a open session with parameters");
	            session.closeAndClearTokenInformation();
	            //clear your preferences if saved
	        }
	    } else {
	    	Log.d("fbLogout:","I am in a session without parameters");
	        session = new Session(Dashboard.this);
	        Session.setActiveSession(session);

	        session.closeAndClearTokenInformation();
	            //clear your preferences if saved

	    }
	}
	
	// public static boolean hasConnection(Context context){
	// ConnectivityManager cm =
	// (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	// if(cm != null){
	// NetworkInfo[] info = cm.getAllNetworkInfo();
	// if (info != null){
	// for (int i = 0; i < info.length; i++){
	// if (info[i].getState() == NetworkInfo.State.CONNECTED)
	// {
	// return true;
	// }
	// }
	// }
	// }
	// return false;
	// }
}
