package jsoft.projects.photoprint;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import jsoft.projects.photoprint.libs.ConnectionMngr;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class FbPhotos extends BaseActivity {

	int counter=0;

	String imgPath = null;
	String upLoadServerUri = "http://www.jhamel.com/print/UploadToServer.php";
	String lineEnd = "\r\n";
	String twoHypens = "--";
	String boundary = "*****";

	TextView tvMulMsg;
	
	private static ArrayList<String> selectedItems;
	private ArrayList<String> imageIds;
	private ArrayList<String> imageUrls;
	private DisplayImageOptions options;
	private ImageAdapter imageAdapter;
	private ProgressDialog dialog = null;
	private int serverResponseCode = 0;
	private String albumId;

	Session session;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_image_grid);

		Bundle extras = getIntent().getExtras();
		albumId = extras.getString("albumId");

		Toast.makeText(this, albumId, Toast.LENGTH_LONG).show();

		session = new Session(getApplicationContext());

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		tvMulMsg = (TextView) findViewById(R.id.tvMulMsg);

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		String url = "https://graph.facebook.com/" + albumId
				+ "/photos?fields=images&access_token="
				+ session.getAccessToken();

		try {

			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			String algorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory
					.getInstance(algorithm);
			tmf.init(keyStore);

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, tmf.getTrustManagers(), null);

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpEntity.getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}

			String jString = "[" + builder.toString() + "]";

			JSONArray jsonArray = new JSONArray(jString);

			String temp = jsonArray.getJSONObject(0).get("data").toString();

			JSONArray jsonAlbumsArray = new JSONArray(temp);

			MatrixCursor mcImages = new MatrixCursor(new String[] { "source",
					"id" }); // properties from the JSONObjects

			for (int i = 0; i < jsonAlbumsArray.length(); i++) {
				JSONObject jo = jsonAlbumsArray.getJSONObject(i);
				JSONArray ja = jo.getJSONArray("images");
				JSONObject joImages = ja.getJSONObject(0);
				mcImages.addRow(new Object[] { joImages.get("source"),
						jo.get("id") });
			}

			this.imageUrls = new ArrayList<String>();
			this.imageIds = new ArrayList<String>();
			String tempUrl;
			for (int i = 0; i < mcImages.getCount(); i++) {
				mcImages.moveToPosition(i);
				Log.d("columnName", mcImages.getColumnName(0).toString());
				int dataColumnIndex = mcImages.getColumnIndex("source");
				tempUrl = mcImages.getString(dataColumnIndex);
				//images = LoadImageFromWebOperations(temp);
				imageUrls.add(tempUrl);
				dataColumnIndex = mcImages.getColumnIndex("id");
				imageIds.add(mcImages.getString(dataColumnIndex));
				// System.out.println(":>> Array path => "+imageUrls.get(i));
			}
			mcImages.close();

			options = new DisplayImageOptions.Builder()
					.showStubImage(R.drawable.stub_image)
					.showImageForEmptyUri(R.drawable.image_for_empty_url)
					.cacheInMemory().cacheOnDisc().build();

			imageAdapter = new ImageAdapter(this, imageUrls);
			//
			GridView gridView = (GridView) findViewById(R.id.gridview);
			gridView.setAdapter(imageAdapter);

		} catch (Exception e) {
//			Log.d("outputs", "Error below");
			e.printStackTrace();
//			Log.d("outputs", "Error Ends");
		}
	}

	@Override
	protected void onStop() {
		imageLoader.stop();
		super.onStop();
	}

	public static Drawable LoadImageFromWebOperations(String url) {
	    try {
	        InputStream is = (InputStream) new URL(url).getContent();
	        Drawable d = Drawable.createFromStream(is, "src name");
	        return d;
	    } catch (Exception e) {
	        return null;
	    }
	}

	public void btnChoosePhotosClick(View v) {
		selectedItems = imageAdapter
				.getCheckedItems();

		dialog = ProgressDialog.show(FbPhotos.this, "", "Uploading file...",
				true);
		new Thread(new Runnable() {
			public void run() {
//				Log.d("selectedItems", selectedItems.toString());
				
//				for(int i=0;i<selectedItems.size();i++){
					new DownloadFileFromURL().execute(selectedItems.toArray());
//				}
//				DownloadFileFromURL dfURL = new DownloadFileFromURL();
//				dfURL.getStatus();
//				if(dfURL.getStatus() == AsyncTask.Status.FINISHED){
//					Toast.makeText(getApplicationContext(), "Download completed", Toast.LENGTH_LONG).show();
//					//UploadFile(selectedItems);
//				}
			}
		}).start();
	}

	public class ImageAdapter extends BaseAdapter {

		ArrayList<String> mList;
		LayoutInflater mInflater;
		Context mContext;
		SparseBooleanArray msparseBooleanArray;

		public ImageAdapter(Context context, ArrayList<String> imageUrls) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
			msparseBooleanArray = new SparseBooleanArray();
			mList = new ArrayList<String>();
			this.mList = imageUrls;
			imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		}

		public ArrayList<String> getCheckedItems() {
			ArrayList<String> mTempArray = new ArrayList<String>();

			for (int i = 0; i < mList.size(); i++) {
				if (msparseBooleanArray.get(i)) {
					mTempArray.add(mList.get(i));
				}
			}
			return mTempArray;
		}

		@Override
		public int getCount() {
			return imageUrls.size();
		}

		@Override
		public Object getItem(int position) { // argument is position
			return null;
		}

		@Override
		public long getItemId(int position) { // argument is position
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.row_multiphoto_item,
						null);
			}

			CheckBox mCheckBox = (CheckBox) convertView
					.findViewById(R.id.checkBox1);

			final ImageView imageView = (ImageView) convertView
					.findViewById(R.id.imageView1);

			// Log.d("ImageUrl>>",imageUrls.get(position));

			imageLoader.displayImage("" + imageUrls.get(position), imageView,
					options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(Bitmap loadedImage) {
							Animation anim = AnimationUtils.loadAnimation(
									FbPhotos.this, R.anim.fade_in);
							imageView.setAnimation(anim);
							anim.start();
						}
					});

			mCheckBox.setTag(position);
			mCheckBox.setChecked(msparseBooleanArray.get(position));
			mCheckBox.setOnCheckedChangeListener(mCheckedChangeListener);

			return convertView;
		}

		OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				msparseBooleanArray.put((Integer) buttonView.getTag(),
						isChecked);
			}
		};
	}
	
	protected int UploadFile(ArrayList<String> sourceFileUri){

		ConnectionMngr cm = new ConnectionMngr(getApplicationContext());
		boolean connection = cm.hasConnection();
		if(!connection){
			Toast.makeText(getApplicationContext(), "No Internet Connection. Please connect to the internet and try again", Toast.LENGTH_LONG).show();
			return 0;
		}
			
			String fileName;// = sourceFileUri.get(2).toString();
			
			HttpURLConnection conn = null;
			DataOutputStream dos = null;
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1*1024*1024;
			File sourceFile = null;
			
				try{
					// Open a URL connection to the Servelet
					FileInputStream fileInputStream = null;
					URL url = new URL(upLoadServerUri);
					
					// Open a HTTP connection to the URL
					conn = (HttpURLConnection)url.openConnection();
					conn.setDoInput(true); // Allow Inputs
					conn.setDoOutput(true); // Allow Outputs
					conn.setUseCaches(false); // Don't use a Cached copy
					conn.setRequestMethod("POST"); //Sets connection method
					conn.setRequestProperty("Connection", "Keep-Alive");
					conn.setRequestProperty("ENCTYPE", "multipart/form-data");					
					conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
					
					for(int i=0 ; i<sourceFileUri.size() ; i++){
						
						fileName = Environment.getExternalStorageDirectory().toString() + "/fb_images/fb_image"+i+".jpg";
						//fileName = new URL(sourceFileUri.get(i));
						
						sourceFile = new File(fileName);
						
						Log.d("File name:", sourceFile.toString());
						
						if(!_ifIsFile(sourceFile)){
							return 0;
						}
						
						//fileName = sourceFile.toString();
						fileInputStream = new FileInputStream(sourceFile);
						//conn.setRequestProperty("uploaded_file[]", fileName);
					
						dos = new DataOutputStream(conn.getOutputStream());
						
						dos.writeBytes(twoHypens + boundary + lineEnd);
						dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file[]\";filename=\"" + fileName+"\""+lineEnd);
						
						dos.writeBytes(lineEnd);
						
						// create a buffer of maximum size
						bytesAvailable = fileInputStream.available();
						
						bufferSize = Math.min(bytesAvailable, maxBufferSize);
						buffer = new byte[bufferSize];
						
						// read file and write it into form
						bytesRead = fileInputStream.read(buffer, 0, bufferSize);
						
						while(bytesRead>0){
							dos.write(buffer, 0, bufferSize);
							bytesAvailable = fileInputStream.available();
							bufferSize = Math.min(bytesAvailable, maxBufferSize);
							bytesRead = fileInputStream.read(buffer, 0, bufferSize);
						}
						dos.writeBytes(lineEnd);
					}
					// send multipart form data necessary after file data
					dos.writeBytes(twoHypens+boundary+twoHypens+lineEnd);
					
					// Responses from the server(code and message)
					serverResponseCode = conn.getResponseCode();
					String serverResponseMessage = conn.getResponseMessage();
					
					Log.i("uploadFile","HTTP Response is : "
	                        + serverResponseMessage + ": " + serverResponseCode);
					if(serverResponseCode == 200){ //if http response is created
						runOnUiThread(new Runnable(){
							public void run(){
								String msg = "File Upload Completed.";
								//tvMulMsg.setText(msg);
								Toast toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
								toast.show();
								
								ShowDashboard();
								
							}
						});
					}
					//close the streams //
	                fileInputStream.close();
	                dos.flush();
	                dos.close();
				}
				catch (MalformedURLException ex) {
	                
	                dialog.dismiss(); 
	                ex.printStackTrace();
	                 
	                runOnUiThread(new Runnable() {
	                    public void run() {
	                        tvMulMsg.setText("MalformedURLException Exception : check script url.");
	                        //Toast.makeText(Gallery.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
	                    }
	                });
	                 
	                Log.e("Upload file to server", "error: " + ex.getMessage(), ex); 
	            } catch (Exception e) {
	                 
	            	dialog.dismiss(); 
	                e.printStackTrace();
	                 
	                runOnUiThread(new Runnable() {
	                    public void run() {
	                        tvMulMsg.setText("Got Exception with server");
	                        //Toast.makeText(getApplicationContext(), "Got Exception with server ", Toast.LENGTH_SHORT).show();
	                    }
	                });
	                Log.e("Upload file to server Exception", "Exception : "  + e.getMessage(), e); 
	            }
				dialog.dismiss();
				DeleteFbImages();
			return serverResponseCode;
		}
	public boolean _ifIsFile(File file){
		if(!file.isFile()){
			dialog.dismiss();
			
			Log.e("uploadFile", "Source File not exist : " + imgPath);
			
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					tvMulMsg.setText("Source File not exist : " + imgPath);
				}
			});
			return false;
		}
		return true;
	}
	
	public void ShowDashboard(){
		finish();
		Intent i = new Intent(this, Dashboard.class);
		startActivity(i);
	}

	
	
	class DownloadFileFromURL extends AsyncTask<Object, String, String> {
		
	    /**
	     * Before starting background thread
	     * Show Progress Bar Dialog
	     * */
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	    }
	 
	    /**
	     * Downloading file in background thread
	     * */
	    @Override
	    protected String doInBackground(Object... params) {
	        int count;
	    	
	    	File directory = new File(Environment.getExternalStorageDirectory(),"/fb_images");
	    	if(directory.exists() == false)
	    	{
	    		directory.mkdir();
	    	}
	    	
	    	for(int i = 0; i<params.length; i++){
	    		try{
	    			
	    			System.out.println(params[i]);
	    			
	    			
	    			
	    			URL url = new URL(params[i].toString());
		            URLConnection conection = url.openConnection();
		            conection.connect();
		            // getting file length
		            int lenghtOfFile = conection.getContentLength();
		 
		            // input stream to read file - with 8k buffer
		            InputStream input = new BufferedInputStream(url.openStream(), 8192);
		 
		            // Output stream to write file
		            
		            File fileName = new File(directory+"/fb_image"+i+".jpg");
		            
		            
		            OutputStream output = new FileOutputStream(fileName);
//		            counter++;
		 
		            byte data[] = new byte[1024];
		 
		            long total = 0;
		 
		            while ((count = input.read(data)) != -1) {
		                total += count;
		                // publishing the progress....
		                // After this onProgressUpdate will be called
		                publishProgress(""+(int)((total*100)/lenghtOfFile));
		 
		                // writing data to file
		                output.write(data, 0, count);
		            }
		 
		            // flushing output
		            output.flush();
		 
		            // closing streams
		            output.close();
		            input.close();
		            
	    		} catch(MalformedURLException e){
	    			e.printStackTrace();
	    		} catch(ClientProtocolException e){
	    			e.printStackTrace();
	    		} catch(IOException e){
	    			e.printStackTrace();
	    		}
	    	}
	        return null;
	    }
	 
	    /**
	     * Updating progress bar
	     * */
	    protected void onProgressUpdate(String... progress) {
	        // setting progress percentage
	        //pDialog.setProgress(Integer.parseInt(progress[0]));
	    	super.onProgressUpdate(progress);
	   }
	 
	    /**
	     * After completing background task
	     * Dismiss the progress dialog
	     * **/
	    @Override
	    protected void onPostExecute(String file_url) {
	        // dismiss the dialog after the file was downloaded
	        //dismissDialog(progress_bar_type);
	 
	        // Displaying downloaded image into image view
	        // Reading image path from sdcard
	    	
	    	UploadFile(selectedItems);
	    	
	    }
	 
	}
	
	public static void DeleteFbImages(){
		
		for(int i=0 ;i< selectedItems.size();i++){
			String delImg = Environment.getExternalStorageDirectory().toString()+"/fb_images/fb_image"+i+".jpg";
			DeleteFile(delImg);
		}
		
		File path = new File (Environment.getExternalStorageDirectory().toString()+"/fb_images");
		if(path.exists()){
			path.delete();
		}
		
	}
	
	public static void DeleteFile(String fileName){
		File file = new File(fileName);
		if(!file.exists()){
			System.out.println("not exists");
			return;
		}
		if(!file.isDirectory()){
			System.out.println("Deleted");
			file.delete();
			return;
		}
	}
	
	
}