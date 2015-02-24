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
import skripsi.com.grubber.image.ImageLoader;
import skripsi.com.grubber.model.User;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ParseUser;

public class Comment extends BaseActivity {

  /* Declare variables */
  protected static final String TAG = Comment.class.getSimpleName();

  /* Comment View */
  String reviewId;
  String userId;
  String username;
  String restaurant;
  String review;
  double rating;
  double price;
  String date;
  String profilePic;
  String commentStatus;

  TextView tvUser;
  TextView tvRestName;
  TextView tvContent;
  TextView tvRate;
  TextView tvCash;
  TextView tvDate;
  RatingBar rbRating;
  RatingBar rbCash;
  ImageView profileImage;
  ImageLoader imageLoader;

  /* Add Comment */
  EditText editComment;
  Button sendBtn;

  ProgressDialog progressDialog;
  List<skripsi.com.grubber.model.Activity> mCommentResult;
  List<skripsi.com.grubber.model.Activity> commentResult;
  List<skripsi.com.grubber.model.Activity> commentCache;
  ListView commentlistview;
  skripsi.com.grubber.model.Activity activity;
  ParseUser puTargetUser;
  User uTargetUser = null;
  CommentListAdapter cAdapter;
  final String CACHE_COMMENT = "reviewId";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_comment);

    commentlistview = (ListView) findViewById(R.id.list_comment);

    Intent intent = getIntent();
    imageLoader = new ImageLoader(getBaseContext());
    reviewId = intent.getStringExtra("reviewId");
    /*
     * userId = intent.getStringExtra("userId"); username = intent.getStringExtra("username");
     * restaurant = intent.getStringExtra("restaurant"); review = intent.getStringExtra("review");
     * rating = intent.getDoubleExtra("rating", 0); price = intent.getDoubleExtra("price", 0); date
     * = intent.getStringExtra("date"); profilePic = intent.getStringExtra("profilePic");
     */
    commentStatus = intent.getStringExtra("commentStatus");

    LayoutInflater inflater = LayoutInflater.from(this);
    View view = inflater.inflate(R.layout.single_post_header, null);

    // tvUser = (TextView) findViewById(R.id.username);
    tvRestName = (TextView) view.findViewById(R.id.restaurant);
    tvContent = (TextView) view.findViewById(R.id.review);
    rbRating = (RatingBar) view.findViewById(R.id.ratingBar);
    tvRate = (TextView) view.findViewById(R.id.ratingValue);
    rbCash = (RatingBar) view.findViewById(R.id.priceBar);
    tvCash = (TextView) view.findViewById(R.id.priceValue);
    tvDate = (TextView) view.findViewById(R.id.date);
    profileImage = (ImageView) view.findViewById(R.id.profilePic);

    activity = ActivityDao.getSinglePost(reviewId);
    puTargetUser = activity.getCreatedBy();
    uTargetUser = new User();
    uTargetUser.parseUser = puTargetUser;

    ParseFile pp = (ParseFile) activity.getCreatedBy().getParseFile("profilePic");
    final String imageUrl = pp.getUrl();
    Format formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm");
    NumberFormat nm = NumberFormat.getNumberInstance();

    setTitle(activity.getCreatedBy().get("fullName").toString());
    show();

    // tvUser.setText(activity.getCreatedBy().getUsername().toString());
    tvRestName.setText(activity.getResoId().getName().toString());
    tvContent.setText(activity.getContent().toString());
    tvRate.setText(Html.fromHtml("<i>" + numberToWords(activity.getRate()) + "</i>"));
    rbRating.setRating((float) activity.getRate());
    tvCash.setText(Html.fromHtml("<i>" + numberToWords(activity.getCash()) + "</i>"));
    rbCash.setRating((float) activity.getCash());
    tvDate.setText(formatter.format(activity.getCreatedAt()));
    imageLoader.DisplayImage(imageUrl, profileImage);

    commentlistview.addHeaderView(view, null, false);
    mCommentResult = new ArrayList<skripsi.com.grubber.model.Activity>();
    cAdapter = new CommentListAdapter(Comment.this, mCommentResult);
    commentlistview.setAdapter(cAdapter);

    addNewComment();
    // new SinglePost().execute();
    new CommentDataTask("open").execute();

  }

  public String numberToWords(double num) {
    String word;

    if (num == 0) {
      word = "zero";
    } else if (num == 1) {
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

  public void addNewComment() {

    editComment = (EditText) findViewById(R.id.comment_text);
    sendBtn = (Button) findViewById(R.id.buttonSend);

    sendBtn.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        // TODO Auto-generated method stub
        String content = editComment.getText().toString();

        if (!content.isEmpty()) {
          ActivityDao.createNewComment(content, User.getCurrentUser(), commentStatus, reviewId,
              uTargetUser);
          Log.d("Send Comment = ", "Succeed");

          new CommentDataTask("add").execute();

          /*
           * if(task.getStatus() == AsyncTask.Status.FINISHED) {
           * commentlistview.setSelection(commentlistview.getAdapter().getCount()-1); }
           */
        } else {
          Log.d("Send Comment = ", "Failed");
          Toast msg = Toast.makeText(getBaseContext(), "Comment is empty", Toast.LENGTH_LONG);
          msg.show();
        }
      }
    });

  }

  public void updateStatus() {
    if (commentStatus.equals("Read")) {
      ActivityDao.updateCommentStatus(reviewId, "C", null, null);
      Log.d("Update Status = ", "Sukses Update Status");
    }
  }

  private class CommentDataTask extends AsyncTask<Void, Void, Void> {

    String task_state;

    public CommentDataTask(String task_state) {
      this.task_state = task_state;
    }

    @Override
    protected Void doInBackground(Void... params) {
      // TODO Auto-generated method stub
      try {
        Log.v("Comment", "Getting comment list for current review = success");
        commentResult = ActivityDao.getCommentList(reviewId);
        /*
         * ParseObject.unpinAllInBackground(CACHE_COMMENT, commentResult, new DeleteCallback() {
         * 
         * @Override public void done(ParseException e) { // TODO Auto-generated method stub
         * ParseObject.pinAllInBackground(CACHE_COMMENT, commentResult); Log.d(TAG,
         * "cache succeed"); } });
         */
      } catch (Exception e) {
        // TODO: handle exception
        Log.v("Comment", "Failed to get post list for current user");
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      // Locate the listview in listview_timeline.xml

      // Pass the result to ListViewAdapter.java
      // cAdapter = new CommentListAdapter(Comment.this, commentResult);
      Log.d("Adapter", "Sukses");
      // Binds the adapter to ListView
      // commentlistview.setAdapter(cAdapter);

      mCommentResult.clear();
      mCommentResult.addAll(commentResult);
      cAdapter.notifyDataSetChanged();
      commentlistview.invalidate();

      if (task_state.equals("open")) {
        updateStatus();
      } else {
        commentlistview.setSelection(commentlistview.getAdapter().getCount() - 1);
        editComment.setText("");
      }

      // updateStatus();
    }

  }

}