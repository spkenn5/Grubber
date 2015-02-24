package skripsi.com.grubber.trending;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import skripsi.com.grubber.R;
import skripsi.com.grubber.adapter.TrendingAdapter;
import skripsi.com.grubber.dao.ActivityDao;
import skripsi.com.grubber.dao.RestaurantDao;
import skripsi.com.grubber.image.ImageLoader;
import skripsi.com.grubber.model.Restaurant;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;

public class TrendingFragment extends Fragment {
  public static final String TAG = TrendingFragment.class.getSimpleName();

  public Button tvRate, tvCash, tvPop;
  public TextView tvName, tvAddress, tvCity, tvDesc;
  public ListView lvList;
  private List<Restaurant> mRandRest = new ArrayList<Restaurant>();
  private List<Restaurant> mRest = null;
  private TrendingAdapter mTrendAdapter;
  private String buttonPressed = "pop";
  private TrendingTask trendTask;
  private LoadRandomRest randTask;

  ImageView profilePic;
  ImageLoader imageLoader = new ImageLoader(getActivity());

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);

    randTask = new LoadRandomRest();
    randTask.execute();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    View view = inflater.inflate(R.layout.fragment_trending, container, false);

    tvName = (TextView) view.findViewById(R.id.tvName);
    tvAddress = (TextView) view.findViewById(R.id.tvAddress);
    tvCity = (TextView) view.findViewById(R.id.tvCity);
    tvDesc = (TextView) view.findViewById(R.id.tvDesc);
    profilePic = (ImageView) view.findViewById(R.id.ibProfilePhoto);

    lvList = (ListView) view.findViewById(R.id.lvTrending);
    tvRate = (Button) view.findViewById(R.id.tvRate);
    tvRate.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        buttonPressed = "rate";
        mRest.clear();
        tvPop.setBackgroundResource(R.color.main_tab_lightblack);
        tvCash.setBackgroundResource(R.color.main_tab_lightblack);
        tvRate.setBackgroundResource(R.drawable.button_tab);
        trendTask = new TrendingTask();
        trendTask.execute();
      }
    });
    tvCash = (Button) view.findViewById(R.id.tvCash);
    tvCash.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        buttonPressed = "cash";
        mRest.clear();
        tvPop.setBackgroundResource(R.color.main_tab_lightblack);
        tvRate.setBackgroundResource(R.color.main_tab_lightblack);
        tvCash.setBackgroundResource(R.drawable.button_tab);
        trendTask = new TrendingTask();
        trendTask.execute();
      }
    });
    tvPop = (Button) view.findViewById(R.id.tvPop);
    tvPop.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        buttonPressed = "pop";
        mRest.clear();
        tvCash.setBackgroundResource(R.color.main_tab_lightblack);
        tvRate.setBackgroundResource(R.color.main_tab_lightblack);
        tvPop.setBackgroundResource(R.drawable.button_tab);
        trendTask = new TrendingTask();
        trendTask.execute();
      }
    });

    return view;
  }

  class TrendingTask extends AsyncTask<Void, Void, List<Restaurant>> {
    static final String SKIP_COUNT = "skipCount";
    private int skipCount;

    public TrendingTask(int skipCount) {
      this.skipCount = skipCount;
    }

    public TrendingTask() {
      this(0);
    }

    @Override
    protected void onPreExecute() {
      Log.v(TAG, "+++ SearchPeople.onPreExecute() called! +++");
      super.onPreExecute();

    }

    @Override
    protected List<Restaurant> doInBackground(Void... params) {
      Log.v(TAG, "+++ SearchTrendRate.doInBackground() called! +++");

      List<Restaurant> result = null;
      try {
        if (buttonPressed.equals("rate")) {
          Log.v(TAG, "Getting Trending by Rate");
          result = ActivityDao.getTrendingByRate();
        } else if (buttonPressed.equals("cash")) {
          Log.v(TAG, "Getting Trending by Cash");
          result = ActivityDao.getTrendingByCash();
        } else if (buttonPressed.equals("pop")) {
          Log.v(TAG, "Getting Trending by Popularity");
          result = ActivityDao.getTrendingByPop();
        } else {
          Toast.makeText(getActivity().getBaseContext(), "Invalid Task", Toast.LENGTH_LONG).show();
        }
      } catch (ParseException e) {
        getView().post(new Runnable() {
          @Override
          public void run() {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setMessage("Something is wrong while trying to query data");
            alert.show();
          }
        });
      }
      return result;
    }

    @Override
    protected void onPostExecute(List<Restaurant> result) {
      Log.v(TAG, "+++ SearchTrendRate.onPostExecute() called! +++");
      if (skipCount == 0) {
        mRest = result;
      } else {
        if (result != null) {
          mRest.addAll(result);
        }
      }
      // initialize UPA
      mTrendAdapter = new TrendingAdapter(getActivity().getBaseContext(),
          R.layout.adapter_trending, mRest);
      lvList.setAdapter(mTrendAdapter);
    }
  }

  class LoadRandomRest extends AsyncTask<Void, Void, List<Restaurant>> {

    @Override
    protected List<Restaurant> doInBackground(Void... params) {
      // TODO Auto-generated method stub
      List<Restaurant> result = null;
      try {
        result = RestaurantDao.getRest();
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      mRandRest.addAll(result);
      return result;
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
      int position = randInt(0, mRandRest.size());
      ParseFile pp = (ParseFile) mRandRest.get(position).getParseFile("photoRest");
      final String imageUrl = pp.getUrl();
      Log.v(TAG, "1 or 2 " + imageUrl.toString() + profilePic.toString());

      imageLoader.DisplayImage(imageUrl, profilePic);

      tvName.setText(mRandRest.get(position).getName());
      tvAddress.setText(mRandRest.get(position).getAddress());
      tvCity.setText(mRandRest.get(position).getCity());
      tvDesc.setText(mRandRest.get(position).getDesc());

      trendTask = new TrendingTask();
      trendTask.execute();
    }

  }

  public static int randInt(int min, int max) {

    // NOTE: Usually this should be a field rather than a method
    // variable so that it is not re-seeded every call.
    Random rand = new Random();

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    int randomNum = rand.nextInt((max - min) + 1) + min;

    return randomNum;
  }
}
