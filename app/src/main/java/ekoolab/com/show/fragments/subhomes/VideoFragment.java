package ekoolab.com.show.fragments.subhomes;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.reflect.TypeToken;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.santalu.emptyview.EmptyView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.VideoPlayerActivity;
import ekoolab.com.show.adapters.VideoAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.AuthInfo;
import ekoolab.com.show.beans.Video;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.JsonParser.JSONParser;
import ekoolab.com.show.utils.JsonParser.JSONParser.ParserListener;
import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.utils.RxUtils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.GridSpacingItemDecoration;

public class VideoFragment extends BaseFragment implements ParserListener, VideoAdapter.OnItemClickListener {
    private final String TAG = "VideoFragment";
    private int pageIndex;
    private EmptyView emptyView;
    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private ArrayList<Video> videos = new ArrayList<Video>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_video;
    }


    @Override
    protected void initData() {
        super.initData();

        pageIndex = 0;
        adapter = new VideoAdapter(getActivity(), videos);
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);
        loadVideoData();
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        emptyView = holder.get(R.id.empty_view);
        recyclerView = holder.get(R.id.recycler_view);

        int spanCount = 2;
        int spacing = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
    }

    private void loadVideoData() {
        emptyView.showLoading();
        HashMap<String, String> map = new HashMap<>(4);
        map.put("timestamp", System.currentTimeMillis() + "");
        map.put("pageSize", Constants.PAGE_SIZE + "");
        map.put("pageIndex", pageIndex + "");
        map.put("token", AuthUtils.getInstance(getContext()).getApiToken());
        ApiServer.basePostRequest(this, Constants.VIDEO_LIST, map,
                new TypeToken<ResponseData<List<Video>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Video>>() {
                    @Override
                    protected void onSuccess(List<Video> videoList) {
                        if (ListUtils.isNotEmpty(videoList)) {
                            videos.clear();
                            videos.addAll(videoList);
                            adapter.notifyDataSetChanged();
                            emptyView.content().show();
                        } else {
                            emptyView.showEmpty();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        emptyView.error(e).show();
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    @Override
    public void onParseSuccess(Object obj) {
        if (obj instanceof List) {
            videos.clear();
            List<Video> fetchedVideos = (ArrayList<Video>) obj;
            if (fetchedVideos != null && fetchedVideos.size() > 0) {
                videos.addAll(fetchedVideos);
                adapter.notifyDataSetChanged();
                emptyView.content().show();
            } else {
                emptyView.showEmpty();
            }
        } else {
            emptyView.error().setErrorText(R.string.format_error).show();
        }
    }

    @Override
    public void onParseError(String err) {
        emptyView.error().setErrorText(err).show();
    }

    @Override
    public void onItemClick(Video video) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putParcelableArrayListExtra("videos", this.videos);

        for (int i = 0; i < videos.size(); i++) {
            if (videos.get(i).resourceId.equalsIgnoreCase(video.resourceId)) {
                intent.putExtra("current_index", i);
                break;
            }
        }

        startActivity(intent);
    }
}
