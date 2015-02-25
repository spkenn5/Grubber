package skripsi.com.grubber.adapter;

import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import skripsi.com.grubber.R;
import skripsi.com.grubber.image.ImageLoader;
import skripsi.com.grubber.model.Activity;
import skripsi.com.grubber.model.User;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.parse.ParseFile;

public class PostListAdapter extends BaseAdapter {

  private static final String TAG = PostListAdapter.class.getSimpleName();
  // Declare variables
  Context context;
  LayoutInflater inflater;
  TextView tvUser;
  TextView tvRestName;
  TextView tvContent;
  TextView tvRate;
  TextView tvCash;
  RatingBar rbRate;
  RatingBar rbCash;
  TextView tvDate;
  ImageView profilePic;
  ImageLoader imageLoader;

  private List<Activity> mPost = null;
  private Bitmap ppBitmap;

  public PostListAdapter(Context context, List<Activity> postList) {
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
  public Activity getItem(int position) {
    // TODO Auto-generated method stub
    return mPost.get(position);
  }

  @Override
  public long getItemId(int position) {
    // TODO Auto-generated method stub
    return position;
  }

  public View getView(final int position, View view, ViewGroup parent) {
    // TODO Auto-generated method stub
    if (view == null) {
      LayoutInflater inflater = (LayoutInflater) this.context
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = inflater.inflate(R.layout.activity_post_list, null);
    }

    // Locate text view in listview_post

    tvUser = (TextView) view.findViewById(R.id.username);
    tvRestName = (TextView) view.findViewById(R.id.restaurant);
    tvContent = (TextView) view.findViewById(R.id.review);

    tvRate = (TextView) view.findViewById(R.id.rating);
    tvCash = (TextView) view.findViewById(R.id.price);
    rbRate = (RatingBar) view.findViewById(R.id.ratingBar);
    rbCash = (RatingBar) view.findViewById(R.id.cashBar);
    tvDate = (TextView) view.findViewById(R.id.date);
    profilePic = (ImageView) view.findViewById(R.id.profilePic);
    Log.d("Adapter", "Berhasil set holder");

    final Format formatter = new SimpleDateFormat("MMM dd, yyyy");
    NumberFormat nm = NumberFormat.getNumberInstance();

    Log.v(TAG, "post = " + mPost.get(position).getCreatedBy().getUsername());

    ParseFile pp = (ParseFile) mPost.get(position).getCreatedBy().getParseFile("profilePic");
    /*
     * byte[] data = null; try { data = pp.getData(); } catch (ParseException e) { // TODO
     * Auto-generated catch block e.printStackTrace(); } ppBitmap =
     * BitmapFactory.decodeByteArray(data, 0, data.length);
     */

    final String imageUrl = pp.getUrl();
    Log.d("current user", User.getCurrentUser().toString());

    // Set the results into TextView
    tvUser.setText("by " + mPost.get(position).getCreatedBy().getUsername());
    tvRestName.setText(mPost.get(position).getResoId().getName());
    tvContent.setText("\"" + mPost.get(position).getContent() + "\"");
    tvRate.setText("Rating ");
    rbRate.setRating((float) mPost.get(position).getRate());
    tvCash.setText("Price ");
    rbCash.setRating((float) mPost.get(position).getCash());
    tvDate.setText(formatter.format(mPost.get(position).getCreatedAt()));
    imageLoader.DisplayImage(imageUrl, profilePic);

    // profilePic.setImageBitmap(RoundImage.getRoundedShape(ppBitmap));

    return view;
  }

  public interface MyFacebookListener {
    // you can define any parameter as per your requirement
    public void postContentToWall(Activity review);
  }

}
