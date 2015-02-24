package skripsi.com.grubber;

import skripsi.com.grubber.model.User;
import skripsi.com.grubber.timeline.TimelineActivity;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.ParseAnalytics;
import com.parse.ParseUser;

public class MainActivity extends ActionBarActivity implements
    LoginFragment.OnFragmentInteractionListener, RegisterFragment.OnFragmentInteractionListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  private static final int FRAGMENT_LOGIN = 1;
  private static final int FRAGMENT_REGISTER = 2;
  private boolean regis = true;
  private boolean editProfile = false;
  private boolean isLoginFragment = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ActionBar actionBar = getActionBar();
    actionBar.hide();
    
    //coba
    
    
    final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
    Log.d(TAG, "OpenGL version  : " + configurationInfo.getGlEsVersion());

    Context context = this.getApplicationContext();
    SharedPreferences settings = context.getSharedPreferences("PREFERENCES", 0);
    boolean isLogged = settings.getBoolean("isLogged", false);

    // Parse
    ParseAnalytics.trackAppOpened(getIntent());

    if (savedInstanceState == null && ParseUser.getCurrentUser() == null) {
      isLoginFragment = true;
      showFragment(FRAGMENT_LOGIN, false);
      
    }
    else if(getIntent().getExtras() != null) {
    	Log.d(TAG, "Edit Profile  : " + getIntent().getExtras().getBoolean("editMode"));
    	editProfile = getIntent().getBooleanExtra("editMode", true);
    	showFragment(FRAGMENT_REGISTER, false);
    }
    else if (ParseUser.getCurrentUser() != null) {
      gotoNextPage();
    } 
    else {
      boolean isActionBarShowing = savedInstanceState.getBoolean("isActionBarShowing");
      if (isActionBarShowing) {
        /*
         * getSupportActionBar().show();
         * this.getSupportActionBar().setIcon(R.drawable.ic_navigation_drawer);
         */
        getSupportActionBar().hide();
      } else {
        getSupportActionBar().hide();
      }
    }
    
    
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return false;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return false;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showFragment(int fragmentIndex, boolean addToBackStack) {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction transaction = fm.beginTransaction();
    Fragment f = null;
    switch (fragmentIndex) {
    case FRAGMENT_LOGIN:
      Log.v(TAG, "Showing Main Screen");
      if (findViewById(R.id.registration_scroll_view) == null) {
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
            R.anim.slide_in_left, R.anim.slide_out_right);
        f = Fragment.instantiate(this, LoginFragment.class.getName());
        if (addToBackStack)
          transaction.addToBackStack(null);
        transaction.replace(R.id.content_frame, f);
        transaction.commit();
      }
      /*
       * this.getSupportActionBar().show();
       * this.getSupportActionBar().setTitle(R.string.grubber_main_register);
       * this.getSupportActionBar().setIcon(R.drawable.ic_launcher_new);
       */
      getSupportActionBar().hide();
      break;
    case FRAGMENT_REGISTER:
      Log.v(TAG, "Showing Registration");
      if (findViewById(R.id.registration_scroll_view) == null) {
    	  
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
            R.anim.slide_in_left, R.anim.slide_out_right);
        
        if(editProfile) {
        	Bundle bundle = new Bundle();
            bundle.putBoolean("editMode", true);
            f = new RegisterFragment();
            f.setArguments(bundle);
            transaction.replace(R.id.content_frame, f);
            transaction.commit();
    	}
        else {
	        f = Fragment.instantiate(this, RegisterFragment.class.getName());
	        if (addToBackStack)
	          transaction.addToBackStack(null);
	        transaction.replace(R.id.content_frame, f);
	        transaction.commit();
        }
      }
      /*
       * this.getSupportActionBar().show();
       * this.getSupportActionBar().setTitle(R.string.grubber_main_register);
       * this.getSupportActionBar().setIcon(R.drawable.ic_launcher_new);
       */
      getSupportActionBar().hide();
      break;
    }
  }

  private void gotoNextPage() {
    Log.v(TAG, "Go Timeline");
    Intent timeline = new Intent(getBaseContext(), TimelineActivity.class);
    startActivity(timeline);
    this.finish();
  }

  @Override
  public void onSuccessfulRegistration() {
    // successful registration
    Log.d(TAG, "Returned to MainActivity after successful registration");
    gotoNextPage();
  }

  @Override
  public void onRegisterAction() {
    Log.v(TAG, "onRegisterAction");
    isLoginFragment = false;
    showFragment(FRAGMENT_REGISTER, true);
  }

  @Override
  public void onBackPressed() {
    Log.v(TAG, "onBackPressed");
    super.onBackPressed();
    if (!regis) {
      Intent intent = new Intent(getBaseContext(), TimelineActivity.class);
      startActivity(intent);
    } /*else {
      finish();
    }*/
    else if(!isLoginFragment && !editProfile) {
    	isLoginFragment = true;
    }
  }

  @Override
  public void onLoginAction(User user) {
    Log.v(TAG, "onLoginAction");
    Context context = this.getApplicationContext();
    SharedPreferences settings = context.getSharedPreferences("PREFERENCES", 0);
    SharedPreferences.Editor editor = settings.edit();
    editor.putBoolean("isLogged", true);
    editor.commit();
    gotoNextPage();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Handle the back button
    if (keyCode == KeyEvent.KEYCODE_BACK && isLoginFragment) {
      // Ask the user if they want to quit
	      new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
	          .setTitle(R.string.grubber_quit).setMessage(R.string.grubber_confirm_quit)
	          .setPositiveButton(R.string.grubber_quit_yes, new DialogInterface.OnClickListener() {
	
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	
	              // Stop the activity
	              MainActivity.this.finish();
	            }
	
	          }).setNegativeButton(R.string.grubber_quit_no, null).show();
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }

  }

  @Override
  public void onSuccessfulEdit() {
    // TODO Auto-generated method stub
    // successful edit
    Log.d(TAG, "Returned to MainActivity after successful edit");
    Intent intent = new Intent(this, TimelineActivity.class);
    startActivity(intent);
  }

  @Override
  public void onResetPasswordAction() {
    // TODO Auto-generated method stub

  }
}
