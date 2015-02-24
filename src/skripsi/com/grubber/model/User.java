package skripsi.com.grubber.model;

import com.parse.ParseFile;
import com.parse.ParseUser;

public class User {
  public ParseUser parseUser = null;
  public Activity act = null;
  public static final String FULL_NAME = "fullName";
  public static final String EMAIL = "email";
  public static final String PARSE_USER = "parseUser";
  public static final String objectId = "objectId";
  public static final String USERNAME = "username";
  public static final String ABOUT_ME = "aboutMe";
  public static final String PHOTO = "profilePic";
  public static final String PHOTO_THUMBNAIL = "photoThumbnail";

  public static final String STALK_COUNTER = "stalkCounter";
  public static final String STALKER_COUNTER = "stalkerCounter";
  public static final String REVIEW_COUNTER = "reviewCounter";

  public static User getCurrentUser() {
    ParseUser pu = ParseUser.getCurrentUser();
    User user = null;
    if (pu != null) {
      user = new User();
      user.parseUser = pu;
    }
    return user;
  }

  public User() {
    this.parseUser = new ParseUser();
  }

  public User(ParseUser parseUser) {
    this.parseUser = parseUser;
  }

  public User(Activity act) {
    this.act = act;
  }

  public ParseUser getParseUser() {
    return parseUser;
  }

  public void setParseUser(ParseUser parseUser) {
    this.parseUser = parseUser;
  }

  public String getEmail() {
    return this.parseUser.getEmail();
  }

  public void setEmail(String email) {
    this.parseUser.setEmail(email);
  }

  public String getUserName() {
    return this.parseUser.getUsername();
  }

  public void setUserName(String username) {
    parseUser.put(USERNAME, username);
  }

  public String getFullName() {
    return this.parseUser.getString(FULL_NAME);
  }

  public void setFullName(String fullName) {
    parseUser.put(FULL_NAME, fullName);
  }

  public String getAboutMe() {
    return this.parseUser.getString(ABOUT_ME);
  }

  public void setAboutMe(String aboutMe) {
    parseUser.put(ABOUT_ME, aboutMe);
  }

  public ParseFile getPhoto() {
    return this.parseUser.getParseFile(PHOTO);
  }

  public void setPhoto(ParseFile pf) {
    parseUser.put(PHOTO, pf);
  }

  public ParseFile getPhotoThumbnail() {
    return this.parseUser.getParseFile(PHOTO_THUMBNAIL);
  }

  public void setPhotoThumbnail(ParseFile pf) {
    parseUser.put(PHOTO_THUMBNAIL, pf);
  }

  public String getObjectId() {
    return parseUser.getObjectId();
  }

  public static boolean isMyUsername(String username) {
    return username != null && getCurrentUser() != null
        && username.equalsIgnoreCase(getCurrentUser().getUserName());
  }

  public static boolean isMyUserId(String userId) {
    return getCurrentUser() != null && getCurrentUser().getObjectId().equals(userId);
  }

  public static boolean isMe(User user, String userId, String username) {
    return (getCurrentUser() != null && getCurrentUser().equals(user)) || isMyUserId(userId)
        || isMyUsername(username);
  }
}
