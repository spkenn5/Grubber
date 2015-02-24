package skripsi.com.grubber.model;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class AuditableParseObject extends ParseObject {
  public static final String CREATED_AT = "createdAt";
  public static final String CREATED_BY = "createdBy";
  public static final String UPDATED_AT = "updatedAt";
  public static final String UPDATED_BY = "updatedBy";
  public static final String OBJECT_ID = "objectId";
  public static final String RESTO_ID = "restoId";

  public void setCreatedBy(User up) {
    put(CREATED_BY, up.getParseUser());
  }

  public ParseUser getCreatedBy() {
    return (ParseUser) get(CREATED_BY);
  }

  public void setUpdatedBy(User up) {
    put(UPDATED_BY, up.getParseUser());
  }

  public User getUpdatedBy() {
    return (User) get(UPDATED_BY);
  }

  public Restaurant getResoId() {
    return (Restaurant) get(RESTO_ID);
  }

}
