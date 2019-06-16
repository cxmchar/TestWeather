package demo.cxm.com.testweather.json;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("parent_city")
    public String cityName;

    @SerializedName("location")
    public String countyName;

    @SerializedName("cid")
    public String weatherId;
}
