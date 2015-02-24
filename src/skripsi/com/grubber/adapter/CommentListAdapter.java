package skripsi.com.grubber.adapter;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;

import com.parse.ParseFile;

import skripsi.com.grubber.R;
import skripsi.com.grubber.image.ImageLoader;
import skripsi.com.grubber.model.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentListAdapter extends BaseAdapter {
	
	private static final String TAG = PostListAdapter.class.getSimpleName();
	
	Context context;
	LayoutInflater inflater;
	TextView tvUser;
	TextView tvContent;
	TextView tvDate;
	ImageView profilePic;
	ImageLoader imageLoader;
	  

	  
	private List<Activity> cPost = null;
	
	public CommentListAdapter(Context context, List<Activity> postList) {
		this.context = context;
	    this.cPost = postList;
	    imageLoader = new ImageLoader(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return cPost.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return cPost.get(position);
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
	      view = inflater.inflate(R.layout.comment_list, null);
		}
		
		tvUser = (TextView) view.findViewById(R.id.username);
	    tvContent = (TextView) view.findViewById(R.id.comment);
	    tvDate = (TextView) view.findViewById(R.id.date);
	    profilePic = (ImageView) view.findViewById(R.id.profilePic);
	    Log.d("Adapter", "Berhasil set holder");
	    final Format formatter = new SimpleDateFormat("dd MMMM, yyyy");
	    
	    Log.v(TAG, "post = " + cPost.get(position).getCreatedBy().getUsername());
	    
	    ParseFile pp = cPost.get(position).getCreatedBy().getParseFile("profilePic");
	    
	    final String imageUrl = pp.getUrl();
	    
	    
	    // Set the results into TextView
	    tvUser.setText(cPost.get(position).getCreatedBy().getUsername());
	    tvContent.setText(cPost.get(position).getContent());
	    tvDate.setText(formatter.format(cPost.get(position).getCreatedAt()));
	    imageLoader.DisplayImage(imageUrl, profilePic);
	    
		return view;
	}
	
}
