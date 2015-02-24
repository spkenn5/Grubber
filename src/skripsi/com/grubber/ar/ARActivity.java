// Copyright 2007-2014 metaio GmbH. All rights reserved.
package skripsi.com.grubber.ar;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;

import skripsi.com.grubber.R;
import skripsi.com.grubber.dao.RestaurantDao;
import skripsi.com.grubber.gps.LocationUtils;
import skripsi.com.grubber.model.Restaurant;
import skripsi.com.grubber.restaurant.RestaurantActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.metaio.cloud.plugin.util.MetaioCloudUtils;
import com.metaio.sdk.ARELInterpreterAndroidJava;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.AnnotatedGeometriesGroupCallback;
import com.metaio.sdk.jni.EGEOMETRY_FOCUS_STATE;
import com.metaio.sdk.jni.IAnnotatedGeometriesGroup;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.IRadar;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.SensorValues;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;
import com.parse.ParseException;

public class ARActivity extends ARViewActivity implements LocationListener,
    GooglePlayServicesClient.ConnectionCallbacks,
    GooglePlayServicesClient.OnConnectionFailedListener {

  protected static final String TAG = ARActivity.class.getSimpleName();
  private IAnnotatedGeometriesGroup mAnnotatedGeometriesGroup;
  // Stores the current instantiation of the location client in this object
  private LocationClient mLocationClient;
  private MyAnnotatedGeometriesGroupCallback mAnnotatedGeometriesGroupCallback;

  String trackingConfigFile;

  private List<IGeometry> mGeoObject = new ArrayList<IGeometry>();
  private List<Restaurant> tempResult = new ArrayList<Restaurant>();
  private List<Restaurant> mResult = new ArrayList<Restaurant>();
  private List<LLACoordinate> LLCoor = new ArrayList<LLACoordinate>();
  private HashMap<IGeometry, Restaurant> markerGeo = new HashMap<IGeometry, Restaurant>();
  private IRadar mRadar;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Set GPS tracking configuration
    boolean result = metaioSDK.setTrackingConfiguration("GPS", false);
    MetaioDebug.log("Tracking data loaded: " + result);
    Log.v(TAG, "Executing getDataTask");
    mLocationClient = new LocationClient(this, this, this);
    new getData().execute();
  }

  /*
   * Called when the Activity is restarted, even before it becomes visible.
   */
  @Override
  public void onStart() {

    super.onStart();

    /*
     * Connect the client. Don't re-start any requests here; instead, wait for onResume()
     */
    mLocationClient.connect();

  }

  @Override
  public void onStop() {

    // After disconnect() is called, the client is considered "dead".
    mLocationClient.disconnect();

    super.onStop();
  }

  @Override
  protected void onDestroy() {
    // Break circular reference of Java objects
    if (mAnnotatedGeometriesGroup != null) {
      mAnnotatedGeometriesGroup.registerCallback(null);
    }

    if (mAnnotatedGeometriesGroupCallback != null) {
      mAnnotatedGeometriesGroupCallback.delete();
      mAnnotatedGeometriesGroupCallback = null;
    }

    super.onDestroy();
  }

  // DRAW ITEM ON AUGMENTED SURFACE

  @Override
  public void onDrawFrame() {
    if (metaioSDK != null && mSensors != null) {
      SensorValues sensorValues = mSensors.getSensorValues();

      float heading = 0.0f;
      if (sensorValues.hasAttitude()) {
        float m[] = new float[9];
        sensorValues.getAttitude().getRotationMatrix(m);

        Vector3d v = new Vector3d(m[6], m[7], m[8]);
        v.normalize();

        heading = (float) (-Math.atan2(v.getY(), v.getX()) - Math.PI / 2.0);
      }

      Rotation rot = new Rotation((float) (Math.PI / 2.0), 0.0f, -heading);
      for (IGeometry geo : mGeoObject) {
        if (geo != null) {
          geo.setRotation(rot);
        }
      }
    }

    super.onDrawFrame();
  }

  public void onButtonClick(View v) {
    Toast.makeText(getBaseContext(), "Loading map...", Toast.LENGTH_LONG).show();
    Intent intent = new Intent(getBaseContext(), GMapActivity.class);
    startActivity(intent);
  }

  @Override
  protected int getGUILayout() {
    return R.layout.activity_ar;
  }

  @Override
  protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
    return null;
  }

  @Override
  protected void loadContents() {
    mAnnotatedGeometriesGroup = metaioSDK.createAnnotatedGeometriesGroup();
    mAnnotatedGeometriesGroupCallback = new MyAnnotatedGeometriesGroupCallback();
    mAnnotatedGeometriesGroup.registerCallback(mAnnotatedGeometriesGroupCallback);

    // Clamp geometries' Z position to range [5000;200000] no matter how close or far they are
    // away.
    // This influences minimum and maximum scaling of the geometries (easier for development).
    metaioSDK.setLLAObjectRenderingLimits(5, 200);

    // Set render frustum accordingly
    metaioSDK.setRendererClippingPlaneLimits(10, 220000);

    // create radar
    mRadar = metaioSDK.createRadar();
    mRadar.setBackgroundTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
        "TutorialLocationBasedAR/Assets/radar2.png"));
    mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
        "TutorialLocationBasedAR/Assets/grub.png"));
    mRadar.setRelativeToScreen(IGeometry.ANCHOR_TL);

    int k = 0;
    for (int i = 0; i < tempResult.size(); i++) {
      if (countDist(tempResult.get(i).getLat(), tempResult.get(i).getLong()) <= 2) {
        Log.v(TAG, "tempResult insert = " + tempResult.get(i).getName());
        Log.v(TAG, "Rest Coord = " + tempResult.get(i).getLat() + " && "
            + tempResult.get(i).getLong());
        // let's create LLA objects for nearby rest
        LLCoor.add(k, new LLACoordinate(tempResult.get(i).getLat(), tempResult.get(i).getLong(), 0,
            0));
        // Load some POIs. Each of them has the same shape at its geoposition. We pass a string
        // (const char*) to IAnnotatedGeometriesGroup::addGeometry so that we can use it as POI
        // title
        // in the callback, in order to create an annotation image with the title on it.

        mGeoObject.add(k, createPOIGeometry(LLCoor.get(k)));
        mAnnotatedGeometriesGroup.addGeometry(mGeoObject.get(k), tempResult.get(i).getName());
        markerGeo.put(mGeoObject.get(k), tempResult.get(i));
        // add geometries to the radar
        mRadar.add(mGeoObject.get(k));
        k++;
      }
    }
  }

  private IGeometry createPOIGeometry(LLACoordinate lla) {
    final File path = AssetsManager.getAssetPathAsFile(getApplicationContext(),
        "TutorialLocationBasedAR/Assets/ExamplePOI.obj");
    if (path != null) {
      IGeometry geo = metaioSDK.createGeometry(path);
      geo.setTranslationLLA(lla);
      geo.setLLALimitsEnabled(true);
      geo.setScale(100);
      return geo;
    } else {
      MetaioDebug.log(Log.ERROR, "Missing files for POI geometry");
      return null;
    }
  }

  @Override
  protected void onGeometryTouched(final IGeometry geometry) {
    MetaioDebug.log("Geometry selected: " + geometry);
    Log.v(TAG, "Object touched!");
    if (markerGeo.containsKey(geometry)) {
      Intent intent = new Intent(ARActivity.this, RestaurantActivity.class);
      intent.putExtra("restObject", markerGeo.get(geometry));
      intent.putExtra("restId", markerGeo.get(geometry).getObjectId());
      intent.putExtra("restName", markerGeo.get(geometry).getName());
      startActivity(intent);
    }
    mSurfaceView.queueEvent(new Runnable() {

      @Override
      public void run() {
        mRadar.setObjectsDefaultTexture(AssetsManager.getAssetPathAsFile(getApplicationContext(),
            "TutorialLocationBasedAR/Assets/yellow.png"));
        mRadar.setObjectTexture(geometry, AssetsManager.getAssetPathAsFile(getApplicationContext(),
            "TutorialLocationBasedAR/Assets/red.png"));
        mAnnotatedGeometriesGroup.setSelectedGeometry(geometry);
      }
    });
  }

  final class MyAnnotatedGeometriesGroupCallback extends AnnotatedGeometriesGroupCallback {
    Bitmap mAnnotationBackground, mEmptyStarImage, mFullStarImage;
    int mAnnotationBackgroundIndex;
    ImageStruct texture;
    String[] textureHash = new String[1];
    TextPaint mPaint;
    Lock geometryLock;

    Bitmap inOutCachedBitmaps[] = new Bitmap[] { mAnnotationBackground, mEmptyStarImage,
        mFullStarImage };
    int inOutCachedAnnotationBackgroundIndex[] = new int[] { mAnnotationBackgroundIndex };

    public MyAnnotatedGeometriesGroupCallback() {
      mPaint = new TextPaint();
      mPaint.setFilterBitmap(true); // enable dithering
      mPaint.setAntiAlias(true); // enable anti-aliasing
    }

    @Override
    public IGeometry loadUpdatedAnnotation(IGeometry geometry, Object userData,
        IGeometry existingAnnotation) {
      if (userData == null) {
        return null;
      }

      if (existingAnnotation != null) {
        // We don't update the annotation if e.g. distance has changed
        return existingAnnotation;
      }

      String title = (String) userData; // as passed to addGeometry
      LLACoordinate location = geometry.getTranslationLLA();
      float distance = (float) MetaioCloudUtils.getDistanceBetweenTwoCoordinates(location,
          mSensors.getLocation());
      // Bitmap thumbnail to be shown in annotation
      Bitmap thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_new);
      try {
        texture = ARELInterpreterAndroidJava.getAnnotationImageForPOI(title, title, distance, "5",
            thumbnail, null, metaioSDK.getRenderSize(), ARActivity.this, mPaint,
            inOutCachedBitmaps, inOutCachedAnnotationBackgroundIndex, textureHash);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if (thumbnail != null)
          thumbnail.recycle();
        thumbnail = null;
      }

      mAnnotationBackground = inOutCachedBitmaps[0];
      mEmptyStarImage = inOutCachedBitmaps[1];
      mFullStarImage = inOutCachedBitmaps[2];
      mAnnotationBackgroundIndex = inOutCachedAnnotationBackgroundIndex[0];

      IGeometry resultGeometry = null;

      if (texture != null) {
        if (geometryLock != null) {
          geometryLock.lock();
        }

        try {
          // Use texture "hash" to ensure that SDK loads new texture if texture changed
          resultGeometry = metaioSDK.createGeometryFromImage(textureHash[0], texture, true, false);
        } finally {
          if (geometryLock != null) {
            geometryLock.unlock();
          }
        }
      }

      return resultGeometry;
    }

    @Override
    public void onFocusStateChanged(IGeometry geometry, Object userData,
        EGEOMETRY_FOCUS_STATE oldState, EGEOMETRY_FOCUS_STATE newState) {
      MetaioDebug.log("onFocusStateChanged for " + (String) userData + ", " + oldState + "->"
          + newState);
    }
  }

  class getData extends AsyncTask<Void, Void, List<Restaurant>> {

    @Override
    protected List<Restaurant> doInBackground(Void... params) {

      try {
        Log.v(TAG, "Retrieving restaurant data");
        tempResult = RestaurantDao.getRest();
        if (tempResult != null) {
          Log.v(TAG, "Populating tempResult");
        }
      } catch (ParseException e) {
        Log.w(TAG, "Problem retrieving restaurants", e);
      }
      return tempResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(List<Restaurant> result) {
      // TODO Auto-generated method stub
      super.onPostExecute(result);
      Log.v(TAG, "Finished getting restaurant list!");
      try {
        // Loading map
        Toast.makeText(getBaseContext(), "Data Loaded, getting more...", Toast.LENGTH_LONG).show();
        Log.v(TAG, "Validating Restaurant Distance to be shown");

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public double countDist(double lat, double lng) {

    Location currentLocation = mLocationClient.getLastLocation();

    double currentLatitude = LocationUtils.getLat(this, currentLocation).getLat();
    double currentLongitude = LocationUtils.getLat(this, currentLocation).getLong();

    double endLatitude = lat;
    double endLongitude = lng;

    float[] results = new float[3];
    Location.distanceBetween(currentLatitude, currentLongitude, endLatitude, endLongitude, results);

    BigDecimal bd = new BigDecimal(results[0]);// results in meters
    BigDecimal rounded = bd.setScale(2, RoundingMode.HALF_UP);
    double values = rounded.doubleValue();

    if (values > 1) {
      values = values * 0.001f;// convert meters to Kilometers
      bd = new BigDecimal(values);
      rounded = bd.setScale(2, RoundingMode.HALF_UP);
      values = rounded.doubleValue();
    }

    return values;
  }

  @Override
  public void onConnectionFailed(ConnectionResult arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onConnected(Bundle arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onDisconnected() {
    // TODO Auto-generated method stub

  }

  @Override
  public void onLocationChanged(Location arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.ar, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
    case R.id.action_refresh:
      // Refresh coordinates
      return true;
    case R.id.action_back:
      finish();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }
}
