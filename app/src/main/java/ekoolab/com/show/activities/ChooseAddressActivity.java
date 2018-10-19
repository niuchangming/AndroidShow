package ekoolab.com.show.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.PoiResultData;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/23
 * @description 发布视频时选择地址的页面
 */
public class ChooseAddressActivity extends BaseActivity implements View.OnClickListener, OnGetPoiSearchResultListener {
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_ADDRESS = "extra_address";
    private TextView tvCancel;
    private TextView tvName;
    private TextView tvSave;
    private EditText etSearch;
    private ImageView ivClear;
    private TextView tvCancelSearch;
    private MapView mapview;
    private BaiduMap baiduMap;
    public LocationClient mLocationClient = null;
    private String cityName = "";
    private RecyclerView recycler;
    private BDLocation bdLocation = null;
    private BDAbstractLocationListener myListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null && bdLocation.hasAddr()) {
                ChooseAddressActivity.this.bdLocation = bdLocation;
                cityName = bdLocation.getCity();
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100)
                        .latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude())
                        .build();
                etSearch.setText(bdLocation.getStreet());
                etSearch.setSelection(etSearch.getText().length());
                baiduMap.setMyLocationData(locData);
                LatLng curLocation = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(curLocation).zoom(17.0f);
                baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                addMarker(bdLocation.getLatitude(), bdLocation.getLongitude(), bdLocation.getStreet());
                mLocationClient.stop();
            }
        }
    };
    private BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding);
    private PoiSearch mPoiSearch;
    private BaseQuickAdapter<PoiResultData, BaseViewHolder> adapter;
    private List<PoiResultData> resultDataList = new ArrayList<>();

    @Override
    protected void initData() {
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(10 * 1000);
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        mLocationClient.setLocOption(option);
        // 初始化搜索模块，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
    }

    @Override
    protected void initViews() {
        super.initViews();
        tvCancel = findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(this);
        tvName = findViewById(R.id.tv_name);
        tvName.setText(R.string.choose_place);
        tvSave = findViewById(R.id.tv_save);
        tvSave.setVisibility(View.GONE);
        etSearch = findViewById(R.id.et_search);
        ivClear = findViewById(R.id.iv_clear);
        ivClear.setOnClickListener(this);
        tvCancelSearch = findViewById(R.id.tv_cancel_search);
        tvCancelSearch.setOnClickListener(this);
        mapview = findViewById(R.id.mapview);
        baiduMap = mapview.getMap();
        baiduMap.setMyLocationEnabled(true);
        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new LinearItemDecoration(this, 1,
                R.color.gray_very_light, 0));
        recycler.setAdapter(adapter = new BaseQuickAdapter<PoiResultData, BaseViewHolder>(R.layout.layout_poiresult_item, resultDataList) {
            @Override
            protected void convert(BaseViewHolder helper, PoiResultData item) {
                helper.setText(R.id.tv_name, item.name);
                helper.setText(R.id.tv_address, item.address);
            }
        });
        adapter.setOnItemClickListener((adapter, view, position) -> {
            if (position > -1 && position < resultDataList.size()) {
                PoiResultData resultData = resultDataList.get(position);
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100)
                        .latitude(resultData.latitude)
                        .longitude(resultData.longitude)
                        .build();
                baiduMap.setMyLocationData(locData);
                LatLng curLocation = new LatLng(resultData.latitude, resultData.longitude);
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(curLocation).zoom(17.0f);
                baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                addMarker(resultData.latitude, resultData.longitude, resultData.name);
                recycler.setVisibility(View.GONE);
            }
        });
        mLocationClient.start();
        etSearch.setOnClickListener(this);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!TextUtils.isEmpty(etSearch.getText())) {
                    PoiCitySearchOption searchOption = new PoiCitySearchOption()
                            .city(bdLocation.getCity())
                            .keyword(etSearch.getText().toString())
                            .pageNum(0)
                            .scope(1);
                    mPoiSearch.searchInCity(searchOption);
//                    PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption()
//                            .keyword(etSearch.getText().toString())
//                            .location(new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude()))
//                            .radius(100000)
//                            .pageNum(0)
//                            .scope(1);
//                    mPoiSearch.searchNearby(nearbySearchOption);
                    Utils.hideInput(etSearch);
                }
                return true;
            }
            return false;
        });
    }

    private void addMarker(double lat, double lnt, String address) {
        baiduMap.clear();
        LatLng llA = new LatLng(lat, lnt);
        MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdA)
                .zIndex(9).draggable(true);
        baiduMap.addOverlay(ooA);
        TextView button = new TextView(getApplicationContext());
        button.setBackgroundResource(R.drawable.popup);
        InfoWindow.OnInfoWindowClickListener listener = null;
        button.setText(address);
        button.setTextColor(Color.BLACK);
        int paddingLeftRight = DisplayUtils.dip2px(8);
        int paddingTop = paddingLeftRight;
        int paddingBottom = paddingTop * 2;
        button.setPadding(paddingLeftRight, paddingTop, paddingLeftRight, paddingBottom);
        listener = new InfoWindow.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick() {
                baiduMap.hideInfoWindow();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_LATITUDE, lat);
                intent.putExtra(EXTRA_LONGITUDE, lnt);
                intent.putExtra(EXTRA_ADDRESS, address);
                setResult(RESULT_OK, intent);
                finish();
            }
        };
        InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), llA, -47, listener);
        baiduMap.showInfoWindow(mInfoWindow);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_choose_address;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                onBackPressed();
                break;
            case R.id.et_search:
                tvCancelSearch.setVisibility(View.VISIBLE);
                recycler.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_cancel_search:
                tvCancelSearch.setVisibility(View.GONE);
                recycler.setVisibility(View.GONE);
                break;
            case R.id.iv_clear:
                etSearch.setText("");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        mapview.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mapview.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        baiduMap.setMyLocationEnabled(false);
        mapview.onDestroy();
        mapview = null;
        mPoiSearch.destroy();
        super.onDestroy();
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(this, "未找到结果", Toast.LENGTH_LONG).show();
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            List<PoiResultData> resultData = convert(result.getAllPoi());
            adapter.replaceData(resultData);
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            StringBuilder strInfo = new StringBuilder("在");
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                if (strInfo.length() != 1) {
                    strInfo.append(",");
                }
                strInfo.append(cityInfo.city);
            }
            strInfo.append("找到结果");
            ToastUtils.showToast(strInfo.toString());
        }
    }

    private List<PoiResultData> convert(List<PoiInfo> allPoi) {
        List<PoiResultData> resultDatas = new ArrayList<>();
        for (PoiInfo poiInfo : allPoi) {
            PoiResultData resultData = new PoiResultData();
            resultData.address = poiInfo.address;
            resultData.area = poiInfo.area;
            resultData.city = poiInfo.city;
            resultData.latitude = poiInfo.location.latitude;
            resultData.longitude = poiInfo.location.longitude;
            resultData.province = poiInfo.province;
            resultData.name = poiInfo.name;
            resultDatas.add(resultData);
        }
        return resultDatas;
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
        if (poiDetailSearchResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
            List<PoiDetailInfo> poiDetailInfoList = poiDetailSearchResult.getPoiDetailInfoList();
            if (null == poiDetailInfoList || poiDetailInfoList.isEmpty()) {
                Toast.makeText(this, "抱歉，检索结果为空", Toast.LENGTH_SHORT).show();
                return;
            }

            for (int i = 0; i < poiDetailInfoList.size(); i++) {
                PoiDetailInfo poiDetailInfo = poiDetailInfoList.get(i);
                if (null != poiDetailInfo) {
                    Toast.makeText(this,
                            poiDetailInfo.getName() + ": " + poiDetailInfo.getAddress(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }
}
