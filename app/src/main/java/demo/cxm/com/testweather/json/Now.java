package demo.cxm.com.testweather.json;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("cloud")
    public String cloud;
    @SerializedName("cond_txt")
    public String info;
    @SerializedName("hum")
    public String hum;
    @SerializedName("tmp")
    public String temperature;
}
