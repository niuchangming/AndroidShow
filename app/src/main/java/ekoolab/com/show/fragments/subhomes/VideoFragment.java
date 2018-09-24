package ekoolab.com.show.fragments.subhomes;

import android.content.Intent;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.santalu.emptyview.EmptyView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.VideoPlayerActivity;
import ekoolab.com.show.adapters.VideoAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Video;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.JsonParser.JSONParser.ParserListener;
import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.GridSpacingItemDecoration;
import me.shihao.library.XRecyclerView;

public class VideoFragment extends BaseFragment implements ParserListener, VideoAdapter.OnItemClickListener {
    private final String TAG = "VideoFragment";
    private int pageIndex;
    private EmptyView emptyView;
    private XRecyclerView recyclerView;
    private VideoAdapter adapter;
    private ArrayList<Video> videos= new ArrayList<Video>();

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
        adapter.setHasStableIds(false);
        recyclerView.setAdapter(adapter);
        loadVideoData(0);
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        emptyView = holder.get(R.id.empty_view);
        recyclerView = holder.get(R.id.recycler_view);
        int spanCount = 2;
        int spacing = 2;
        recyclerView.gridLayoutManager(spanCount);
        ((SimpleItemAnimator)recyclerView.getRecyclerView().getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.getRecyclerView().addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        recyclerView.setOnRefreshListener(new XRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageIndex = 0;
                loadVideoData(1);
            }

            @Override
            public void onLoadMore() {
                loadVideoData(2);
            }
        });
//        recyclerView.autoRefresh();
    }

    private void loadVideoData(int flag) {
        if(flag==0){
            emptyView.showLoading();
        }
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
                        try {
                            if (ListUtils.isNotEmpty(videoList)) {
                                pageIndex++;
                                if(flag==2){
                                    videos.addAll(videoList);
                                    adapter.notifyItemRangeChanged(videos.size()-videoList.size(),videos.size());
                                }else if(flag==1){
                                    adapter.notifyItemRangeRemoved(videoList.size(),videos.size());
                                    videos.clear();
                                    videos.addAll(videoList);
                                    adapter.notifyItemRangeChanged(0,videos.size());
                                }else{
                                    videos.clear();
                                    videos.addAll(videoList);
                                    adapter.notifyDataSetChanged();
                                }
                                recyclerView.refreshComlete();
                                emptyView.content().show();
                            } else {
                                emptyView.showEmpty();
                            }
                        }catch (Exception e){
                            recyclerView.refreshComlete();
                            e.printStackTrace();
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
        recyclerView.refreshComlete();
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putParcelableArrayListExtra("videos", this.videos);

        for (int i = 0; i < videos.size(); i++) {
            if (videos.get(i).resourceId.equalsIgnoreCase(video.resourceId)) {
                intent.putExtra("current_index", i);
                break;
            }
        }

       startActivityForResult(intent,0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0&&resultCode==2){
            if(videos!=null&&videos.size()!=0){
                ArrayList<Video> videoArrayList = data.getParcelableArrayListExtra("videos");
                if(videoArrayList!=null&&videoArrayList.size()!=0){
                    videos.clear();
                    videos.addAll(videoArrayList);
                }
            }
            adapter.notifyDataSetChanged();
        }
    }
}
