package skripsi.com.grubber;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import skripsi.com.android.Utility;
import skripsi.com.android.widget.Validate;
import skripsi.com.grubber.dao.UserDao;
import skripsi.com.grubber.image.ImageLoader;
import skripsi.com.grubber.model.User;
import skripsi.com.grubber.profile.ProfileFragment;
import skripsi.com.grubber.timeline.TimelineActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import eu.janmuller.android.simplecropimage.CropImage;

public class RegisterFragment extends Fragment {
  private static final int PHOTO_REQUEST_CODE = 678;
  private static final String TAG = RegisterFragment.class.getSimpleName();

  private OnFragmentInteractionListener mListener;
  private boolean isEdit = false;

  // private ImageButton mPhoto = null;
  private byte[] photoBitmap = null;
  private Bitmap photoThumbnail = null;
  private EditText mEmail = null;
  // private TextView tvPass = null;
  // private TextView tvPass2 = null;
  private EditText mPassword = null;
  private EditText mPassword2 = null;
  private EditText mUsername = null;
  private EditText mFullName = null;
  private EditText mAboutMe = null;
  private ImageView mPhoto = null;
  // private CheckBox mAgree = null;
  // private TextView mTnC = null;
  private Button btnSubmit = null;
  private Button btnCancel = null;
  // private ProgressBarDialogFragment mProgressDialog = null;
  private Bundle bundle;
  private boolean mIsSubmitting = false;

  private UpdateProfileTask mUpdateProfileTask;

  public static final int REQUEST_CODE_CROP_IMAGE = 0x3;
  private File mFileTemp;
  public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
  Bitmap bitmap;
  ParseFile pp;

  private ProgressBarDialogFragment mProgressDialog = null;

