package skripsi.com.grubber;

//import java.text.SimpleDateFormat;
import skripsi.com.android.Utility;
import skripsi.com.grubber.model.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that contain this fragment
 * must implement the {@link SplashFragment.OnFragmentInteractionListener} interface to handle
 * interaction events. Use the {@link SplashFragment#newInstance} factory method to create an
 * instance of this fragment.
 * 
 */
public class LoginFragment extends DialogFragment {
  private static final String TAG = LoginFragment.class.getSimpleName();
  private OnFragmentInteractionListener mListener;
  private EditText mUsername = null;
  private EditText mPassword = null;

  private ProgressBarDialogFragment mProgressDialog = null;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setRetainInstance(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.v(TAG, "LoginFragment onCreateView called");
    mProgressDialog = new ProgressBarDialogFragment();
    mProgressDialog.setCancelable(false);

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.home_screen, container, false);

    mUsername = (EditText) view.findViewById(R.id.etUsername);
    mUsername.setFilters(new InputFilter[] { new LengthFilter(100) });
    mPassword = (EditText) view.findViewById(R.id.etPassword);
    mPassword.setFilters(new InputFilter[] { new LengthFilter(33) });

    // Set listener for Register button
    view.findViewById(R.id.btnRegister).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        goToRegistration();
      }
    });

    // Set listener for Login button
    view.findViewById(R.id.btnLogin).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        login();
      }
    });

    return view;
  }

  protected void login() {
    Utility.lockScreenOrientation(getActivity());
    mProgressDialog.show(getActivity().getSupportFragmentManager(),
        ProgressBarDialogFragment.class.getName());

    boolean proceedToLogin = true;
    String username = mUsername.getText().toString();
    if (username.length() == 0) {
      mUsername.setError(getResources().getString(R.string.grubber_registration_username_required));
      proceedToLogin = false;
    }
    String password = mPassword.getText().toString();
    if (password.length() == 0) {
      mPassword.setError(getResources().getString(R.string.grubber_registration_password_required));
      proceedToLogin = false;
    }
    if (proceedToLogin) {
      ParseUser.logInInBackground(username, password, new LogInCallback() {
        @Override
        public void done(ParseUser user, ParseException e) {
          if (e == null && user != null) {
            User u = new User(user);
            mListener.onLoginAction(u);

          } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setMessage(getResources().getString(R.string.grubber_main_password_login_failed));
            alert.show();
          }
          mProgressDialog.dismiss();
          Utility.unlockScreenOrientation(getActivity());
        }
      });
    } else {
      mProgressDialog.dismiss();
      Utility.unlockScreenOrientation(getActivity());
    }
  }

  protected void goToRegistration() {
    mListener.onRegisterAction();
  }

  protected void goToResetPassword() {
    mListener.onResetPasswordAction();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this fragment to allow an
   * interaction in this fragment to be communicated to the activity and potentially other fragments
   * contained in that activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating with
   * Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    public void onLoginAction(User user);

    public void onRegisterAction();

    public void onResetPasswordAction();
  }
}
