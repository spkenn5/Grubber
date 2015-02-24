package skripsi.com.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.parse.ParseFile;

public class Utility {
  public static final boolean DEBUG = false;

  private static final String TAG = Utility.class.getSimpleName();

  public static long MINUTES = 60; // 60 seconds
  public static long HOURS = 60 * MINUTES; // 3600 seconds
  public static long DAYS = 24 * HOURS; // 86400 seconds
  public static final int MAX_DAYS = 30;

  public static Map<String, Integer> colorsCache = new HashMap<String, Integer>();
  public static Map<String, Integer> textStyleCache = new HashMap<String, Integer>();

  public static String getTrimmedText(Editable e) {
    String result = null;
    if (e != null) {
      result = e.toString().trim();
    }
    return result;
  }

  public static String convertStringToRegex(String originalText) {
    StringBuilder sb = new StringBuilder();
    if (originalText != null) {
      sb.append("(");
      String[] strings = originalText.split("\\s");
      for (String s : strings) {
        sb.append("(?=.*?(").append(Pattern.quote(s)).append("))");
      }
      sb.append(")+");
    }
    return sb.toString();
  }

  public static String getMetadata(Context context, String key) {
    try {
      ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
          PackageManager.GET_META_DATA);
      if (ai.metaData != null) {
        return ai.metaData.getString(key);
      }
    } catch (PackageManager.NameNotFoundException e) {
      // if we can't find it in the manifest, just return null
    }

    return null;
  }

  public static List<String> getLocaleCountries() {
    Locale[] locales = Locale.getAvailableLocales();
    List<String> countries = new ArrayList<String>();
    for (Locale locale : locales) {
      String country = locale.getDisplayCountry();
      if (country.trim().length() > 0 && !countries.contains(country)) {
        countries.add(country);
      }
    }
    Collections.sort(countries);
    return countries;
  }

  public static Locale getLocaleFromString(String s) {
    Locale result = Locale.getDefault();
    if (s != null) {
      StringTokenizer st = new StringTokenizer(s, "_");
      String language = st.nextToken();
      String country = st.hasMoreTokens() ? st.nextToken() : null;
      result = new Locale(language, country);
    }
    return result;
  }

  public static ParseFile savePicture(Bitmap bitmap, String filename) {
    ParseFile result = null;
    ByteArrayOutputStream baos = null;
    try {
      baos = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
      baos.flush();
      result = new ParseFile(filename, baos.toByteArray());
    } catch (Exception e1) {
      Log.w(TAG, "Problem saving Picture", e1);
    } finally {
      if (baos != null) {
        try {
          baos.close();
        } catch (IOException e) {
        }
      }
    }
    return result;
  }

  public static void lockScreenOrientation(Activity activity) {
    int currentOrientation = activity.getResources().getConfiguration().orientation;
    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
      activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    } else {
      activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
  }

  public static void unlockScreenOrientation(Activity activity) {
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
  }

  public static Bitmap getBitmapFromDrawable(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
        drawable.getIntrinsicHeight(), Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }

  public static Bitmap getFramedBitmap(Resources r, Bitmap original) {
    Drawable imageDrawable = new BitmapDrawable(r, original);
    int width = original.getWidth();
    int height = original.getHeight();
    Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    Canvas canvas = new Canvas(output);

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(Color.RED);
    paint.setStyle(Style.FILL);
    RectF innerRect = new RectF(0, 0, width - 4, height - 4);
    float innerRadius = width / 9f;

    canvas.drawRoundRect(innerRect, innerRadius, innerRadius, paint); // red
    // mask

    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    imageDrawable.setBounds(2, 2, width - 2, height - 2); // we're reducing
    // size by 2px
    // at all sides
    canvas.saveLayer(innerRect, paint, Canvas.ALL_SAVE_FLAG);

    canvas.translate(-2, -2); // but we translate by -2 because the mask is
    // at 0, 0
    imageDrawable.draw(canvas); // the image should be cropped 1px at all
    // sides
    canvas.restore();

    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
    paint.setColor(Color.BLACK);
    paint.setStyle(Style.FILL);
    paint.setAlpha(200);
    canvas.translate(0, 0);
    RectF secondInnerRect = new RectF(2, 2, width - 2, height - 2);
    canvas.drawRoundRect(secondInnerRect, innerRadius, innerRadius, paint);

    return output;
  }

  @TargetApi(VERSION_CODES.HONEYCOMB)
  public static void enableStrictMode() {
    if (Utility.hasGingerbread()) {
      StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
          .detectAll().penaltyLog();
      StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll()
          .penaltyLog();

      StrictMode.setThreadPolicy(threadPolicyBuilder.build());
      StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }
  }

  public static boolean hasFroyo() {
    // Can use static final constants like FROYO, declared in later versions
    // of the OS since they are inlined at compile time. This is guaranteed behavior.
    return Build.VERSION.SDK_INT >= VERSION_CODES.FROYO;
  }

  public static boolean hasGingerbread() {
    return Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD;
  }

  public static boolean hasHoneycomb() {
    return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
  }

  public static boolean hasHoneycombMR1() {
    return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;
  }

  public static boolean hasJellyBean() {
    return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
  }

  public static <T> void syncList(ArrayAdapter<T> target, List<T> source) {
    List<T> toRemove = new ArrayList<T>();
    for (int i = 0; i < target.getCount(); i++) {
      T t = target.getItem(i);
      if (!source.contains(t)) {
        toRemove.add(t);
      }
    }
    Iterator<T> i = toRemove.iterator();
    while (i.hasNext()) {
      T t = i.next();
      Log.v(TAG, String.format("Removing %s", t));
      target.remove(t);
    }

    for (int j = 0; j < source.size(); j++) {
      T t = source.get(j);
      if (target.getCount() <= j || !target.getItem(j).equals(t)) {
        Log.v(TAG, String.format("Inserting %s to index %s", t, j));
        target.insert(t, j);
      }
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T extends Fragment> T getFragmentByType(Class<T> cls, FragmentManager fm) {
    if (fm != null && fm.getFragments() != null) {
      List<Fragment> fragments = fm.getFragments();
      for (Iterator iterator = fragments.iterator(); iterator.hasNext();) {
        Fragment fragment = (Fragment) iterator.next();
        if (cls.isInstance(fragment)) {
          return (T) fragment;
        }
      }
    }
    return null;
  }

}
