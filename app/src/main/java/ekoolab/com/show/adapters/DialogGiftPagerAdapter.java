package ekoolab.com.show.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.Gift;
import ekoolab.com.show.utils.DisplayUtils;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2018/9/30
 * @description
 */
public class DialogGiftPagerAdapter extends PagerAdapter {

    private Context context;
    private List<Gift> gifts;
    private RecyclerView[] recyclerViews;
    private int curGiftPos = -1;

    public DialogGiftPagerAdapter(Context context, List<Gift> gifts) {
        this.context = context;
        this.gifts = gifts;
        recyclerViews = new RecyclerView[(int) Math.ceil(gifts.size() / 8.0)];
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (recyclerViews[position] == null) {
            recyclerViews[position] = new RecyclerView(context);
            recyclerViews[position].setLayoutManager(new GridLayoutManager(context, 4));
            int startIndex = position * 8;
            int endIndex;
            if (startIndex + 8 < gifts.size()) {
                endIndex = startIndex + 8;
            } else {
                endIndex = gifts.size();
            }
            final List<Gift> giftList = this.gifts.subList(startIndex, endIndex);
            GiftAdapter adapter = new GiftAdapter(giftList);
            adapter.setOnItemClickListener((adapter1, view, pos) -> {
               giftList.get(pos).isSelected = true;
               adapter1.notifyDataSetChanged();
               if (curGiftPos != -1) {
                   gifts.get(curGiftPos).isSelected = false;
                   recyclerViews[curGiftPos / 8].getAdapter().notifyDataSetChanged();
               }
               curGiftPos = position * 8 + pos;
            });
            recyclerViews[position].setAdapter(adapter);
        }
        container.addView(recyclerViews[position]);
        return recyclerViews[position];
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return (int) Math.ceil(gifts.size() / 8.0);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public int getCurGiftPos() {
        return curGiftPos;
    }

    private class GiftAdapter extends BaseQuickAdapter<Gift, BaseViewHolder> {

        private int itemWidth, itemHeight;

        public GiftAdapter(@Nullable List<Gift> data) {
            super(R.layout.item_moment_gift, data);
            itemWidth = DisplayUtils.getScreenWidth() / 4;
            itemHeight = (int) (itemWidth * 1.2);
        }

        @Override
        protected void convert(BaseViewHolder helper, Gift item) {
            ViewGroup.LayoutParams params = helper.itemView.getLayoutParams();
            params.width = itemWidth;
            params.height = itemHeight;
            ImageView imageView = helper.getView(R.id.iv_image);
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = itemWidth * 2 / 3;
            layoutParams.height = itemWidth * 2 / 3;
            Glide.with(imageView).load(item.image).into(imageView);
            helper.setText(R.id.tv_name, item.name);
            helper.setText(R.id.tv_money, item.price + "");
            helper.itemView.setSelected(item.isSelected);
        }
    }
}
