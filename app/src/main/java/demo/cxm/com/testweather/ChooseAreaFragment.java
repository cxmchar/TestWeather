package demo.cxm.com.testweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import demo.cxm.com.testweather.db.City;
import demo.cxm.com.testweather.db.County;
import demo.cxm.com.testweather.db.Province;
import demo.cxm.com.testweather.util.HttpUtil;
import demo.cxm.com.testweather.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int PROVINCE = 0;
    public static final int CITY = 1;
    public static final int COUNTY = 2;

    private ProgressBar progressBar;
    private TextView textView;
    private Button button;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;
    private int selectedLevel;
    //set fragement
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        textView = view.findViewById(R.id.title_text);
        button = view.findViewById(R.id.back);
        progressBar = view.findViewById(R.id.progress_bar);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(selectedLevel == PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if(selectedLevel == CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if(selectedLevel == COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity()instanceof MainActivity){
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity()instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.refreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedLevel == COUNTY){
                    queryCities();
                }else if(selectedLevel == CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    /*
        查询全国省份，优先数据库查询，数据库没有再去服务器查询
    */
    private void queryProvinces(){
        textView.setText("中国");
        button.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if(provinceList.size() > 0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectedLevel = PROVINCE;
        }else {
            progressBar.setVisibility(View.VISIBLE);
            String address = "http://guolin.tech/api/china";
            queryFromInternet(address, "province");
        }
    }
    /*
        查询全省城市，优先数据库查询，数据库没有再去服务器查询
    */
    private void queryCities(){
        textView.setText(selectedProvince.getProvinceName());
        button.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        cityList = LitePal.where("provinceId = ?", String.valueOf(
                selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectedLevel = CITY;
        }else {
            progressBar.setVisibility(View.VISIBLE);
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromInternet(address, "city");
        }
    }
    /*
        查询全省城市，优先数据库查询，数据库没有再去服务器查询
    */
    private void queryCounties(){
        textView.setText(selectedCity.getCityName());
        button.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        countyList = LitePal.where("cityId = ?", String.valueOf(
                selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            selectedLevel = COUNTY;
        }else {
            progressBar.setVisibility(View.VISIBLE);
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/"+cityCode;
            queryFromInternet(address, "county");
        }
    }
    /*
        向服务器查询数据
    */
    private void queryFromInternet(String address, String type){
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,
                            selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,
                            selectedCity.getId());
                }
                if(result){
                    //回到主线程处理逻辑
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

}

