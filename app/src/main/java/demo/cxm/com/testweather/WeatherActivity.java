package demo.cxm.com.testweather;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;

import demo.cxm.com.testweather.json.Weather;
import demo.cxm.com.testweather.util.HttpUtil;
import demo.cxm.com.testweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private TextView county;
    private TextView updateTime;
    private TextView temperature;
    private TextView info;
    private TextView hum;
    private TextView cloud;
    private ImageView bing_img;
    public SwipeRefreshLayout refreshLayout;
    public DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_weather);
        county = findViewById(R.id.county_name);
        updateTime = findViewById(R.id.update_time);
        temperature = findViewById(R.id.temperature);
        info = findViewById(R.id.info);
        hum = findViewById(R.id.hum);
        cloud = findViewById(R.id.cloud);
        bing_img = findViewById(R.id.bing_img);
        drawerLayout = findViewById(R.id.drawer_layout);
        refreshLayout = findViewById(R.id.swipe_refreshLayout);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = pref.getString("weather", null);
        String bing_img_Url = pref.getString("bing_img", null);
        final String weatherId;

        if (bing_img_Url != null) {
            Glide.with(this).load(bing_img_Url).into(bing_img);
        } else {
            loadBingPic();
        }

        if (weatherString != null) {
            //有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeather(weather);
        } else {
            weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        county.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /*
    请求图片路经
    */
    private void loadBingPic() {
        String responseBing_url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1560425232738&di=3fe4bcfa418eae35de787feedd8a34e3&imgtype=0&src=http%3A%2F%2Fhbimg.b0.upaiyun.com%2F2f0584ddf3619f50c920722ac61cc8bf2747cb5b2a674-9LW075_fw236";
        HttpUtil.sendOkHttpRequest(responseBing_url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(WeatherActivity.this,
                        "找不着图片路径", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bing_img_Url = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                        WeatherActivity.this).edit();
                editor.putString("bing_img", bing_img_Url);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bing_img_Url).into(bing_img);
                    }
                });
            }
        });
    }

    /*
    请求天气数据
    */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "https://free-api.heweather.com/s6/weather/now?" +
                "key=1e45b377355c4c60be65c1511d353266&location=" + weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,
                                "获取天气失败", Toast.LENGTH_SHORT).show();
                        refreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeather(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this,
                                    "获取天气失败", Toast.LENGTH_SHORT).show();
                        }
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    /*
    处理weather中的数据并且显示出来
    */
    private void showWeather(Weather weather) {
        String CountyName = weather.basic.countyName;
        String UpdateTime = "最近更新时间：" + weather.update.updateTime;
        String Temperature = weather.now.temperature + "℃";
        String Info = weather.now.info;
        String Hum = "空气相对湿度：" + weather.now.hum;
        String Cloud = "云量：" + weather.now.cloud;

        county.setText(CountyName);
        updateTime.setText(UpdateTime);
        temperature.setText(Temperature);
        info.setText(Info);
        hum.setText(Hum);
        cloud.setText(Cloud);
    }
}
