package cn.lightsky.infiniteindicator;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

import cn.lightsky.infiniteindicator.indicator.CircleIndicator;
import cn.lightsky.infiniteindicator.indicator.PageIndicator;
import cn.lightsky.infiniteindicator.indicator.RecycleAdapter;
import cn.lightsky.infiniteindicator.jakewharton.salvage.RecyclingPagerAdapter;
import cn.lightsky.infiniteindicator.slideview.BaseSliderView;

/**
 * Created by lightSky on 2014/12/22.
 * Thanks to: https://github.com/Trinea/android-auto-scroll-view-pager
 */
public class InfiniteIndicatorLayout extends RelativeLayout implements RecyclingPagerAdapter.DataChangeListener {

    private final ScrollHandler handler;
    private PageIndicator mIndicator;
    private ViewPager mViewPager;
    private RecycleAdapter mRecycleAdapter;

    public static final int DEFAULT_INTERVAL = 2000;
    /**
     * do nothing when sliding at the last or first item *
     */
    public static final int SLIDE_BORDER_MODE_NONE = 0;
    /**
     * cycle when sliding at the last or first item *
     */
    public static final int SLIDE_BORDER_MODE_CYCLE = 1;
    /**
     * deliver event to parent when sliding at the last or first item *
     */
    public static final int SLIDE_BORDER_MODE_TO_PARENT = 2;

    /**
     * auto scroll time in milliseconds, default is {@link #DEFAULT_INTERVAL} *
     */
    private long interval = DEFAULT_INTERVAL;
    /**
     * whether automatic cycle when auto scroll reaching the last or first item, default is true *
     */
    private boolean isInfinite = true;
    /**
     * whether stop auto scroll when touching, default is true *
     */
    private boolean isStopScrollWhenTouch = true;
    /**
     * how to process when sliding at the last or first item, default is {@link #SLIDE_BORDER_MODE_NONE} *
     */
    private int slideBorderMode = SLIDE_BORDER_MODE_NONE;

    public static final int MSG_WHAT = 0;
    private boolean isAutoScroll = false;
    private boolean isStopByTouch = false;
    private float downX = 0f;

    public InfiniteIndicatorLayout(Context context) {
        this(context, null);
    }

