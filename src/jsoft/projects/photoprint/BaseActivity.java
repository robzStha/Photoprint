package jsoft.projects.photoprint;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
 
public abstract class BaseActivity extends Activity {
 
    public ImageLoader imageLoader = ImageLoader.getInstance();
 
}