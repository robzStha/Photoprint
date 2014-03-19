//package jsoft.projects.photoprint.libs;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.HttpStatus;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import android.os.AsyncTask;
//import android.widget.AbsListView;
//import android.widget.AbsListView.OnScrollListener;
//
//import com.facebook.internal.Utility;
//
//public class GetPhotosData extends AsyncTask<Void, Void, Void>{
//
//	// HOLD THE URL TO MAKE THE API CALL TO
//		private String URL;
//
//		// STORE THE PAGING URL
//		private String pagingURL;
//
//		// FLAG FOR CURRENT PAGE
//		int current_page = 1;
//
//		// BOOLEAN TO CHECK IF NEW FEEDS ARE LOADING
//		Boolean loadingMore = true;
//		Boolean stopLoadingData = false;
//	
//	
//	@Override
//	protected Void doInBackground(Void... arg0) {
//		// CHANGE THE LOADING MORE STATUS TO PREVENT DUPLICATE CALLS FOR
//        // MORE DATA WHILE LOADING A BATCH
//        loadingMore = true;
//
//        // SET THE INITIAL URL TO GET THE FIRST LOT OF ALBUMS
//        URL = "https://graph.facebook.com/" + initialAlbumID
//                + "/photos&access_token="
//                + Utility.mFacebook.getAccessToken() + "?limit=10";
//
//        try {
//
//            HttpClient hc = new DefaultHttpClient();
//            HttpGet get = new HttpGet(URL);
//            HttpResponse rp = hc.execute(get);
//
//            if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                String queryAlbums = EntityUtils.toString(rp.getEntity());
//
//                JSONObject JOTemp = new JSONObject(queryAlbums);
//
//                JSONArray JAPhotos = JOTemp.getJSONArray("data");
//
//                // IN MY CODE, I GET THE NEXT PAGE LINK HERE
//
//                getPhotos photos;
//
//                for (int i = 0; i < JAPhotos.length(); i++) {
//                    JSONObject JOPhotos = JAPhotos.getJSONObject(i);
//                    // Log.e("INDIVIDUAL ALBUMS", JOPhotos.toString());
//
//                    if (JOPhotos.has("link")) {
//
//                        photos = new getPhotos();
//
//                        // GET THE ALBUM ID
//                        if (JOPhotos.has("id")) {
//                            photos.setPhotoID(JOPhotos.getString("id"));
//                        } else {
//                            photos.setPhotoID(null);
//                        }
//
//                        // GET THE ALBUM NAME
//                        if (JOPhotos.has("name")) {
//                            photos.setPhotoName(JOPhotos.getString("name"));
//                        } else {
//                            photos.setPhotoName(null);
//                        }
//
//                        // GET THE ALBUM COVER PHOTO
//                        if (JOPhotos.has("picture")) {
//                            photos.setPhotoPicture(JOPhotos
//                                    .getString("picture"));
//                        } else {
//                            photos.setPhotoPicture(null);
//                        }
//
//                        // GET THE PHOTO'S SOURCE
//                        if (JOPhotos.has("source")) {
//                            photos.setPhotoSource(JOPhotos
//                                    .getString("source"));
//                        } else {
//                            photos.setPhotoSource(null);
//                        }
//
//                        arrPhotos.add(photos);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//	}
//	
//	@Override
//    protected void onPostExecute(Void result) {
//
//        // SET THE ADAPTER TO THE GRIDVIEW
//        gridOfPhotos.setAdapter(adapter);
//
//        // CHANGE THE LOADING MORE STATUS
//        loadingMore = false;
//    }
//
//	// ONSCROLLLISTENER
////	gridOfPhotos.setOnScrollListener(new OnScrollListener() {
////
////	    @Override
////	    public void onScrollStateChanged(AbsListView view, int scrollState) {
////
////	    }
////
////	    @Override
////	    public void onScroll(AbsListView view, int firstVisibleItem,
////	            int visibleItemCount, int totalItemCount) {
////	        int lastInScreen = firstVisibleItem + visibleItemCount;
////	        if ((lastInScreen == totalItemCount) && !(loadingMore)) {
////
////	            if (stopLoadingData == false) {
////	                // FETCH THE NEXT BATCH OF FEEDS
////	                new loadMorePhotos().execute();
////	            }
////
////	        }
////	    }
////	});
//	
//	
//}
