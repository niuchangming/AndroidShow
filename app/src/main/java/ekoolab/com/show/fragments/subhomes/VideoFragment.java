package ekoolab.com.show.fragments.subhomes;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.santalu.emptyview.EmptyView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.VideoPlayerActivity;
import ekoolab.com.show.adapters.VideoAdapter;
import ekoolab.com.show.beans.AuthInfo;
import ekoolab.com.show.beans.Video;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.JsonParser.JSONParser;
import ekoolab.com.show.utils.JsonParser.JSONParser.ParserListener;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.GridSpacingItemDecoration;

public class VideoFragment extends BaseFragment implements ParserListener, VideoAdapter.OnItemClickListener {
    private final String TAG = "VideoFragment";
    private int pageIndex;
    private EmptyView emptyView;
    private RecyclerView recyclerView;
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

    private void loadVideoData(){
        emptyView.showLoading();
        AndroidNetworking.post(Constants.VIDEO_LIST)
                .addBodyParameter("timestamp", new Date().getTime() + "")
                .addBodyParameter("pageSize", Constants.PAGE_SIZE + "")
                .addBodyParameter("pageIndex", pageIndex + "")
                .addBodyParameter("token", AuthUtils.getInstance(getContext()).getApiToken())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            int errorCode = response.getInt("errorCode");
                            String message = response.getString("message");
                            if (errorCode == 1) {
                                String data = response.getString("data");
                                JSONParser.getInstance(VideoFragment.this).start(data, Video.class);
                            } else {
                                emptyView.error().setErrorText(message).show();
                            }
                        }catch (JSONException e){
                            emptyView.error(e).show();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        emptyView.error(error).show();
                    }
                });
    }

    @Override
    public void onParseSuccess(Object obj) {
        if(obj instanceof List){
            videos.clear();
            List<Video> fetchedVideos = (ArrayList<Video>) obj;
            if(fetchedVideos != null && fetchedVideos.size() > 0){
                videos.addAll(fetchedVideos);
                adapter.notifyDataSetChanged();
                emptyView.content().show();
            }else{
                emptyView.showEmpty();
            }
        }else{
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

        for(int i = 0; i < videos.size(); i++){
            if (videos.get(i).resourceId.equalsIgnoreCase(video.resourceId)) {
                intent.putExtra("current_index", i);
                break;
            }
        }

        startActivity(intent);
    }
}
