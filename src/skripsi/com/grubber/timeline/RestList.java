package skripsi.com.grubber.timeline;

import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import skripsi.com.grubber.BaseActivity;
import skripsi.com.grubber.R;
import skripsi.com.grubber.adapter.CommentListAdapter;
import skripsi.com.grubber.dao.ActivityDao;
import skripsi.com.grubber.dao.RestaurantDao;
import skripsi.com.grubber.image.ImageLoader;
import skripsi.com.grubber.model.Restaurant;
import skripsi.com.grubber.model.User;
import skripsi.com.grubber.restaurant.PostReviewFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

public class RestList extends BaseActivity {

  /* Declare variables */
  protected static final String TAG = RestList.class.getSimpleName();

  /* Comment View */
  String restId;
  String userId;
  String username;
  String restaurant;
  String review;
  double rating;
  double price;
  String date;
  String profilePic;
  String commentStatus;

  TextView tvRestAddress;
  TextView tvDesc;

  TextView tvRate;
  TextView tvCash;
  TextView tvCity;
  RatingBar rbRating;
  RatingBar rbCash;
  ImageView profileImage;
  ImageLoader imageLoader;

  /* Add Comment */
  LinearLayout LLCommentField;

  ProgressDialog progressDialog;
  List<skripsi.com.grubber.model.Activity> mReviewResult;
  List<skripsi.com.grubber.model.Activity> reviewResult;
  List<skripsi.com.grubber.model.Activity> commentCache;
  ListView reviewListView;
  Restaurant activity;
  ParseUser puTargetUser;
  User uTargetUser = null;
  CommentListAdapter rAdapter;
  final String CACHE_COMMENT = "reviewId";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_restaurant);

    reviewListView = (ListView) findViewById(R.id.list_comment);

    Intent intent = getIntent();
    imageLoader = new ImageLoader(getBaseContext());
    restId = intent.getStringExtra("restId");

    LayoutInflater inflater = LayoutInflater.from(this);
    View view = inflater.inflate(R.layout.single_post_header_rest, null);

    // tvUser = (TextView) findViewById(R.id.username);
    tvRestAddress = (TextView) view.findViewById(R.id.restaurant);
    tvDesc = (TextView) view.findViewById(R.id.review);
    rbRating = (RatingBar) view.findViewById(R.id.ratingBar);
    tvRate = (TextView) view.findViewById(R.id.ratingValue);
    rbCash = (RatingBar) view.findViewById(R.id.priceBar);
    tvCash = (TextView) view.findViewById(R.id.priceValue);
    tvCity = (TextView) view.findViewById(R.id.date);
    profileImage = (ImageView) view.findViewById(R.id.profilePic);

    try {
      activity = RestaurantDao.getRestProfileById(restId);
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    ParseFile pp = (ParseFile) activity.getParseFile("photoRest");
    final String imageUrl = pp.getUrl();
    Format formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");
    NumberFormat nm = NumberFormat.getNumberInstance();

    setTitle(activity.getName());
    show();

    // tvUser.setText(activity.getCreatedBy().getUsername().toString());
    tvRestAddress.setText(activity.getAddress());
    tvDesc.setText(activity.getDesc().toString());
    tvRate.setText(Html.fromHtml("<i>" + nm.format(activity.getStar()) + "</i>"));
    rbRating.setRating((float) activity.getStar());
    tvCash.setText(Html.fromHtml("<i>" + nm.format(activity.getCash()) + "</i>"));
    rbCash.setRating((float) activity.getCash());
    tvCity.setText(activity.getCity());
    imageLoader.DisplayImage(imageUrl, profileImage);

    reviewListView.addHeaderView(view, null, false);
    mReviewResult = new ArrayList<skripsi.com.grubber.model.Activity>();
    rAdapter = new CommentListAdapter(RestList.this, mReviewResult);
    reviewListView.setAdapter(rAdapter);

    // new SinglePost().execute();
    new ReviewDataTask("open").execute();

  }

  public String numberToWords(double num) {
    String word;

    if (num == 1) {
      word = "one";
    } else if (num == 2) {
      word = "two";
    } else if (num == 3) {
      word = "three";
    } else if (num == 4) {
      word = "four";
    } else
      word = "five";

    return word;
  }

  private class ReviewDataTask extends AsyncTask<Void, Void, Void> {

    String task_state;

    public ReviewDataTask(String task_state) {
      this.task_state = task_state;
    }

    @Override
    protected Void doInBackground(Void... params) {
      // TODO Auto-generated method stub
      Log.v(TAG, "Getting restaurant review");

      try {
        Log.v(TAG, "Getting review list for current rest = success");
        reviewResult = ActivityDao.getRestReviews(activity);

      } catch (Exception e) {
        // TODO: handle exception
        Log.v(TAG, "Failed to get review list for current rest");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      Log.d("Adapter", "Sukses");
      if (reviewResult != null) {
        mReviewResult.clear();
        mReviewResult.addAll(reviewResult);
        rAdapter.notifyDataSetChanged();
        reviewListView.invalidate();
      } else {
        Log.v(TAG, "reviewResult = null!");
      }

    }

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu items for use in the action bar
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.restprof, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle presses on the action bar items
    switch (item.getItemId()) {
    case R.id.action_write:
      // Refresh coordinates
      Intent intent = new Intent(getBaseContext(), PostReviewFragment.class);
      intent.putExtra("restId", restId);
      startActivity(intent);
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    // TODO Auto-generated method stub
    super.onBackPressed();
    Intent intent = new Intent(this, TimelineActivity.class);
    startActivity(intent);
    this.finish();
  }
}
