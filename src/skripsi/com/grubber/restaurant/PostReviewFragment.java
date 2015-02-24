package skripsi.com.grubber.restaurant;

import skripsi.com.grubber.BaseActivity;
import skripsi.com.grubber.R;
import skripsi.com.grubber.adapter.RestaurantProfileAdapter;
import skripsi.com.grubber.dao.ActivityDao;
import skripsi.com.grubber.dao.RestaurantDao;
import skripsi.com.grubber.model.Restaurant;
import skripsi.com.grubber.model.User;
import skripsi.com.grubber.timeline.Comment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.parse.ParseException;

public class PostReviewFragment extends BaseActivity implements ConnectionCallbacks,
    OnConnectionFailedListener {
  private final static String TAG = PostReviewFragment.class.getSimpleName();

  private GoogleApiClient mGoogleApiClient;
  private ImageButton mPhoto = null;
  private byte[] photoBitmap = null;
  private Bitmap photoThumbnail = null;

  // to be filled by user
  private EditText mRestRev = null;
  /*
   * private RatingBar mCashBar = null; private RatingBar mRateBar = null;
   */
  private SeekBar mCashBar = null;
  private SeekBar mRateBar = null;
  private TextView tvRating = null;
  private TextView tvCash = null;
  private Button mPostBtn = null;
  private Button mCancelBtn = null;

  // temp to be filled by user
  private String content;
  private float cash = 1;
  private float rate = 1;

  private RestaurantProfileAdapter mAdapter;
  private String mRest;
  private Restaurant mDataRest;
  private String mStringRestName;
  private String mStringRestId;

  private PostReviewTask mPostReviewTask;
  Comment mComment;

  private String mRestNameString;
  private int tempCountReviews = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setTitle("Post Review");
    show();
    setContentView(R.layout.fragment_postreview);
    if (savedInstanceState == null) {
      Intent intent = getIntent();
      mStringRestId = intent.getStringExtra("restId");
      if (mStringRestId == null) {
        Log.v(TAG, "Bundle is null!");
      } else {
        try {
          mDataRest = RestaurantDao.getRestProfileById(mStringRestId);
          new CountReviewRestaurant().execute();
        } catch (ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    tvCash = (TextView) findViewById(R.id.price);
    tvRating = (TextView) findViewById(R.id.rating);

    mRestRev = (EditText) findViewById(R.id.etReviewBox);
    mCashBar = (SeekBar) findViewById(R.id.cashBar);
    mCashBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub
        cash = arg1;

        tvCash.setText("Cash : " + cashDefine(arg1));
      }
    });

    mRateBar = (SeekBar) findViewById(R.id.ratingBar);
    mRateBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub
        rate = arg1;

        tvRating.setText("Rate : " + rateDefine(arg1));
      }
    });

    mPostBtn = (Button) findViewById(R.id.btnPost);
    mPostBtn.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        Log.v(TAG, "Post review to server initialized!");
        getRestViewContent();
        mPostReviewTask = new PostReviewTask();
        mPostReviewTask.execute();
      }
    });
    mCancelBtn = (Button) findViewById(R.id.btnCancel);
    mCancelBtn.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        Intent intent = new Intent(getBaseContext(), RestaurantActivity.class);
        intent.putExtra("restId", mStringRestId);
        startActivity(intent);
      }
    });

  }

  public void getRestView() {

    LayoutInflater inflater = LayoutInflater.from(this);
    View getView = inflater.inflate(R.layout.fragment_postreview, null);

  }

  public void getRestViewContent() {
    content = mRestRev.getText().toString();/*
                                             * rate = mRateBar.getRating(); cash =
                                             * mCashBar.getRating();
                                             */
    // mCountReviews.setText(tempCountReviews);
  }

  public String cashDefine(int num) {

    String word;
    if (num == 0) {
      word = "not define";
    } else if (num == 1) {
      word = "very expensive";
    } else if (num == 2) {
      word = "expensive";
    } else if (num == 3) {
      word = "fair";
    } else if (num == 4) {
      word = "cheap";
    } else
      word = "very cheap";

    return word;
  }

  public String rateDefine(double num) {
    String word;

    if (num == 0) {
      word = "zero";
    } else if (num == 1) {
      word = "very bad";
    } else if (num == 2) {
      word = "bad";
    } else if (num == 3) {
      word = "fair";
    } else if (num == 4) {
      word = "good";
    } else
      word = "very good";

    return word;
  }

  class PostReviewTask extends AsyncTask<Void, Object, Restaurant> {

    public PostReviewTask() {
    }

    @Override
    protected Restaurant doInBackground(Void... params) {
      // first step to make sure it is sending
      boolean send = false;
      try {
        if (content != null && mDataRest != null && cash >= 0 && rate >= 0
            && mDataRest.getName() != null && mDataRest.getObjectId() != null) {
          ActivityDao.createNewPost(content, User.getCurrentUser(), mDataRest, cash, rate);
          RestaurantDao.updateRestaurantRating(mDataRest, cash, rate, tempCountReviews);
          send = true;
          Log.v(TAG, "Send = " + send);
        } else {
          send = false;
          Log.v(TAG, "Content = " + content);
          Log.v(TAG, "Rest = " + mDataRest.getName());
          Log.v(TAG, "Cash = " + cash);
          Log.v(TAG, "Rate = " + rate);
          Log.v(TAG, "Name = " + mDataRest.getName());
          Log.v(TAG, "RestId = " + mDataRest.getObjectId());

        }
      } catch (Exception e) {
        Log.w(TAG, "Problem loading profile", e);
        e.printStackTrace();
      }
      return mDataRest;
    }

    @Override
    protected void onPostExecute(Restaurant result) {
      super.onPostExecute(result);
      Log.v(TAG, "Finished task!");
      Toast.makeText(getBaseContext(), "Review has been posted!", Toast.LENGTH_LONG).show();
      Intent intent = new Intent(getBaseContext(), RestaurantActivity.class);
      intent.putExtra("restId", mStringRestId);
      startActivity(intent);
    }
  }

  class CountReviewRestaurant extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
      // TODO Auto-generated method stub
      try {
        tempCountReviews = ActivityDao.getReviewCount(mDataRest);
        Log.v(TAG, "hasil count " + tempCountReviews);
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      // TODO Auto-generated method stub
      super.onPostExecute(result);
    }

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
  public void onConnectionSuspended(int arg0) {
    // TODO Auto-generated method stub

  }

}