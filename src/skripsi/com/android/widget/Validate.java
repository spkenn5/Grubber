package skripsi.com.android.widget;

import java.util.regex.Pattern;

import android.graphics.Color;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

public class Validate {
  private static final String TAG = Validate.class.getSimpleName();

  public static final int VALID_TEXT_COLOR = Color.BLACK;
  public static final int INVALID_TEXT_COLOR = Color.RED;

  public static final String regex_email = "^([^@\\s]+)@((?:[-a-z0-9]+\\.)+[a-z]{2,})$";
  public static final String regex_phone = "^\\(?\\d{3}[\\)?|-]\\d{3}[- ]\\d{4}((\\s=?)(ext\\.|x)\\d{1,4})?$";
  public static final String regex_postal_code = "^\\d{5}(-\\d{4})?$";
  public static final String regex_password = "[^\\s]{6,20}";

  public static final long PHOTO_FILE_MAX_SIZE = 1024 * 1024 * 2;// 2 MB

  public static boolean isEmailAddress(EditText editText, boolean required) {
    Log.d(TAG, "isEmailAddress()");
    return isValid(editText, regex_email, required);
  }

  public static boolean isPhoneNumber(EditText editText, boolean required) {
    Log.d(TAG, "isPhoneNumber()");
    return isValid(editText, regex_phone, required);
  }

  public static boolean isPostalCode(EditText editText, boolean required) {
    Log.d(TAG, "isPostalCode()");
    return isValid(editText, regex_postal_code, required);
  }

  public static boolean isValid(EditText editText, String regex, boolean required) {
    boolean result = isValid(editText.getText(), regex, required);
    return result;
  }

  public static boolean isValid(Editable editable, String regex, boolean required) {
    Log.d(TAG, "isValid()");

    boolean validated = true;
    String text = editable.toString().trim();
    boolean hasText = hasText(editable);

    if (required && !hasText)
      validated = false;

    if (validated && hasText) {
      if (!Pattern.matches(regex, text)) {
        validated = false;
      }
    }

    return validated;

  }

  public static boolean hasText(EditText editText) {
    return hasText(editText.getText());
  }

  public static boolean hasText(Editable editable) {
    Log.d(TAG, "hasText()");

    boolean validated = true;

    String text = editable.toString().trim();

    if (text.length() == 0) {
      editable.clear();
      editable.append(text);
      validated = false;
    }

    return validated;
  }

  public static boolean hasTextLength(CharSequence charSequence, int min, int max) {
    boolean result = true;
    if (charSequence == null || charSequence.length() < min || charSequence.length() > max) {
      result = false;
    }
    return result;
  }

  public static boolean isValidImageFileSize(int size) {
    //return size > 0 && size <= PHOTO_FILE_MAX_SIZE;
    return true;
  }
}
