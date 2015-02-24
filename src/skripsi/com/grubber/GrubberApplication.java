package skripsi.com.grubber;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import skripsi.com.android.Utility;
import skripsi.com.grubber.model.Activity;
import skripsi.com.grubber.model.Restaurant;
import skripsi.com.grubber.profile.HelpFragment;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.PushService;

public class GrubberApplication extends Application {
  public static final String TAG = GrubberApplication.class.getSimpleName();

  public static final String PARSE_APP_ID = "com.parse.APP_ID";
  public static final String PARSE_CLIENT_KEY = "com.parse.CLIENT_KEY";

  // This is for caching bitmaps
  private LruCache<String, Bitmap> mBitmapCache;
  private WeakHashMap<String, BitmapWorkerTask> mBitmapATask;
  private AssetsExtracter mTask;

  @Override
  public void onCreate() {
    super.onCreate();
    Log.v(TAG, "Initializing Parse");
    initializeParse();
    mTask = new AssetsExtracter();
    mTask.execute(0);

    PushService.setDefaultPushCallback(this, MainActivity.class);
    ParseInstallation.getCurrentInstallation().saveInBackground();

  }

  private void initializeParse() {
    // ParseObject subclass registration
    ParseObject.registerSubclass(Restaurant.class);
    ParseObject.registerSubclass(Activity.class);
    // enabling local data store
    Parse.enableLocalDatastore(this);
    // initialize Parse
    Parse.initialize(this, Utility.getMetadata(this, GrubberApplication.PARSE_APP_ID),
        Utility.getMetadata(this, GrubberApplication.PARSE_CLIENT_KEY));
    // by default, all objects are public
    ParseACL defaultACL = new ParseACL();
    defaultACL.setPublicReadAccess(true);
    defaultACL.setPublicWriteAccess(true);
    ParseACL.setDefaultACL(defaultACL, true);

  }

  public static void showTutorial(Context context) {
    Intent i = new Intent(context, HelpFragment.class);
    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(i);
  }

  public static void showTimeline(Context context) {
    // Intent i = new Intent(context, TimelineActivity.class);
    // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    // context.startActivity(i);
    Log.v(TAG, "Timeline haha");
  }

  public static void showSearch(Context context) {
    throw new UnsupportedOperationException();
  }

  private void logoutInternally(Context context) {
    ParseUser.logOut();

    Intent i = new Intent(context, MainActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(i);
  }

  public void loadBitmap(ParseFile pf, ImageView imageView, int defaultImage, boolean framed) {
    if (pf != null) {
      final String imageKey = pf.getName();

      Bitmap bm = null;
      try {
        if (imageKey != null && pf.getData() != null) {
          bm = BitmapFactory.decodeByteArray(pf.getData(), 0, pf.getData().length);
        }
      } catch (ParseException e) {
        Log.w(TAG, "Error while creating bitmap from data", e);
      }

      final Bitmap bitmap = imageKey != null ? getBitmapFromMemCache(imageKey) : bm;
      if (bitmap != null) {
        Log.v(TAG, String.format("ImageCache hit for %s", pf.getName()));
        imageView.setImageBitmap(bitmap);
      } else {
        Log.v(TAG, String.format("ImageCache miss for %s", pf.getName()));
        Bitmap defaultImageBm = getBitmapFromMemCache(String.valueOf(defaultImage));
        if (defaultImageBm == null) {
          if (framed) {
            defaultImageBm = Utility.getFramedBitmap(this.getResources(),
                BitmapFactory.decodeResource(getResources(), defaultImage));
          } else {
            defaultImageBm = BitmapFactory.decodeResource(getResources(), defaultImage);
          }
          addBitmapToMemoryCache(String.valueOf(defaultImage), defaultImageBm);
        }
        imageView.setImageBitmap(defaultImageBm);
        BitmapWorkerTask bwt = mBitmapATask.get(imageKey);
        if (bwt == null) {
          Log.v(TAG, String.format("ImageCache triggering async task for %s", pf.getName()));
          BitmapWorkerTask task = new BitmapWorkerTask(imageView, framed);
          task.execute(pf);
          mBitmapATask.put(imageKey, task);
        } else {
          bwt.addAdditionaImageView(imageView);
          Log.v(TAG, String.format("ImageCache skip loading the same image for %s", pf));
        }
      }
    } else {
      Bitmap defaultImageBm = getBitmapFromMemCache(String.valueOf(defaultImage));
      if (defaultImageBm == null) {
        defaultImageBm = Utility.getFramedBitmap(this.getResources(),
            BitmapFactory.decodeResource(getResources(), defaultImage));
        addBitmapToMemoryCache(String.valueOf(defaultImage), defaultImageBm);
      }
      imageView.setImageBitmap(defaultImageBm);
    }
  }

  public Bitmap getBitmapFromMemCache(String key) {
    return mBitmapCache.get(key);
  }

  public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    if (getBitmapFromMemCache(key) == null) {
      mBitmapCache.put(key, bitmap);
    }
  }

