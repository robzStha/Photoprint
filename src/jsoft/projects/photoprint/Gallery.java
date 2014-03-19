package jsoft.projects.photoprint;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import jsoft.projects.photoprint.libs.ConnectionMngr;
import jsoft.projects.photoprint.libs.SessionMngr;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class Gallery extends BaseActivity{
	
	private ProgressDialog dialog = null;
	private int serverResponseCode = 0;
	public String imgPath = null;
	String upLoadServerUri = "http://www.jhamel.com/print/UploadToServer.php";
	//TextView tvMulMsg = (TextView)findViewById(R.id.tvMulMsg);
	
	SessionMngr session;
	
	String lineEnd = "\r\n";
	String twoHypens = "--";
	String boundary = "*****";
	
	TextView tvMulMsg;
	
	private ArrayList<String> imageUrls;
	private DisplayImageOptions options;
	private ImageAdapter imageAdapter;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_image_grid);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
 
        tvMulMsg = (TextView)findViewById(R.id.tvMulMsg);
        
        session = new SessionMngr(getApplicationContext());
        
        final String[] columns = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID };
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        Cursor imagecursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
 
        this.imageUrls = new ArrayList<String>();
 
        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            imageUrls.add(imagecursor.getString(dataColumnIndex));
 
            System.out.println(":>> Array path => "+imageUrls.get(i));
        }
 
        options = new DisplayImageOptions.Builder()
            .showStubImage(R.drawable.stub_image)
            .showImageForEmptyUri(R.drawable.image_for_empty_url)
            .cacheInMemory()
            .cacheOnDisc()
            .build();
 
        imageAdapter = new ImageAdapter(this, imageUrls);
 
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(imageAdapter);
	}
        @Override
    	protected void onStop() {
    		imageLoader.stop();
    		super.onStop();
    	}

    	public void btnChoosePhotosClick(View v){
    		
    		final ArrayList<String> selectedItems = imageAdapter.getCheckedItems();
    		
    		dialog = ProgressDialog.show(Gallery.this, "", "Uploading file...",true);
    		new Thread(new Runnable(){
				public void run(){
					UploadFile(selectedItems);
				}
			}).start();
    	}
        
	public class ImageAdapter extends BaseAdapter{
		
		ArrayList<String> mList;
		LayoutInflater mInflater;
		Context mContext;
		SparseBooleanArray msparseBooleanArray;
		
		public ImageAdapter(Context context, ArrayList<String> imageList) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);
			msparseBooleanArray = new SparseBooleanArray();
			mList = new ArrayList<String>();
			this.mList = imageList;
			imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		}
		
		public ArrayList<String> getCheckedItems(){
			ArrayList<String> mTempArray = new ArrayList<String>();
			
			for(int i=0;i<mList.size();i++){
				if(msparseBooleanArray.get(i)){
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
		public Object getItem(int position) { //argument is position
			return null;
		}
		@Override 
		public long getItemId(int position) { //argument is position
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.row_multiphoto_item, null);
			}
			
			CheckBox mCheckBox = (CheckBox) convertView.findViewById(R.id.checkBox1);
			
			final ImageView imageView =(ImageView) convertView.findViewById(R.id.imageView1);
			
			imageLoader.displayImage("file://"+imageUrls.get(position), imageView, options, new SimpleImageLoadingListener() {
	            @Override
	            public void onLoadingComplete(Bitmap loadedImage) {
	                Animation anim = AnimationUtils.loadAnimation(Gallery.this, R.anim.fade_in);
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
	        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	            msparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
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
					
					sourceFile = new File(sourceFileUri.get(i));
					//boolean flag = _ifIsFile(sourceFile);
					
					if(!_ifIsFile(sourceFile)){
						return 0;
					}
					
					fileName = sourceFile.toString();
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
		Intent i = new Intent(Gallery.this, Dashboard.class);
		startActivity(i);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        ShowDashboard(); 
        return true;
    }
}