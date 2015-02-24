package skripsi.com.grubber.adapter;

import java.text.NumberFormat;

import skripsi.com.grubber.R;
import skripsi.com.grubber.model.Restaurant;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

public class RestaurantProfileAdapter extends BaseAdapter {
  private final static String TAG = RestaurantProfileAdapter.class.getSimpleName();

  private static final int PHOTO_REQUEST_CODE = 678;
  public static final String REST_ID = "restId";
  public static final String REST_SCREENNAME = "restName";

  private String mRestId = null;
  private String mRestNameString = null;
  private ImageButton mPhoto = null;
  private byte[] photoBitmap = null;
  private Bitmap photoThumbnail = null;
  private TextView mRestName = null;
  private TextView mRestCity = null;
  private TextView mRestDesc = null;
  private RatingBar mCashBar = null;
  private RatingBar mRateBar = null;

  private Restaurant rest;
  private Context context;
  private ProgressBar mProgressBar = null;

  private TextView mCountReviews;

  private int tempCountReviews;
  private boolean isProfileLoaded = false;
  private boolean isCountLoaded = false;

  public RestaurantProfileAdapter(Context context, Restaurant rest) {
    // TODO Auto-generated constructor stub
    this.context = context;
    this.rest = rest;
    // this.tempCountReviews = totalRev;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // TODO Auto-generated method stub
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) this.context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.fragment_restprofile, null);
    }
    Restaurant data = new Restaurant();
    NumberFormat nm = NumberFormat.getNumberInstance();

    // default mode for the profile page is invisible

    mProgressBar = (ProgressBar) convertView.findViewById(R.id.pbSpinner);
    mPhoto = (ImageButton) convertView.findViewById(R.id.ibProfilePhoto);
    mPhoto.setClickable(false);

    mRestName = (TextView) convertView.findViewById(R.id.tvRestName);
    mRestDesc = (TextView) convertView.findViewById(R.id.tvRestDesc);
    mRestCity = (TextView) convertView.findViewById(R.id.tvRestCity);
    mCountReviews = (TextView) convertView.findViewById(R.id.tvTotalReview);

    mRestName.setText(rest.getName());
    mRestCity.setText(rest.getCity());
    mRestDesc.setText(rest.getDesc());
    // mCountReviews.setText(tempCountReviews);
    return convertView;
  }

  @Override
  public Object getItem(int position) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getItemId(int position) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getCount() {
    // TODO Auto-generated method stub
    return 1;
  }

}
