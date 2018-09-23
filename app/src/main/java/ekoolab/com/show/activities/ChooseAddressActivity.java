package ekoolab.com.show.activities;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import ekoolab.com.show.R;

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
    private BDAbstractLocationListener myListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation != null && bdLocation.hasAddr() && bdLocation.hasAltitude()) {
                mLocationClient.stop();
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100)
                        .latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude())
                        .build();
                baiduMap.setMyLocationData(locData);
                LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    };

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
        tvCancelSearch = findViewById(R.id.tv_cancel_search);
        mapview = findViewById(R.id.mapview);
        baiduMap = mapview.getMap();
        baiduMap.setMyLocationEnabled(true);
        mLocationClient.start();
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
