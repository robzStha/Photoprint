package jsoft.projects.photoprint;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class FbLogin extends Activity{

	public String accessToken;

	TextView tvUsername;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
		Session.openActiveSession(FbLogin.this, true, new Session.StatusCallback() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void call(Session session, SessionState state, Exception exception) {

				if(session.isOpened()){
					accessToken = session.getAccessToken().toString();
					session.requestNewPublishPermissions(new Session.NewPermissionsRequest(FbLogin.this, "user_photos"));
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
						
						@Override
						public void onCompleted(GraphUser user, Response response) {

//						Log.d("User info", user.toString());
							if(user != null){
								Intent i = new Intent(FbLogin.this, FbGallery.class);
								startActivity(i);
								//tvUsername.setText("Hello " + user.getName()+"!"+" Your access token is: "+accessToken);
							}
						}
					});
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {   
		finish();
		Intent i = new Intent(FbLogin.this, Dashboard.class);
		startActivity(i);
        return true;
    }
	
}
