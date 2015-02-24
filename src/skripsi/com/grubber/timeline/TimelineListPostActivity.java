package skripsi.com.grubber.timeline;

import java.util.ArrayList;
import java.util.List;

import skripsi.com.grubber.R;
import skripsi.com.grubber.adapter.PostListAdapter;
import skripsi.com.grubber.dao.ActivityDao;
import skripsi.com.grubber.model.Activity;
import skripsi.com.grubber.model.User;
import skripsi.com.grubber.notifications.NotificationTask;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.widget.FacebookDialog;
import com.parse.ParseException;
import com.parse.ParseUser;

public class TimelineListPostActivity extends Fragment {

  protected static final String TAG = TimelineListPostActivity.class.getSimpleName();
  // Your Facebook APP ID
  private static String APP_ID = "696866727099220"; // Replace your App ID here
  // Instance of Facebook Class
  private Facebook facebook;
  private UiLifecycleHelper uiHelper;
  private AsyncFacebookRunner mAsyncRunner;
  String FILENAME = "AndroidSSO_data";
  private SharedPreferences mPrefs;
  private Button mFBShare = null;
  private Button mFBLogin = null;

  ListView listview;
  List<Activity> m1Result;
  List<Activity> mResult;
  List<ParseUser> uResult;
  ProgressDialog timeline_progress;
  ProgressBar loading_spinner;
  PostListAdapter mAdapter;
  int index = 0;
  int top = 0;

  SwipeRefreshLayout refresh;

  private Session.StatusCallback callback = new Session.StatusCallback() {
    @Override
    public void call(Session session, SessionState state, Exception exception) {
      onSessionStateChange(session, state, exception);
    }
  };

