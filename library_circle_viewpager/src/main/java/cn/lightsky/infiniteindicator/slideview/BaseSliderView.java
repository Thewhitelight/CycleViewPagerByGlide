package cn.lightsky.infiniteindicator.slideview;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.io.File;

/**
 * When you want to make your own slider view, you must extends from this class.
 * BaseSliderView provides some useful methods.
 * if you want to show progressbar, you just need to set a progressbar id as @+id/loading_bar.
 * <p/>
 * Thanks to :  https://github.com/daimajia/AndroidImageSlider
 */
public abstract class BaseSliderView {

    protected Context mContext;

    private Bundle mBundle;

    /**
     * image will be displayed in targetImageView if some error occurs during.
     */
    private int mImageResForError;

    /**
     * image will be displayed in targetImageView if empty url(null or emtpy String).
     */
    private int mImageResForEmpty;

    private String mUrl;
    private File mFile;
    private int mRes;

    protected OnSliderClickListener mOnSliderClickListener;

    private boolean mIsShowErrorView;

    private BitmapLoadCallBack mBitmapLoadListener;

    /**
     * Scale type of the image.
     */
    private ScaleType mScaleType = ScaleType.FitCenter;

    public enum ScaleType {
        CenterCrop, FitCenter
    }

    protected BaseSliderView(Context context) {
        mContext = context;
        mBundle = new Bundle();
    }

    /**
     * the placeholder image when loading image from url or file.
     *
     * @param resId Image resource id
     */
    public BaseSliderView showImageResForEmpty(int resId) {
        mImageResForEmpty = resId;
        return this;
    }

    /**
     * if you set isShowErrorView false, this will set a error placeholder image.
     *
     * @param resId image resource id
     */
    public BaseSliderView showImageResForError(int resId) {
        mImageResForError = resId;
        return this;
    }

    public int getImageResForEmpty() {
        return mImageResForEmpty;
    }

    public int getImageResForError() {
        return mImageResForError;
    }

    /**
     * determine whether remove the image which failed to download or load from file
     *
     * @param disappear
     */
    public BaseSliderView isShowErrorView(boolean disappear) {
        mIsShowErrorView = disappear;
        return this;
    }

    /**
     * set a url as a image that preparing to load
     *
     * @param url
     */
    public BaseSliderView image(String url) {
        if (mFile != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," + "you only have permission to call it once");
        }
        mUrl = url;
        return this;
    }

    /**
     * set a file as a image that will to load
     *
     * @param file
     */
    public BaseSliderView image(File file) {
        if (mUrl != null || mRes != 0) {
            throw new IllegalStateException("Call multi image function," + "you only have permission to call it once");
        }
        mFile = file;
        return this;
    }

    public BaseSliderView image(int res) {
        if (mUrl != null || mFile != null) {
            throw new IllegalStateException("Call multi image function," + "you only have permission to call it once");
        }
        mRes = res;
        return this;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isShowErrorView() {
        return mIsShowErrorView;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * set click listener on a slider image
     *
     * @param l
     */
    public BaseSliderView setOnSliderClickListener(OnSliderClickListener l) {
        mOnSliderClickListener = l;
        return this;
    }

    /**
     * When you want to implement your own slider view, please call this method in the end in `getView()` method
     *
     * @param v               the whole view
     * @param targetImageView where to place image
     */
    protected void bindEventAndShow(final View v, ImageView targetImageView) {
        final BaseSliderView me = this;

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnSliderClickListener != null) {
                    mOnSliderClickListener.onSliderClick(me);
                }
            }
        });

        if (targetImageView == null) {
            return;
        }

        mBitmapLoadListener.onLoadStart(me);

        loadByGlide(v, targetImageView);
    }

    protected void loadByGlide(final View v, ImageView targetImageView) {
        RequestManager rm = Glide.with(mContext.getApplicationContext());
        if (rm == null) {
            return;
        }
        DrawableRequestBuilder builder;
        if (mUrl != null) {
            builder = Glide.with(mContext.getApplicationContext()).load(mUrl);
        } else if (mFile != null) {
            builder = Glide.with(mContext.getApplicationContext()).load(mFile);
        } else if (mRes != 0) {
            builder = Glide.with(mContext.getApplicationContext()).load(mRes);
        } else {
            return;
        }

        if (getImageResForEmpty() != 0) {
            builder.placeholder(getImageResForEmpty());

        }
        if (getImageResForError() != 0) {
            builder.error(getImageResForError());
        }

        switch (mScaleType) {
            case FitCenter:
                builder.fitCenter();
                break;
            case CenterCrop:
                builder.centerCrop();
                break;
        }

        builder.into(targetImageView);
    }

    public BaseSliderView setScaleType(ScaleType type) {
        mScaleType = type;
        return this;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    /**
     * the extended class have to implement getView(), which is called by the adapter,
     * every extended class response to render their own view.
     */
    public abstract View getView();

    /**
     * set a listener to get a message , if load error.
     */
    public void setOnImageLoadListener(BitmapLoadCallBack l) {
        mBitmapLoadListener = l;
    }

    public interface OnSliderClickListener {
        void onSliderClick(BaseSliderView slider);
    }

    /**
     * when you have some extra information, please put it in this bundle.
     */
    public Bundle getBundle() {
        return mBundle;
    }

    public interface BitmapLoadCallBack {
        void onLoadStart(BaseSliderView target);

        void onLoadComplete(BaseSliderView target);

        void onLoadFail(BaseSliderView target);
    }

}
