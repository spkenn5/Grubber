package skripsi.com.grubber.adapter;

import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import skripsi.com.grubber.R;
import skripsi.com.grubber.image.ImageLoader;
import skripsi.com.grubber.model.Restaurant;
import skripsi.com.grubber.restaurant.RestaurantActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.ParseFile;

public class TrendingAdapter extends BaseAdapter {

  private static final String TAG = TrendingAdapter.class.getSimpleName();
  // Declare variables
  Context context;
  LayoutInflater inflater;
  int layoutResourceId;
  TextView tvRestName;
  TextView tvCity;
  TextView tvAddress;
  TextView tvDesc;
  TextView tvRate, tvCash;
  RatingBar rbRate;
  RatingBar rbCash;
  ImageView profilePic;
  ImageLoader imageLoader;
  private List<Restaurant> mPost = null;

  private Bitmap ppBitmap;

  public TrendingAdapter(Context context, int layoutResourceId, List<Restaurant> postList) {

    this.layoutResourceId = layoutResourceId;
    this.context = context;
    this.mPost = postList;
    imageLoader = new ImageLoader(context);
  }

  @Override
  public int getCount() {
    // TODO Auto-generated method stub
    return mPost.size();
  }

  @Override
  public Restaurant getItem(int position) {
    // TODO Auto-generated method stub
    return mPost.get(position);
  }

  @Override
  public long getItemId(int position) {
    // TODO Auto-generated method stub
    return position;
  }

  @Override
  public View getView(final int position, View view, ViewGroup parent) {
    // TODO Auto-generated method stub
    if (view == null) {
      LayoutInflater inflater = (LayoutInflater) this.context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = inflater.inflate(R.layout.adapter_trending, null);
    }

    // Locate text view in listview_post
    tvRestName = (TextView) view.findViewById(R.id.restaurant);
    tvCity = (TextView) view.findViewById(R.id.city);
    tvAddress = (TextView) view.findViewById(R.id.address);
    tvDesc = (TextView) view.findViewById(R.id.desc);
    tvRate = (TextView) view.findViewById(R.id.rating);
    tvRate.setText("Rating ");
    rbRate = (RatingBar) view.findViewById(R.id.ratingBar);
    rbRate.setRating((float) mPost.get(position).getStar());
    tvCash = (TextView) view.findViewById(R.id.price);
    tvCash.setText("Price ");
    rbCash = (RatingBar) view.findViewById(R.id.cashBar);
    rbCash.setRating((float) mPost.get(position).getCash());
    profilePic = (ImageView) view.findViewById(R.id.profilePic);
    Log.d("Adapter", "Berhasil set holder");

    final Format formatter = new SimpleDateFormat("dd MMMM, yyyy");
    NumberFormat nm = NumberFormat.getNumberInstance();

    ParseFile pp = (ParseFile) mPost.get(position).getParseFile("photoRest");
    final String imageUrl = pp.getUrl();
    // Set the results into TextView

    tvRestName.setText(mPost.get(position).getName());
    tvCity.setText(mPost.get(position).getCity());
    tvAddress.setText(mPost.get(position).getAddress());
    tvDesc.setText(mPost.get(position).getDesc());
    rbRate.setRating(mPost.get(position).getStar());
    rbCash.setRating(mPost.get(position).getCash());
    imageLoader.DisplayImage(imageUrl, profilePic);

    // Listen for ListView Item Click
    view.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        // TODO Auto-generated method stub
        // Send single item click data to SingleItemView Class
        Intent intent = new Intent(context, RestaurantActivity.class);
        intent.putExtra("restId", mPost.get(position).getObjectId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.putExtra("profilePic",
        // ((ParseFile) mPost.get(position).getCreatedBy().getParseFile("profilePic")).getUrl());
        context.startActivity(intent);
      }
    });

    return view;
  }
}
