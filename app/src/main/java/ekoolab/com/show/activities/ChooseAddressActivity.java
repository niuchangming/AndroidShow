package ekoolab.com.show.activities;

import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

import ekoolab.com.show.R;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/23
 * @description 发布视频时选择地址的页面
 */
public class ChooseAddressActivity extends BaseActivity implements View.OnClickListener {
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
    private BDAbstractLocationListener myListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null && bdLocation.hasAddr()) {
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
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(17.0f);
                baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                addMarker(bdLocation.getLatitude(), bdLocation.getLongitude(), bdLocation.getStreet());
                mLocationClient.stop();
            }
        }
    };
    private BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.mipmap.icon_gcoding);

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
        recycler.addItemDecoration(new LinearItemDecoration(this, DisplayUtils.dip2px(15), R.color.gray_very_light));
        mLocationClient.start();
        etSearch.setOnClickListener(this);
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH && event.getAction() == KeyEvent.ACTION_DOWN) {

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

                break;
            case R.id.tv_cancel_search:
                tvCancelSearch.setVisibility(View.GONE);
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
        super.onDestroy();
    }
}
