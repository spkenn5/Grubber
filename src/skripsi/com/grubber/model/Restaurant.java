package skripsi.com.grubber.model;

import java.io.Serializable;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Restaurant")
public class Restaurant extends ParseObject implements Serializable {

  private static final String TAG = Restaurant.class.getSimpleName();

  // these TYPES are the ones that can be displayed
  public ParseObject restId = null;
  public static final String ID = "objectId";
  public static final String NAME = "restName";
  public static final String DESC = "description";
  public static final String CITY = "city";
  public static final String ADDRESS = "address";
  public static final String LONG = "longitude";
  public static final String LAT = "latitude";
  public static final String STAR = "stars";
  public static final String CASH = "cash";

  public ParseObject getRestObject() {
    return getParseObject(ID);
  }

  public String getName() {
    return getString(NAME);
  }

  public String getCity() {
    return getString(CITY);
  }

  public String getAddress() {
    return getString(ADDRESS);
  }

  public String getDesc() {
    return getString(DESC);
  }

  public float getStar() {
    return (float) getDouble(STAR);
  }

  public float getCash() {
    return (float) getDouble(CASH);
  }

  public double getLat() {
    return getDouble(LAT);
  }

  public double getLong() {
    return getDouble(LONG);
  }

}
