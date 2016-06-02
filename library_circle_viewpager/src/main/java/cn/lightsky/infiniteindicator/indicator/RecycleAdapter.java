package cn.lightsky.infiniteindicator.indicator;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cn.lightsky.infiniteindicator.jakewharton.salvage.RecyclingPagerAdapter;
import cn.lightsky.infiniteindicator.slideview.BaseSliderView;

public class RecycleAdapter extends RecyclingPagerAdapter implements BaseSliderView.BitmapLoadCallBack {

    private ArrayList<BaseSliderView> mSliderViews;
    private boolean isLoop = true;

    public RecycleAdapter() {
        mSliderViews = new ArrayList<>();
    }

    public int getRealCount() {
        return mSliderViews.size();
    }

    public <T extends BaseSliderView> void addSlider(T slider) {
        slider.setOnImageLoadListener(this);
        mSliderViews.add(slider);
        notifyDataSetChanged();
    }

    /**
     * get really position
     *
     * @param position
     */
    public int getPosition(int position) {
        return isLoop ? position % getRealCount() : position;
    }

    @Override
    public int getCount() {
        return isLoop ? getRealCount() * 100 : getRealCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup container) {
        return mSliderViews.get(getPosition(position)).getView();
    }

    public <T extends BaseSliderView> void removeSlider(T slider) {
        if (mSliderViews.contains(slider)) {
            mSliderViews.remove(slider);
            notifyDataSetChanged();
        }
    }

    public void removeSliderAt(int position) {
        if (mSliderViews.size() < position) {
            mSliderViews.remove(position);
            notifyDataSetChanged();
        }
    }

    public void removeAllSliders() {
        mSliderViews.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onLoadStart(BaseSliderView target) {
    }

    @Override
    public void onLoadComplete(BaseSliderView target) {
    }

    @Override
    public void onLoadFail(BaseSliderView target) {
        if (target.isShowErrorView()) {
            return;
        }
        for (BaseSliderView slider : mSliderViews) {
            if (slider.equals(target)) {
                removeSlider(target);
                break;
            }
        }
    }

    /**
     * @return the is Loop
     */
    public boolean isLoop() {
        return isLoop;
    }

    /**
     * @param isLoop the is InfiniteLoop to set
     */
    public void setLoop(boolean isLoop) {
        this.isLoop = isLoop;
        notifyDataSetChanged();
    }

}
