/*package jsoft.projects.photoprint.libs;

import jsoft.projects.photoprint.Gallery.ImageAdapter;

import org.json.JSONObject;

import android.os.Bundle;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

public class FetchAlbum {

	*//**
	 * Makes a request for user's photo albums from Facebook Graph API
	 * @param session
	 *//*
	private void fetchAlbumsFromFB(Session session) {

	    // callback after Graph API response with user object
	    Request.GraphUserCallback graphUserCallback;
	    graphUserCallback = new Request.GraphUserCallback() {
	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            JSONObject jsonObject = null;
	            if (user != null)
	                jsonObject = user.getInnerJSONObject();

	            ImageAdapter.getInstance().setPhotoAlbums(jsonObject);
	        }
	    };

	    // assign callback to final instance variable in inner class
	    final Request.GraphUserCallback finalCallback = graphUserCallback;
	    Request.Callback wrapperCallback = new Request.Callback() {
	        @Override
	        public void onCompleted(Response response) {
	            finalCallback.onCompleted(response.getGraphObjectAs(GraphUser.class), response);
	        }
	    };

	    // make a new async request
	    Bundle params = new Bundle();
	    params.putString("fields", "photos");
	    Request request = new Request(session, "me/albums", params, null, wrapperCallback);
	    request.executeAsync();
	}


	
	
}
*/