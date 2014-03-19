package jsoft.projects.photoprint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.Session;

public class FbGallery extends BaseActivity implements OnItemSelectedListener{
	
	Session session;
	
	private static final String SELECT = "Please select an album";
	private ArrayList<String> albumLinks, albumIds;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fb_albums);
		session = new Session(this);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 

		String url = "https://graph.facebook.com/me?fields=id,name,albums&access_token="+session.getAccessToken();

		try {
		    DefaultHttpClient httpClient = new DefaultHttpClient();
		    HttpGet httpGet = new HttpGet(url);
		    
		    HttpResponse httpResponse = httpClient.execute(httpGet);
		    HttpEntity httpEntity = httpResponse.getEntity();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"));
		    StringBuilder builder = new StringBuilder();
		    for (String line = null; (line = reader.readLine()) != null;) {
		        builder.append(line).append("\n");
		    }

		    String jString = "["+builder.toString()+"]";

		    JSONArray jsonArray = new JSONArray(jString);
		    
		    String temp = jsonArray.getJSONObject(0).get("albums").toString();
		    
		    JSONObject jsonAlbumsObj = new JSONObject(temp);
		    JSONArray jsonAlbumsArray = jsonAlbumsObj.getJSONArray("data");

		    MatrixCursor mcImages = new MatrixCursor(new String[] {"name","id"}); // properties from the JSONObjects
		    
		    for (int i = 0; i < jsonAlbumsArray.length(); i++) {
		          JSONObject jo = jsonAlbumsArray.getJSONObject(i);
		          mcImages.addRow(new Object[] {jo.get("name"),jo.get("id")});
		    }
		    
		    this.albumLinks= new ArrayList<String>();
		    this.albumIds= new ArrayList<String>();
		    albumLinks.add(SELECT);
		    albumIds.add("-");
		    for(int i=0;i<mcImages.getCount();i++){
		    	mcImages.moveToPosition(i);
		    	int dataColumnIndex = mcImages.getColumnIndex("name");
		    	albumLinks.add(mcImages.getString(dataColumnIndex));
		    	dataColumnIndex = mcImages.getColumnIndex("id");
		    	albumIds.add(mcImages.getString(dataColumnIndex));
		    	System.out.println(":>> Array path => "+albumLinks.get(i));
		    }
		    
		    Spinner mSpinner = (Spinner) findViewById(R.id.albumLinks);
		    
		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,albumLinks);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    
		    mSpinner.setAdapter(adapter);
		    mSpinner.setOnItemSelectedListener(this);
		    
		    mcImages.close();
		    
		    Log.d("Link Array", albumLinks.toString());
		    
		    
		}catch(Exception e){
			Log.d("outputs", "Error below");
			e.printStackTrace();
			Log.d("outputs", "Error Ends");
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		String item = parent.getItemAtPosition(position).toString();
		if(item!=SELECT){
			String albumId = getAlbumId(position);
			Intent i = new Intent(this,FbPhotos.class);
			i.putExtra("albumId", albumId);
			startActivity(i);
			Toast.makeText(parent.getContext(), "Selected: " + item+Integer.toString(position)+"-"+albumId, Toast.LENGTH_LONG).show();
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public String getAlbumId(int index){
		String id="";
		id = albumIds.get(index);
		return id;
	}
	
}	