    public InfiniteIndicatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfiniteIndicatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_default_indicator, this, true);

        handler = new ScrollHandler(this);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mRecycleAdapter = new RecycleAdapter();
        mRecycleAdapter.setDataChangeListener(this);
        mViewPager.setAdapter(mRecycleAdapter);
    }

    public <T extends BaseSliderView> void addSlider(T imageContent) {
        mRecycleAdapter.addSlider(imageContent);
    }

    public void removeSlider() {
        mRecycleAdapter.removeAllSliders();
    }

    public void removeSliderAt(int position) {
        mRecycleAdapter.removeSliderAt(position);
    }

    /**
     * according page count and is loop decide the first page to display
     */
    public void initFirstPage() {
        if (isInfinite && mRecycleAdapter.getRealCount() > 1) {
            mViewPager.setCurrentItem(mRecycleAdapter.getRealCount() * 50);
        } else {
            setInfinite(false);
            mViewPager.setCurrentItem(0);
        }
    }

    /**
     * start auto scroll, first scroll delay time is {@link #getInterval()}
     */
    public void startAutoScroll() {
        if (mRecycleAdapter.getRealCount() > 1) {
            isAutoScroll = true;
            sendScrollMessage(interval);
        }
    }

    /**
     * start auto scroll
     *
     * @param delayTimeInMills first scroll delay time
     */
    public void startAutoScroll(int delayTimeInMills) {
        if (mRecycleAdapter.getRealCount() > 1) {
            isAutoScroll = true;
            sendScrollMessage(delayTimeInMills);
        }
    }

    /**
     * stop auto scroll
     */
    public void stopAutoScroll() {
        isAutoScroll = false;
        handler.removeMessages(MSG_WHAT);
    }

    private void sendScrollMessage(long delayTimeInMills) {
        /** remove messages before, keeps one message is running at most **/
        handler.removeMessages(MSG_WHAT);
        handler.sendEmptyMessageDelayed(MSG_WHAT, delayTimeInMills);
    }

    private void sendScrollMessage() {
        /** remove messages before, keeps one message is running at most **/
        sendScrollMessage(interval);
    }

    /**
     * scroll only once
     */
    public void scrollOnce() {
        PagerAdapter adapter = mViewPager.getAdapter();
        int currentItem = mViewPager.getCurrentItem();
        int totalCount;
        if (adapter == null || (totalCount = adapter.getCount()) <= 1) {
            return;
        }
        int nextItem = ++currentItem;
        if (nextItem < 0) {
            if (isInfinite) {
                mViewPager.setCurrentItem(totalCount - 1);
            }
        } else if (nextItem == totalCount) {
            if (isInfinite) {
                mViewPager.setCurrentItem(0);
            }
        } else {
            mViewPager.setCurrentItem(nextItem, true);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (isStopScrollWhenTouch) {
            if ((action == MotionEvent.ACTION_DOWN) && isAutoScroll) {
                isStopByTouch = true;
                stopAutoScroll();
            } else if (ev.getAction() == MotionEvent.ACTION_UP && isStopByTouch) {
                startAutoScroll();
            }
        }

        if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT || slideBorderMode == SLIDE_BORDER_MODE_CYCLE) {
            float touchX = ev.getX();
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                downX = touchX;
            }
            int currentItem = mViewPager.getCurrentItem();
            PagerAdapter adapter = mViewPager.getAdapter();
            int pageCount = adapter == null ? 0 : adapter.getCount();
            /**
             * current index is first one and slide to right or current index is last one and slide to left.<br/>
             * if slide border mode is to parent, then requestDisallowInterceptTouchEvent false.<br/>
             * else scroll to last one when current item is first one, scroll to first one when current item is last
             * one.
             */
            if ((currentItem == 0 && downX <= touchX) || (currentItem == pageCount - 1 && downX >= touchX)) {
                if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    if (pageCount > 1) {
                        mViewPager.setCurrentItem(pageCount - currentItem - 1);
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.dispatchTouchEvent(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void notifyDataChange() {
        if (mIndicator != null) {
            mIndicator.notifyDataSetChanged();
        }
    }

    public static class ScrollHandler extends Handler {
        public WeakReference<InfiniteIndicatorLayout> mLeakActivityRef;

        public ScrollHandler(InfiniteIndicatorLayout infiniteIndicatorLayout) {
            mLeakActivityRef = new WeakReference<>(infiniteIndicatorLayout);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            InfiniteIndicatorLayout infiniteIndicatorLayout = mLeakActivityRef.get();
            if (infiniteIndicatorLayout != null) {
                switch (msg.what) {
                    case MSG_WHAT:
                        infiniteIndicatorLayout.scrollOnce();
                        infiniteIndicatorLayout.sendScrollMessage();
                    default:
                        break;
                }
            }
        }
    }

    /**
     * get auto scroll interval time in milliseconds, default is {@link #DEFAULT_INTERVAL}
     *
     * @return the interval
     */
    public long getInterval() {
        return interval;
    }

    /**
     * set auto scroll interval time in milliseconds, default is {@link #DEFAULT_INTERVAL}
     *
     * @param interval the interval to set
     */
    public void setInterval(long interval) {
        this.interval = interval;
    }

    /**
     * whether is infinite loop of viewPager , default is true
     *
     * @return the isInfinite
     */
    public boolean isInfinite() {
        return isInfinite;
    }

    /**
     * set whether is loop when reaching the last or first item, default is true
     *
     * @param isInfinite the isInfinite
     */
    public void setInfinite(boolean isInfinite) {
        this.isInfinite = isInfinite;
        mRecycleAdapter.setLoop(isInfinite);
    }

    /**
     * whether stop auto scroll when touching, default is true
     *
     * @return the stopScroll When Touch
     */
    public boolean isStopScrollWhenTouch() {
        return isStopScrollWhenTouch;
    }

    /**
     * set whether stop auto scroll when touching, default is true
     *
     * @param stopScrollWhenTouch
     */
    public void setStopScrollWhenTouch(boolean stopScrollWhenTouch) {
        this.isStopScrollWhenTouch = stopScrollWhenTouch;
    }

    /**
     * get how to process when sliding at the last or first item
     *
     * @return the slideBorderMode {@link #SLIDE_BORDER_MODE_NONE}, {@link #SLIDE_BORDER_MODE_TO_PARENT},
     * {@link #SLIDE_BORDER_MODE_CYCLE}, default is {@link #SLIDE_BORDER_MODE_NONE}
     */
    public int getSlideBorderMode() {
        return slideBorderMode;
    }

    /**
     * set how to process when sliding at the last or first item
     * will be explore in future version
     *
     * @param slideBorderMode {@link #SLIDE_BORDER_MODE_NONE}, {@link #SLIDE_BORDER_MODE_TO_PARENT},
     *                        {@link #SLIDE_BORDER_MODE_CYCLE}, default is {@link #SLIDE_BORDER_MODE_NONE}
     */
    private void setSlideBorderMode(int slideBorderMode) {
        this.slideBorderMode = slideBorderMode;
    }

    public PageIndicator getPagerIndicator() {
        return mIndicator;
    }

    public enum IndicatorPosition {

        Right_Bottom("Right_Bottom", R.id.default_center_bottom_indicator);

        private final String name;
        private final int id;

        IndicatorPosition(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String toString() {
            return name;
        }

        public int getResourceId() {
            return id;
        }
    }

    CircleIndicator pagerIndicator;

    public void setIndicatorPosition() {
        if (pagerIndicator == null) {
            pagerIndicator = (CircleIndicator) findViewById(IndicatorPosition.Right_Bottom.getResourceId());
            setCustomIndicator(pagerIndicator);
        }
    }

    public void setIndicatorPosition(int position) {
        if (pagerIndicator == null) {
            pagerIndicator = (CircleIndicator) findViewById(IndicatorPosition.Right_Bottom.getResourceId());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = position;
            pagerIndicator.setLayoutParams(params);
            setCustomIndicator(pagerIndicator);
        }
    }

    public void setCustomIndicator(PageIndicator indicator) {
        initFirstPage();
        mIndicator = indicator;
        mIndicator.setViewPager(mViewPager);
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        if (onPageChangeListener != null) {
            mIndicator.setOnPageChangeListener(onPageChangeListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
    }

}
