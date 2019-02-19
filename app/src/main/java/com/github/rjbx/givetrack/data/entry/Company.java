package com.github.rjbx.givetrack.data.entry;

import android.content.ContentValues;
import java.util.Map;

public interface Company extends Entry {

    String getEin();
    void setEin(String ein);
    String getName();
    void setName(String name);
    String getLocationStreet();
    void setLocationStreet(String locationStreet);
    String getLocationDetail();
    void setLocationDetail(String locationDetail);
    String getLocationCity();
    void setLocationCity(String locationCity);
    String getLocationState();
    void setLocationState(String locationState);
    String getLocationZip();
    void setLocationZip(String locationZip);
    String getHomepageUrl();
    void setHomepageUrl(String homepageUrl);
    String getNavigatorUrl();
    void setNavigatorUrl(String navigatorUrl);
    String getPhone();
    void setPhone(String phone);
    String getEmail();
    void setEmail(String email);
    String getImpact();
    void setImpact(String impact);
    int getType();
    void setType(int type);
    Map<String, Object> toParameterMap();
    ContentValues toContentValues();
    void fromContentValues(ContentValues values);
}
