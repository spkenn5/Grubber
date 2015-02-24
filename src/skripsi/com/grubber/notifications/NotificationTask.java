package skripsi.com.grubber.notifications;

import java.util.List;

import skripsi.com.grubber.dao.ActivityDao;
import skripsi.com.grubber.model.User;

import com.parse.ParseException;
import com.readystatesoftware.viewbadger.BadgeView;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class NotificationTask extends AsyncTask<Void, Void, Void>{

	List<skripsi.com.grubber.model.Activity> mResult;
	int count_notif;
	public BadgeView badge;
	Context context;
	private NotificationResult mNotificationResult;
	
	public NotificationTask(Activity activity) {
		mNotificationResult = (NotificationResult) activity;
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
    	if(mResult != null)
    		count_notif = mResult.size();
    	else count_notif = 0;
      Log.d("Jumlah notif",
          String.format("getNotifications found %s records", mResult == null ? 0 : count_notif));
      if (count_notif != 0) {
    	mNotificationResult.badgeConfiguration(count_notif);
      }
    }
    
    public interface NotificationResult {
    	public void badgeConfiguration(int totalNotif);
    }

}
