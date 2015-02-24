package skripsi.com.grubber.timeline;

import java.util.List;

import skripsi.com.grubber.BaseActivity;
import skripsi.com.grubber.MainActivity;
import skripsi.com.grubber.R;
import skripsi.com.grubber.ar.ARActivity;
import skripsi.com.grubber.dao.ActivityDao;
import skripsi.com.grubber.model.Activity;
import skripsi.com.grubber.model.User;
import skripsi.com.grubber.notifications.NotificationTask;
import skripsi.com.grubber.notifications.NotificationTask.NotificationResult;
import skripsi.com.grubber.notifications.Notifications;
import skripsi.com.grubber.profile.ProfileFragment;
import skripsi.com.grubber.trending.TrendingFragment;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.Toast;

import com.parse.ParseException;
import com.readystatesoftware.viewbadger.BadgeView;

public class TimelineActivity extends BaseActivity implements NotificationResult {

  /* Tab Identifiers */
  static String timeline_tab = "Timeline";
  TabHost tabhost;
  TimelineListPostActivity timeline;
  TrendingFragment trending;
  MainActivity MapList;
  ARActivity ar_tab;
  Notifications notif;
  ProfileFragment profile;
  List<Activity> mResult;
  
  Dialog noConnectionDialog;

  public TabWidget tabs;
  public BadgeView badge;
  int count_notif;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.timeline_main_tab);

    /* Action Bar */
    setTitle("GRUBBER");
    setHomeAsUp(false);
    show();
    connectionAlertDialog();

    /* Tab */
    timeline = new TimelineListPostActivity();
    trending = new TrendingFragment();
    ar_tab = new ARActivity();
    MapList = new MainActivity();
    notif = new Notifications();
    profile = new ProfileFragment();

    tabhost = (TabHost) findViewById(android.R.id.tabhost);
    tabhost.setOnTabChangedListener(listener);
    tabhost.setup();

    initializeTab();
    
    //new RemoteDataTask().execute();
    new NotificationTask(this).execute();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return false;
  }

  /* Initialize the tabs and set views and identifiers for the tabs */
  public void initializeTab() {

    // Do the same for the other tabs

    TabHost.TabSpec spec = tabhost.newTabSpec("Timeline");
    spec.setContent(new TabHost.TabContentFactory() {

      @Override
      public View createTabContent(String tag) {
        // TODO Auto-generated method stub
        return findViewById(android.R.id.tabcontent);
      }
    });
    spec.setIndicator(createTabView(R.drawable.home, "Grubber"));
    tabhost.addTab(spec);

    spec = tabhost.newTabSpec("Trending");
    spec.setContent(new TabHost.TabContentFactory() {

      @Override
      public View createTabContent(String tag) {
        // TODO Auto-generated method stub
        return findViewById(android.R.id.tabcontent);
      }
    });
    spec.setIndicator(createTabView(R.drawable.trending, "Trending"));
    tabhost.addTab(spec);

    spec = tabhost.newTabSpec("Grub!");
    spec.setContent(new TabHost.TabContentFactory() {

      @Override
      public View createTabContent(String tag) {
        // TODO Auto-generated method stub
        return findViewById(android.R.id.tabcontent);
      }
    });
    spec.setIndicator(createTabView(R.drawable.ar, "Grub!"));
    tabhost.addTab(spec);

    spec = tabhost.newTabSpec("Notifications");
    spec.setContent(new TabHost.TabContentFactory() {

      @Override
      public View createTabContent(String tag) {
        // TODO Auto-generated method stub
        return findViewById(android.R.id.tabcontent);
      }
    });
    spec.setIndicator(createTabView(R.drawable.notif, "Notification"));
    tabhost.addTab(spec);

    spec = tabhost.newTabSpec("Profile");
    spec.setContent(new TabHost.TabContentFactory() {

      @Override
      public View createTabContent(String tag) {
        // TODO Auto-generated method stub
        return findViewById(android.R.id.tabcontent);
      }
    });
    spec.setIndicator(createTabView(R.drawable.profile, "Profile"));
    tabhost.addTab(spec);

    tabs = (TabWidget) findViewById(android.R.id.tabs);
    badge = new BadgeView(this, tabs, 3);

    tabhost.getTabWidget().getChildAt(0).setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        // TODO Auto-generated method stub
        if (tabhost.getCurrentTab() != 0) {
          tabhost.setCurrentTab(0);
        } else {
          Log.d("Timeline", "!!!!Clicked");
          timeline.scrollToUp();
        }
      }
    });

    Log.d("count_notif", String.format("%s", count_notif));

  }

  /* TabChangeListener for changing the tab when one of the tabs is pressed */
  TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {

    @Override
    public void onTabChanged(String tabId) {
      // TODO Auto-generated method stub

      // Set current tab

      if (tabId.equals("Timeline")) {
        pushFragments(tabId, timeline);
      } else if (tabId.equals("Trending")) {
        pushFragments(tabId, trending);
      } else if (tabId.equals("Grub!")) {
        Toast.makeText(getBaseContext(), "Loading Augmented Reality Modules...", Toast.LENGTH_LONG)
            .show();
        Intent intent = new Intent(getBaseContext(), ARActivity.class);
        startActivity(intent);
      } else if (tabId.equals("Notifications")) {
        pushFragments(tabId, notif);
      } else
        pushFragments(tabId, profile);
    }
  };

  public void pushFragments(String tabId, Fragment fragment) {
    // TODO Auto-generated method stub
	
    FragmentManager manager = getSupportFragmentManager();
    FragmentTransaction ft = manager.beginTransaction();
    
      
    if (fragment.isAdded()) {
      ft.show(fragment);
    } else {
      if(isNetworkAvailable()) {
    	  ft.replace(android.R.id.tabcontent, fragment);
      }
      else {
    	  ft.replace(android.R.id.tabcontent, timeline);
    	  noConnectionDialog.show();
      }
    }
    setTitle(tabId);
    show();
    ft.commit();
    
  }

  /* returns the tab view i.e. the tab icon */
  private View createTabView(final int id, final String name_tab) {
    // TODO Auto-generated method stub
    View view = LayoutInflater.from(this).inflate(R.layout.tab_icon, null);
    ImageView image_view = (ImageView) view.findViewById(R.id.icon_image);
    image_view.setImageDrawable(getResources().getDrawable(id));
    /*
     * TextView text_tab = (TextView) view.findViewById(R.id.text_tab); text_tab.setText(name_tab);
     */

    return view;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      profile.editProfileAction();
      // return false;
    } else if (id == R.id.action_help) {
      profile.helpAction();
      // return false;
    } else if (id == R.id.action_logout) {
      profile.logoutAction();
      // return false;
    }

    return super.onOptionsItemSelected(item);
  }
  
  public boolean isNetworkAvailable() {
	  
	  ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	  NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	  return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
		
  }
  
  public void connectionAlertDialog() {
	  	noConnectionDialog = new Dialog(this);
		noConnectionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		noConnectionDialog.setContentView(R.layout.alert_box);
		
		Button dialogBtn = (Button) noConnectionDialog.findViewById(R.id.button1);
		
		dialogBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				tabhost.setCurrentTab(0);
				setTitle("Timeline");
				show();
				noConnectionDialog.dismiss();
			}
		});
}

  /*public class RemoteDataTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

    }

    @Override
    protected Void doInBackground(Void... params) {
      // TODO Auto-generated method stub
      try {
        mResult = ActivityDao.getNotifications(User.getCurrentUser(), "yes");
        Log.d("Jumlah notif1", String.format("getNotifications found %s records",
            mResult == null ? 0 : mResult.size()));
      } catch (ParseException e) {
        Log.v("pas awal banget", "Failed to get post list for current user");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      count_notif = mResult.size();
      Log.d("Jumlah notif",
          String.format("getNotifications found %s records", mResult == null ? 0 : count_notif));
      if (count_notif != 0) {
        badge.setText(String.valueOf(count_notif));
        badge.setTextSize(11);
        badge.setBadgeMargin(35, 20);
        badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        badge.show();
      }
    }
  }*/

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();

  }

	@Override
	public void badgeConfiguration(int totalNotif) {
		// TODO Auto-generated method stub
		badge.setText(String.valueOf(totalNotif));
        badge.setTextSize(11);
        badge.setBadgeMargin(35, 20);
        badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        badge.show();
	}
}
