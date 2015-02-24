package skripsi.com.grubber.dao;

import java.util.ArrayList;
import java.util.List;

import skripsi.com.grubber.model.Activity;
import skripsi.com.grubber.model.AuditableParseObject;
import skripsi.com.grubber.model.User;
import android.content.Context;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class UserDao extends ParseObject {
  private static final String TAG = UserDao.class.getSimpleName();

  public static User registerUser(Context context, String email, String password, String username,
      String fullname, String aboutMe, byte[] photo) throws ParseException {
    // validation is already done, now attempt registration
    User user = new User();
    user.getParseUser().setUsername(username);
    user.getParseUser().setEmail(email);
    user.getParseUser().setPassword(password);

    // ------------------------------------------------------
    // , byte[] photo
    assert (ParseUser.getCurrentUser() != null);
    user.setUserName(username);
    user.setFullName(fullname);
    user.setAboutMe(aboutMe);
    if (photo != null) {
      ParseFile pp = new ParseFile(username.concat(".jpg"), photo);
      //ParseFile pp = Utility.savePicture(photoThumbnail, username.concat("-tn.png"));
      pp.save();
      Log.d(TAG, "Done saving profile photo file");
      user.setPhoto(pp);
    }
    /*
     * if (photoThumbnail != null) { ParseFile thumbnail = Utility.savePicture(photoThumbnail,
     * username.concat("-tn.png")); thumbnail.save(); Log.d(TAG,
     * "Done saving profile photo thumbnail file"); user.setPhotoThumbnail(thumbnail);
     * photoThumbnail.recycle(); }
     */// ------------------------------------------------------
    try {
      user.getParseUser().signUp();
    } catch (ParseException e) {
      Log.v(TAG, "Problem while signing up user", e);
      throw e;
    }

    return user;
  }

  public static void updateUser(User user, String fullname, String email, String about,
      String password, byte[] photo) throws ParseException {
    user.setEmail(email);
    user.setFullName(fullname);
    user.setAboutMe(about);
    if(!password.equals("")) {
    	user.getParseUser().setPassword(password);
    }
    if (photo != null) {
      ParseFile pp = new ParseFile(fullname.concat(".jpg"), photo);
      pp.save();
      Log.d(TAG, "Done saving profile photo file");
      user.setPhoto(pp);
    }
    user.getParseUser().saveEventually();
    Log.d(TAG, "Done updating profile");
  }

  public static int getRevCountById(ParseUser userId) throws ParseException {

    ParseQuery<Activity> pqAll = new ParseQuery<Activity>("Activity");
    pqAll.whereEqualTo(Activity.TYPE, Activity.TYPE_REVIEW);
    pqAll.whereEqualTo(AuditableParseObject.CREATED_BY, userId);

    int totalRevCount;
    try {
      totalRevCount = pqAll.count();
      Log.v(TAG, String.format("Loaded UserProfile [%s]", userId.toString()));
    } catch (ParseException e) {
      Log.w(TAG, String.format("Problem loading UserProfile [%s]", userId.toString()), e);
      throw e;
    }

    return totalRevCount;
  }

  public static int getStalkCountById(ParseUser userId) throws ParseException {

    ParseQuery<Activity> pqAll = new ParseQuery<Activity>("Activity");
    pqAll.whereEqualTo(Activity.TYPE, Activity.TYPE_STALK);
    pqAll.whereEqualTo(AuditableParseObject.CREATED_BY, userId);

    int totalStalkCount;
    try {
      pqAll.find();
      totalStalkCount = pqAll.count();
      Log.v(TAG, String.format("Loaded UserProfile [%s]", userId.toString()));
    } catch (ParseException e) {
      Log.w(TAG, String.format("Problem loading UserProfile [%s]", userId.toString()), e);
      throw e;
    }

    return totalStalkCount;
  }

  public static int getStalkedCountById(ParseUser userId) throws ParseException {

    ParseQuery<Activity> pqAll = new ParseQuery<Activity>("Activity");
    pqAll.whereEqualTo(Activity.TYPE, Activity.TYPE_STALK);
    pqAll.whereEqualTo(Activity.TARGET_USER_PROFILE, userId);

    int totalStalkCount;
    try {
      pqAll.find();
      totalStalkCount = pqAll.count();
      Log.v(TAG, String.format("Loaded UserProfile [%s]", userId.toString()));
    } catch (ParseException e) {
      Log.w(TAG, String.format("Problem loading UserProfile [%s]", userId.toString()), e);
      throw e;
    }

    return totalStalkCount;
  }

  public static List<User> getUser(String username, String objectId) throws ParseException {
    ParseQuery<ParseUser> query1 = ParseQuery.getQuery(ParseUser.class);
    Log.v(TAG, "Getting " + username + " & " + objectId);
    query1.whereEqualTo(AuditableParseObject.OBJECT_ID, objectId);
    query1.whereEqualTo(User.USERNAME, username);

    List<User> result = null;
    List<ParseUser> temp = null;
    int recordsFound = 0;
    try {
      recordsFound = query1.count();
      Log.v(TAG, "User like found -> " + recordsFound);
      temp = query1.find();

      if (temp != null && !temp.isEmpty()) {
        result = new ArrayList<User>();
        for (ParseUser parseUser : temp) {
          result.add(new User(parseUser));
        }
      }
      Log.d(TAG,
          String.format("Search people found %s records", result == null ? 0 : result.size()));
    } catch (ParseException e) {
      Log.w(TAG, "Problem in retrieving people", e);
      throw e;
    }
    return result;
  }
}