  // public void login() {
  // Session session = Session.getActiveSession();
  // if (!session.isOpened() && !session.isClosed()) {
  // session.openForRead(new Session.OpenRequest(this).setCallback(callback));
  // } else {
  // Session.openActiveSession(getActivity(), this, true, callback);
  // }
  // }
  //
  // private class SessionStatusCallback implements Session.StatusCallback {
  // @Override
  // public void call(Session session, SessionState state, Exception exception) {
  // if (exception != null) {
  // // handleException(exception);
  // Log.e(TAG, exception.getMessage());
  // }
  // if (state.isOpened()) {
  //
  // } else if (state.isClosed()) {
  // login();
  // }
  // }
  // }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    facebook = new Facebook(APP_ID);
    mAsyncRunner = new AsyncFacebookRunner(facebook);
    uiHelper = new UiLifecycleHelper(getActivity(), callback);
    uiHelper.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
      @Override
      public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
        Log.e("Activity", String.format("Error: %s", error.toString()));
      }

      @Override
      public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
        Log.i("Activity", "Success!");
      }
    });
  }

  private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    if (state.isOpened()) {
      // System.out.println("Logged in...");
      Log.v(TAG, "Logged in");
    } else if (state.isClosed()) {
      // System.out.println("Logged out...");
      Log.v(TAG, "Logged out");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_postlist_timeline, container, false);
    listview = (ListView) view.findViewById(R.id.list_timeline);
    loading_spinner = (ProgressBar) view.findViewById(R.id.loading_spinner);
    loading_spinner.setVisibility(View.GONE);

    refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
    refresh.setOnRefreshListener(new OnRefreshListener() {

      @Override
      public void onRefresh() {
        // TODO Auto-generated method stub
        if (((TimelineActivity) getActivity()).isNetworkAvailable()) {
          new NotificationTask(getActivity()).execute();
          new RemoteDataTask(1).execute();
        } else {
          refresh.setRefreshing(false);
          ((TimelineActivity) getActivity()).noConnectionDialog.show();
        }
      }
    });
    //
    refresh.setColorScheme(R.color.progressbar_1, R.color.progressbar_2, R.color.progressbar_3,
        R.color.progressbar_4);

    m1Result = new ArrayList<Activity>();
    mAdapter = new PostListAdapter(getActivity(), m1Result);
    listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

      public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
        Log.v(TAG, "Content Clicked!");
        postContentToWall(m1Result.get(pos));
        return true;
      }

    });
    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        // Send single item click data to SingleItemView Class
        Intent intent = new Intent(getActivity(), Comment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("reviewId", m1Result.get(arg2).getObjectId());

        if (m1Result.get(arg2).getCreatedBy().getObjectId()
            .equals(User.getCurrentUser().getObjectId())) {
          intent.putExtra("commentStatus", "Read");
          Log.d("commentStatus", "ReadAll");
        } else {
          intent.putExtra("commentStatus", "Unread");
          Log.d("commentStatus", "NoAccess");
        }
        startActivity(intent);
      }
    });
    listview.setAdapter(mAdapter);

    Log.d(TAG, "view created");
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onViewCreated(view, savedInstanceState);
    if (mResult == null) {
      refresh.setRefreshing(true);
      loading_spinner.setVisibility(View.VISIBLE);

      if (((TimelineActivity) getActivity()).isNetworkAvailable()) {
        new RemoteDataTask(0).execute();
      } else {
        // ((TimelineActivity) getActivity()).noConnectionDialog.show();
        refresh.setRefreshing(false);
        loading_spinner.setVisibility(View.GONE);
      }
    } else {
      updateAdapter();
    }
  }

  private class RemoteDataTask extends AsyncTask<Void, Void, Void> {

    int flag;

    public RemoteDataTask(int flag) {
      this.flag = flag;
    }

    @Override
    protected Void doInBackground(Void... params) {
      // TODO Auto-generated method stub
      try {
        Log.v(TAG, "Getting post list for current user = "
            + ParseUser.getCurrentUser().getUsername());
        mResult = ActivityDao.getPostList(User.getCurrentUser());
      } catch (ParseException e) {
        Log.v(TAG, "Failed to get post list for current user");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {

      // Locate the listview in listview_timeline.xml

      // Pass the result to ListViewAdapter.java
      /*
       * mAdapter = new PostListAdapter(getActivity(), mResult); Log.d("Adapter", "Sukses"); //
       * Binds the adapter to ListView listview.setAdapter(mAdapter);
       */
      if (isAdded()) {
        if (mResult == null) {
          Toast.makeText(getActivity().getApplicationContext(), "No review", Toast.LENGTH_SHORT)
              .show();
        } else
          updateAdapter();
      }
      if (flag == 0) {
        loading_spinner.setVisibility(View.GONE);
        refresh.setRefreshing(false);
      } else if (flag == 1) {
        refresh.setRefreshing(false);
      }

    }

  }

  public void updateAdapter() {
    m1Result.clear();
    m1Result.addAll(mResult);
    mAdapter.notifyDataSetChanged();
    listview.invalidate();
  }

  public void scrollToUp() {
    if (listview != null)
      listview.smoothScrollToPosition(0);
  }

  @Override
  public void onDestroy() {
    uiHelper.onDestroy();
    super.onDestroy();
  }

  @Override
  public void onResume() {
    super.onResume();
    Session session = Session.getActiveSession();
    if (session != null && (session.isOpened() || session.isClosed())) {
      onSessionStateChange(session, session.getState(), null);
    }

    uiHelper.onResume();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    uiHelper.onSaveInstanceState(outState);
  }

  @Override
  public void onPause() {
    super.onPause();
    uiHelper.onPause();
  }

  public void postContentToWall(Activity review) {
    if (review == null) {
      Toast.makeText(getActivity().getApplicationContext(), "No restaurant data received ...",
          Toast.LENGTH_SHORT).show();
    } else if (review != null) {
      Toast.makeText(getActivity().getApplicationContext(), "Content received ...",
          Toast.LENGTH_SHORT).show();
    }
    if (!checkNetwork()) {
      Toast.makeText(getActivity().getApplicationContext(), "No active internet connection ...",
          Toast.LENGTH_SHORT).show();
      return;
    }
    if (!checkFbInstalled()) {
      Toast.makeText(getActivity().getApplicationContext(), "Facebook app not installed!..",
          Toast.LENGTH_SHORT).show();
      return;
    }

    Toast.makeText(getActivity().getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
    Log.v(TAG, "Review =" + review.getRestName() + " & " + review.getContent() + " & "
        + review.getCreatedBy().getUsername());
    if (FacebookDialog.canPresentShareDialog(getActivity(),
        FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
      FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(getActivity())
          .setName(review.getContent() + " at " + review.getResoId().getName())
          .setPlace(review.getResoId().getCity()).setCaption(review.getCreatedBy().getUsername())
          .setLink("https://play.google.com/store/apps/details?id=com.framework.grubber")
          .setDescription(review.getContent()).setPicture("http://3.t.imgbox.com/BBGnlGHR.jpg")
          .build();
      uiHelper.trackPendingDialogCall(shareDialog.present());
    } else {
      // System.out.println("Fail Success!");
    }

  }

  private boolean checkNetwork() {
    boolean wifiAvailable = false;
    boolean mobileAvailable = false;

    ConnectivityManager conManager = (ConnectivityManager) getActivity().getSystemService(
        Context.CONNECTIVITY_SERVICE);
    NetworkInfo[] networkInfo = conManager.getAllNetworkInfo();
    for (NetworkInfo netInfo : networkInfo) {
      if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
        if (netInfo.isConnected())
          wifiAvailable = true;
      if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
        if (netInfo.isConnected())
          mobileAvailable = true;
    }
    return wifiAvailable || mobileAvailable;
  }

  public Boolean checkFbInstalled() {
    PackageManager pm = getActivity().getPackageManager();
    boolean flag = false;
    try {
      pm.getPackageInfo("com.facebook.katana", PackageManager.GET_ACTIVITIES);
      flag = true;
    } catch (PackageManager.NameNotFoundException e) {
      flag = false;
    }
    return flag;
  }

  public void loginToFacebook() {
    mPrefs = getActivity().getPreferences(getActivity().MODE_PRIVATE);
    String access_token = mPrefs.getString("access_token", null);
    long expires = mPrefs.getLong("access_expires", 0);

    if (access_token != null) {
      facebook.setAccessToken(access_token);
    }

    if (expires != 0) {
      facebook.setAccessExpires(expires);
    }

    if (!facebook.isSessionValid()) {
      facebook.authorize(getActivity(), new String[] { "email", "publish_stream" },
          new DialogListener() {

            @Override
            public void onCancel() {
              // Function to handle cancel event
            }

            @Override
            public void onComplete(Bundle values) {

              Session.openActiveSession(getActivity(), true, new Session.StatusCallback() {

                @Override
                public void call(Session session, SessionState state, Exception exception) {
                  if (session.isOpened()) {

                  }
                }
              });
              // Function to handle complete event
              // Edit Preferences and update facebook acess_token
              SharedPreferences.Editor editor = mPrefs.edit();
              editor.putString("access_token", facebook.getAccessToken());
              editor.putLong("access_expires", facebook.getAccessExpires());
              editor.commit();
            }

            @Override
            public void onError(DialogError error) {
              // Function to handle error

            }

            @Override
            public void onFacebookError(FacebookError fberror) {
              // Function to handle Facebook errors

            }

          });
    }
  }

}