  protected boolean validateForm() {
    boolean result = false;

    result = validateEmail(true) && validatePassword() && validatePassword2() && validateUsername()
        && validateFullName() && validateAboutMe();
    if (result) {
      // check Photo
    }
    return result;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    setHasOptionsMenu(true);
    getActivity().getActionBar().hide();
    Utility.lockScreenOrientation(getActivity());
    if (ParseUser.getCurrentUser() != null) {
      getActivity().supportInvalidateOptionsMenu();
    }
    if (getArguments() != null) {
      isEdit = getArguments().getBoolean("editMode");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_register, container, false);

    mProgressDialog = new ProgressBarDialogFragment();
    mProgressDialog.setCancelable(false);
    TextView title = (TextView) view.findViewById(R.id.tvRegistrationPhoto);
    mPhoto = (ImageView) view.findViewById(R.id.gambar);
    mEmail = (EditText) view.findViewById(R.id.etRegistrationEmail);
    mPassword = (EditText) view.findViewById(R.id.etRegistrationPassword);
    mPassword2 = (EditText) view.findViewById(R.id.etRegistrationPassword2);
    mUsername = (EditText) view.findViewById(R.id.etRegistrationUserName);
    mFullName = (EditText) view.findViewById(R.id.etRegistrationFullName);
    mAboutMe = (EditText) view.findViewById(R.id.etRegistrationAboutMe);
    btnSubmit = (Button) view.findViewById(R.id.btnSubmit);
    btnCancel = (Button) view.findViewById(R.id.btnCancel);

    if (isEdit) {
      title.setText("Edit Profile");
    }

    mPhoto.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setPhoto();
      }
    });
    mPhoto.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        mPhoto.setImageResource(R.drawable.ic_launcher_new);
        if (photoBitmap != null) {
          photoBitmap = null;
        }
        if (photoThumbnail != null) {
          photoThumbnail.recycle();
          photoThumbnail = null;
        }
        return true;
      }
    });

    mEmail.setFilters(new InputFilter[] { new LengthFilter(100) });
    mEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          validateEmail(false);
        }
      }
    });
    mPassword.setFilters(new InputFilter[] { new LengthFilter(33) });
    mPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          validatePassword();
        }
      }
    });

    mPassword2.setFilters(new InputFilter[] { new LengthFilter(33) });
    mPassword2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          validatePassword2();
        }
      }
    });

    mUsername.setFilters(new InputFilter[] { new LengthFilter(33) });
    mUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          validateUsername();
        }
      }
    });

    mFullName.setFilters(new InputFilter[] { new LengthFilter(33) });
    mFullName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          validateFullName();
        }
      }
    });

    mAboutMe.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          validateAboutMe();
        }
      }
    });

    btnSubmit.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        // TODO Auto-generated method stub
        if (isEdit) {
          submitEdit();
        } else {
          submitRegistration();
        }
      }
    });

    btnCancel.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        Fragment f = null;
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
            R.anim.slide_in_left, R.anim.slide_out_right);
        if (!isEdit) {
          /*f = Fragment.instantiate(getActivity(), LoginFragment.class.getName());
          transaction.addToBackStack(null);
          transaction.replace(R.id.content_frame, f);*/
          ((MainActivity)getActivity()).onBackPressed();
        } else {
          /*f = Fragment.instantiate(getActivity(), ProfileFragment.class.getName());
          transaction.addToBackStack(null);
          transaction.replace(android.R.id.tabcontent, f);*/
        	((MainActivity)getActivity()).onBackPressed();
        }
        transaction.commit();
      }
    });

    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
    } else {
      mFileTemp = new File(getActivity().getFilesDir(), TEMP_PHOTO_FILE_NAME);
    }

    updateView();
    return view;
  }
  
  
  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    //inflater.inflate(R.menu.registration, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    //menu.findItem(R.id.miRegistrationOk).setEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    /*int itemId = item.getItemId();
    if (itemId == R.id.miRegistrationOk) {
      if (!isEdit) {
        submitRegistration();
      } else {
        submitEdit();
      }
    } else if (itemId == R.id.miRegistrationCancel) {

      FragmentManager fm = getFragmentManager();
      FragmentTransaction transaction = fm.beginTransaction();
      Fragment f = null;
      transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
          R.anim.slide_in_left, R.anim.slide_out_right);
      if (!isEdit) {
        f = Fragment.instantiate(getActivity(), LoginFragment.class.getName());
        transaction.addToBackStack(null);
        transaction.replace(R.id.content_frame, f);
      } else {
        f = Fragment.instantiate(getActivity(), ProfileFragment.class.getName());
        transaction.addToBackStack(null);
        transaction.replace(android.R.id.tabcontent, f);
      }
      transaction.commit();
    }*/
    return super.onOptionsItemSelected(item);
  }

  private void startCropImage() {

    Intent intent = new Intent(getActivity(), CropImage.class);
    intent.putExtra(CropImage.IMAGE_PATH, mFileTemp.getPath());
    intent.putExtra(CropImage.SCALE, true);

    intent.putExtra(CropImage.ASPECT_X, 1);
    intent.putExtra(CropImage.ASPECT_Y, 1);

    startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
  }

  public static void copyStream(InputStream input, OutputStream output) throws IOException {

    byte[] buffer = new byte[1024];
    int bytesRead;
    while ((bytesRead = input.read(buffer)) != -1) {
      output.write(buffer, 0, bytesRead);
    }
  }

  private Bitmap rescaleImage(Bitmap newBitmap) {
	if(newBitmap.getHeight() >= 300) {
		bitmap = Bitmap.createScaledBitmap(newBitmap, 300, 300, false);
	}
    return bitmap;
  }

  protected void setPhoto() {
    Intent photoIntent = new Intent(Intent.ACTION_PICK);
    photoIntent.setType("image/*");
    photoIntent.setAction(Intent.ACTION_GET_CONTENT);
    photoIntent.addCategory(Intent.CATEGORY_OPENABLE);
    startActivityForResult(photoIntent, PHOTO_REQUEST_CODE);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // TODO Auto-generated method stub
    if (resultCode != getActivity().RESULT_OK) {

      return;
    }

    switch (requestCode) {

    case PHOTO_REQUEST_CODE:

      try {

        InputStream inputStream = getActivity().getContentResolver()
            .openInputStream(data.getData());
        FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
        copyStream(inputStream, fileOutputStream);
        fileOutputStream.close();
        inputStream.close();

        startCropImage();

      } catch (Exception e) {

        Log.e(TAG, "Error while creating temp file", e);
      }

      break;
    case REQUEST_CODE_CROP_IMAGE:

      String path = data.getStringExtra(CropImage.IMAGE_PATH);
      if (path == null) {

        return;
      }

      bitmap = BitmapFactory.decodeFile(mFileTemp.getPath());
      rescaleImage(bitmap);
      mPhoto.setImageBitmap(bitmap);
      break;
    }

    super.onActivityResult(requestCode, resultCode, data);
    /*
     * try { if(requestCode != getActivity().RESULT_CANCELED && data != null) { // We need to recyle
     * unused bitmaps if (photoThumbnail != null) { photoThumbnail.recycle(); } InputStream stream =
     * getActivity().getContentResolver().openInputStream( data.getData()); photoThumbnail =
     * BitmapFactory.decodeStream(stream); stream.close(); gambar.setImageBitmap(photoThumbnail); }
     * } catch (FileNotFoundException e) { e.printStackTrace(); } catch (IOException e) {
     * e.printStackTrace(); }
     */
  }

  protected void submitRegistration() {
    if (bitmap == null) {
      AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
      alert.setMessage(R.string.grubber_registration_profile_picture_warning);
      alert.setPositiveButton(R.string.grubber_registration_profile_picture_warning_yes,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              setPhoto();
            }
          });
      alert.setNegativeButton(R.string.grubber_registration_profile_picture_warning_no,
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // user skips photo
              bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user);
              new RegisterTask().execute();
            }
          });
      alert.show();
    } else {
      new RegisterTask().execute();
    }
  }

  private void submitEdit() {
    // TODO Auto-generated method stub
    mIsSubmitting = true;
    getActivity().supportInvalidateOptionsMenu();
    updateProfile(Utility.getTrimmedText(mFullName.getText()),
	    Utility.getTrimmedText(mPassword.getText()), Utility.getTrimmedText(mAboutMe.getText()),
	    Utility.getTrimmedText(mEmail.getText()), photoBitmap);
  }

  protected boolean validateAboutMe() {
    boolean result = true;
    return result;
  }

  protected boolean validateEmail(boolean isSync) {
    boolean result = true;
    if (!Validate.hasText(mEmail)) {
      mEmail.setError(getResources().getText(R.string.grubber_registration_email));
      result = false;
    } else if (!Validate.isEmailAddress(mEmail, true)) {
      mEmail.setError(getResources().getText(R.string.grubber_registration_email_invalid));
      result = false;
    } else {
      // check if username is taken
      ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
      userQuery.whereEqualTo("email", mEmail.getText().toString().trim());
      if (!isSync) {
        userQuery.findInBackground(new FindCallback<ParseUser>() {
          @Override
          public void done(List<ParseUser> users, ParseException e) {
            if (e == null) {
              if (users != null && users.size() > 0) {
                mEmail.setError(getResources()
                    .getText(R.string.grubber_registration_username_taken));
              }
            } else {
              Log.w(TAG, "Problem checking existing username", e);
              Toast.makeText(getActivity(), getResources().getText(R.string.grubber_backend_error),
                  Toast.LENGTH_LONG).show();
            }
          }
        });
      } else {
        result = false;
        try {
          List<ParseUser> users = userQuery.find();
          if (users != null && users.size() > 0) {
            mEmail.setError(getResources().getText(R.string.grubber_registration_username_taken));
          } else {
            result = true;
          }
        } catch (ParseException e) {
          Log.w(TAG, "Problem checking existing username", e);
          Toast.makeText(getActivity(), getResources().getText(R.string.grubber_backend_error),
              Toast.LENGTH_LONG).show();
        }
      }
    }
    return result;
  }

  protected boolean validatePassword() {
    boolean result = true;
    if (!Validate.hasText(mPassword) || !Validate.isValid(mPassword, Validate.regex_password, true)) {
      mPassword.setError(getResources().getText(R.string.grubber_registration_password_required));
      result = false;
    }
    return result;
  }

  protected boolean validatePassword2() {
    boolean result = true;
    if (mPassword.getError() == null
        && !mPassword.getText().toString().equals(mPassword2.getText().toString())) {
      mPassword2.setError(getResources().getText(R.string.grubber_registration_password2_mismatch));
      result = false;
    }
    return result;
  }

  protected boolean validateUsername() {
    boolean result = true;

    if (!Validate.hasText(mUsername) || !Validate.hasTextLength(mUsername.getText(), 3, 33)) {
      mUsername.setError(getResources().getText(R.string.grubber_registration_screenname_required));
      result = false;
    } else if (!Validate.isValid(mUsername, "^[^\\t\\n\\x0B\\f\\r]{3,33}$", true)) {
      mUsername.setError(getResources().getText(R.string.grubber_registration_screenname_invalid));
      result = false;
    }
    return result;
  }

  protected boolean validateFullName() {
    boolean result = true;
    if (!Validate.hasText(mFullName) || !Validate.hasTextLength(mFullName.getText(), 3, 33)) {
      mFullName.setError(getResources().getText(R.string.grubber_registration_full_name_required));
      result = false;
    } else if (!Validate.isValid(mFullName.getText(), "^[^\\t\\n\\x0B\\f\\r]{3,33}$", true)) {
      mFullName.setError(getResources().getText(R.string.grubber_registration_full_name_invalid));
      result = false;
    }
    return result;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    // try {
    // mListener = (OnFragmentInteractionListener) activity;
    // } catch (ClassCastException e) {
    // throw new ClassCastException(activity.toString()
    // + " must implement OnFragmentInteractionListener");
    // }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public interface OnFragmentInteractionListener {
    public void onSuccessfulRegistration();

    public void onLoginAction(User user);

    public void onSuccessfulEdit();
  }

  class RegisterTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      Utility.lockScreenOrientation(getActivity());
      // show progress bar
      mProgressDialog.show(getActivity().getSupportFragmentManager(),
          ProgressBarDialogFragment.class.getName());
    }

    @Override
    protected Void doInBackground(Void... params) {
      try {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] image = stream.toByteArray();

        UserDao.registerUser(getActivity(), mEmail.getText().toString().trim(), mPassword.getText()
            .toString().trim(), Utility.getTrimmedText(mUsername.getText()),
            Utility.getTrimmedText(mFullName.getText()),
            Utility.getTrimmedText(mAboutMe.getText()), image);
      } catch (ParseException e) {
        Log.w(TAG, "Problem while signing up user", e);
        getView().post(new Runnable() {
          @Override
          public void run() {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setMessage(getActivity().getString(R.string.grubber_backend_error));
            alert.show();
          }
        });
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      Utility.unlockScreenOrientation(getActivity());
      mProgressDialog.dismiss();

      Toast.makeText(getActivity(), "Register user success!", Toast.LENGTH_LONG).show();
      Intent intent = new Intent(getActivity(), TimelineActivity.class);
      startActivity(intent);
    }
  }

  public void updateProfile(String fullname, String password, String aboutMe, String email,
      byte[] photo) {

    /*mUpdateProfileTask = new UpdateProfileTask(fullname, password, aboutMe, email, photo);
    mUpdateProfileTask.execute();*/
	  new UpdateProfileTask().execute();
    Log.i(TAG, "Initiated profile update");
  }

  class UpdateProfileTask extends AsyncTask<Void, Void, Void> {
    private User user = User.getCurrentUser();
    private Exception e;

    /*public UpdateProfileTask(String fullname, String password, String about, String email,
        byte[] photo) {

      Log.v(TAG, "Executing edit ->" + User.getCurrentUser().getParseUser());
      user = User.getCurrentUser();
      user.setParseUser(User.getCurrentUser().getParseUser());
      user.setFullName(fullname);
      user.getParseUser().setPassword(password);
      user.setAboutMe(about);
      user.setEmail(email);
      if (photo != null) {
        ParseFile pp = new ParseFile(fullname.concat(".jpg"), photo);
        try {
          pp.save();
        } catch (ParseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        Log.d(TAG, "Done saving profile photo file");
        user.setPhoto(pp);
      }
    }*/
    
    @Override
    protected Void doInBackground(Void... params) {
      try {
    	if(bitmap != null) {  
    	  ByteArrayOutputStream stream = new ByteArrayOutputStream();
          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
          photoBitmap = stream.toByteArray();
    	}
        UserDao.updateUser(user, Utility.getTrimmedText(mFullName.getText()), mEmail.getText()
            .toString().trim(), Utility.getTrimmedText(mAboutMe.getText()), mPassword.getText()
            .toString().trim(), photoBitmap);
      } catch (ParseException e) {
        Log.w(TAG, "Problem while updating user profile", e);
        this.e = e;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {

      Toast.makeText(getActivity(), "Profile updated!", Toast.LENGTH_LONG).show();
      Intent intent = new Intent(getActivity(), TimelineActivity.class);
      startActivity(intent);
    }
  }

  public void updateView() {
    if (isEdit) {
      // edit profile mode
      Log.v(TAG, "updateView for edit mode");
      
      ImageLoader imageLoader = new ImageLoader(getActivity());
      
      pp = (ParseFile) User.getCurrentUser().getPhoto();
      try {
		photoBitmap = pp.getData();
		Log.v(TAG, "updateView for edit mode " + photoBitmap.toString());
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      imageLoader.DisplayImage(pp.getUrl(), mPhoto);
      //mPhoto.setImageBitmap(bitmap);
      mEmail.setText(User.getCurrentUser().getEmail());
      mEmail.setFocusable(false);
      mUsername.setText(User.getCurrentUser().getUserName());
      mUsername.setFocusable(false);
      mFullName.setText(User.getCurrentUser().getFullName());
      mAboutMe.setText(User.getCurrentUser().getAboutMe());
    }

    if (mIsSubmitting) {
      // submit is in progress
      mPhoto.setEnabled(false);
      mFullName.setEnabled(false);
      mEmail.setEnabled(false);
      mUsername.setEnabled(false);
      mAboutMe.setEnabled(false);

    } else {
      // normal regis mode
      Log.v(TAG, "updateView for regis mode");
      mPhoto.setEnabled(true);
      mFullName.setEnabled(true);
      mAboutMe.setEnabled(true);
      mEmail.setEnabled(true);
      mUsername.setEnabled(true);

    }
  }

}
