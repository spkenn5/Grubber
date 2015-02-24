package skripsi.com.grubber.dao;

import java.util.List;

import skripsi.com.grubber.model.Activity;
import skripsi.com.grubber.model.Restaurant;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class RestaurantDao {

  private static final String TAG = RestaurantDao.class.getSimpleName();

  public static List<Restaurant> getRest() throws ParseException {
    // posting by self

    ParseQuery<Restaurant> pqMine = ParseQuery.getQuery(Restaurant.class);

    List<Restaurant> result = null;
    try {
      result = pqMine.find();
      Log.d(TAG, String.format("getRest found %s records", result == null ? 0 : result.size()));
    } catch (ParseException e) {
      Log.w(TAG, "Problem in retrieving timeline", e);
      throw e;
    }
    return result;
  }

  public static Restaurant getRestProfileById(String restId) throws ParseException {
    ParseQuery<Restaurant> pqAll = ParseQuery.getQuery(Restaurant.class);

    // pqAll.whereContains(Restaurant.ID, restId);
    pqAll.whereEqualTo(Restaurant.ID, restId);

    Restaurant pu;
    Restaurant temp = null;
    try {
      pu = pqAll.getFirst();
      Log.v(TAG, String.format("Loaded RestProfile [%s]", restId));
      temp = pu;
    } catch (ParseException e) {
      Log.w(TAG, String.format("Problem loading RestProfile [%s]", restId), e);
      throw e;
    }

    return temp;
  }
  
  public static void updateRestaurantRating(Restaurant restId, float cash, float rate, int countReview) {

    Log.v(TAG, "Masuk");
    double newCash = 0, newRate = 0;
    
    if(cash != 0) {
    	newCash = ((restId.getCash() * countReview) + cash) / (countReview + 1);
    	Log.v(TAG, "Coba cash " + cash);
    	Log.v(TAG, "Coba cash parse " + restId.getCash());
    	Log.v(TAG, "Coba count review " + countReview);
    	Log.v(TAG, "Coba cash baru " + newCash);
    	restId.put("cash", newCash);
    }
    
    newRate = ((restId.getStar() * countReview) + rate) / (countReview + 1);
    restId.put("stars", newRate);
    
    restId.saveEventually();
  }
}