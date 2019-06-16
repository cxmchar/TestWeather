package demo.cxm.com.testweather.db;
import org.litepal.crud.LitePalSupport;

public class County extends LitePalSupport {
    private int id;
    private int cityId;
    private String countyName;
    private String weatherId;

    public int getCityId() {
        return cityId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
