package skripsi.com.grubber.notifications;

import java.util.List;

import skripsi.com.grubber.BaseActivity;
import skripsi.com.grubber.R;
import skripsi.com.grubber.adapter.NotificationListAdapter;
import skripsi.com.grubber.dao.ActivityDao;
import skripsi.com.grubber.model.Activity;
import skripsi.com.grubber.model.User;
import skripsi.com.grubber.profile.ProfileFragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseUser;

public class Notifications extends Fragment {

  protected static final String TAG = Notifications.class.getSimpleName();

  ListView listview;
  List<Activity> mResult;
  NotificationListAdapter mAdapter;

  SwipeRefreshLayout refresh;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    View view = inflater.inflate(R.layout.fragment_notifications, container, false);

    refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
    refresh.setOnRefreshListener(new OnRefreshListener() {

      @Override
      public void onRefresh() {
        // TODO Auto-generated method stub
        new RemoteDataTask().execute();
      }
    });

    refresh.setColorScheme(R.color.progressbar_1, R.color.progressbar_2, R.color.progressbar_3,
        R.color.progressbar_4);
    // refresh.setColorSchemeResources(R.color.progressbar_1,
    // R.color.progressbar_2,
    // R.color.progressbar_3,
    // R.color.progressbar_4);

    refresh.setRefreshing(true);
    new NotificationTask(getActivity()).execute();
    new RemoteDataTask().execute();
    return view;
  }

  private class RemoteDataTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
      // TODO Auto-generated method stub
      try {
        mResult = ActivityDao.getNotifications(User.getCurrentUser(), "no");

      } catch (ParseException e) {
        Log.v(TAG, "Failed to get post list for current user");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {

      if (isAdded()) {
        // Locate the listview in listview_timeline.xml
        listview = (ListView) getView().findViewById(R.id.list_notif);
        listview.setOnItemClickListener(new OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            Intent intent = null;
            if (mResult.get(position).getType().equals("C")) {
              Log.v(TAG, "Loading Comment from Notif");
              intent = new Intent(getActivity(), skripsi.com.grubber.timeline.Comment.class);
              intent.putExtra("reviewId", mResult.get(position).getReviewId());
              if (mResult.get(position).getTargetUserProfile().getObjectId()
                  .equals(User.getCurrentUser().getObjectId())) {
                intent.putExtra("commentStatus", "Read");
                Log.v("commentStatus", "Read");
              } else {
                intent.putExtra("commentStatus", "Unread");
                Log.v("commentStatus", "Unread");
              }
              getActivity().startActivity(intent);
            } else {
              Log.v(TAG, "Loading Profile from Notif");
              Bundle bundle = new Bundle();
              bundle.putString("commentStatus", "Read");
              Log.d("commentStatus", "Read");
              ParseUser from = mResult.get(position).getCreatedBy();
              ParseUser to = mResult.get(position).getTargetParseUserProfile();
              bundle.putString("objectId", mResult.get(position).getCreatedBy().getObjectId());
              bundle.putString("userName", mResult.get(position).getCreatedBy().getUsername());
              ActivityDao.updateCommentStatus(null, "S", to, from);
              Fragment fragment = new ProfileFragment();
              fragment.setArguments(bundle);
              FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
              FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
              fragmentTransaction.replace(android.R.id.tabcontent, fragment);
              fragmentTransaction.addToBackStack(null);
              fragmentTransaction.commit();
            }
          }
        });
        // Pass the result to ListViewAdapter.java
        mAdapter = new NotificationListAdapter(getActivity(), mResult);
        Log.d("Adapter", "Sukses");
        // Binds the adapter to ListView
        listview.setAdapter(mAdapter);
        // Close the progress dialog
        refresh.setRefreshing(false);
      }
    }
  }
  
  @Override
	public void onResume() {
		// TODO Auto-generated method stub
	  ((BaseActivity) getActivity()).setTitle("Notifications");
		super.onResume();
	}

}