  private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean> {

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
      try {
        // Extract all assets and overwrite existing files if debug build
        AssetsManager.extractAllAssets(getApplicationContext(), BuildConfig.DEBUG);
      } catch (IOException e) {
        MetaioDebug.log(Log.ERROR, "Error extracting assets: " + e.getMessage());
        MetaioDebug.printStackTrace(Log.ERROR, e);
        return false;
      }

      return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
      if (result) {
        // Start AR Activity on success
        Log.v(TAG, "Success!");
      } else {
        // Show a toast with an error message
        Toast toast = Toast.makeText(getApplicationContext(),
            "Error extracting application assets!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
      }

    }

  }

  class BitmapWorkerTask extends AsyncTask<ParseFile, Void, Bitmap> {
    private WeakReference<ImageView> imageView;
    private Map<ImageView, Void> additionalImageViews;
    private boolean framed;

    public BitmapWorkerTask(ImageView imageView, boolean framed) {
      imageView.setTag(R.id.tag_bitmap_worker, this);
      this.imageView = new WeakReference<ImageView>(imageView);
      this.framed = framed;
    }

    public void addAdditionaImageView(ImageView imageView) {
      if (additionalImageViews == null) {
        additionalImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, Void>());
      }
      imageView.setTag(R.id.tag_bitmap_worker, this);
      additionalImageViews.put(imageView, null);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(ParseFile... params) {
      Bitmap bitmap = null, bitmap2 = null;
      try {
        byte[] data = params[0].getData();
        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (framed) {
          bitmap2 = Utility.getFramedBitmap(GrubberApplication.this.getResources(), bitmap);
          bitmap.recycle();
          addBitmapToMemoryCache(String.valueOf(params[0].getName()), bitmap2);
        } else {
          addBitmapToMemoryCache(String.valueOf(params[0].getName()), bitmap);
        }
      } catch (ParseException e) {
        Log.w(
            TAG,
            String.format("Error reading from ParseFile %s [%s]", params[0].getName(),
                params[0].getUrl()), e);
      }
      return framed ? bitmap2 : bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      super.onPostExecute(result);
      if (result != null && this.imageView != null && this.imageView.get() != null
          && this.equals(this.imageView.get().getTag(R.id.tag_bitmap_worker))) {
        this.imageView.get().setImageBitmap(result);
        if (additionalImageViews != null && !additionalImageViews.isEmpty()) {
          Log.v(TAG, String.format("We have %s additional ImageViews to load",
              additionalImageViews.size()));
          for (ImageView iv : additionalImageViews.keySet()) {
            if (iv.getTag(R.id.tag_bitmap_worker) == this) {
              iv.setImageBitmap(result);
            }
          }
        }
      }
    }
  }
}
