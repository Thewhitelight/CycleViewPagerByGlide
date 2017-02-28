package cn.libery.cycleviewpager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import cn.lightsky.infiniteindicator.InfiniteIndicatorLayout;
import cn.lightsky.infiniteindicator.indicator.CircleIndicator;
import cn.lightsky.infiniteindicator.slideview.BaseSliderView;
import cn.lightsky.infiniteindicator.slideview.DefaultSliderView;

public class MainActivity extends AppCompatActivity {
    List<String> pics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pics = Arrays.asList("http://img.61gequ.com/allimg/2011-4/201142614314278502.jpg", "http://pic14.nipic" +
                        ".com/20110527/2531170_101932834000_2.jpg", "http://pic1.nipic" +
                ".com/2008-12-25/2008122510134038_2.jpg",
                "http://img1.3lian.com/2015/w7/98/d/21.jpg", "sss");
        initCircleViewPager(pics, Gravity.TOP | Gravity.RIGHT);
    }

    private void initCircleViewPager(final List<String> urls, int position) {
        InfiniteIndicatorLayout indicator = (InfiniteIndicatorLayout) findViewById(R.id.indicator_default_circle);
        if (indicator != null) {
            for (int i = 0; i < urls.size(); i++) {
                final String url = urls.get(i);
                final int index = i;
                DefaultSliderView sliderView = new DefaultSliderView(getApplicationContext());
                sliderView.image(url)
                        .setScaleType(BaseSliderView.ScaleType.FitCenter)
                        .showImageResForEmpty(R.color.colorPrimary)
                        .showImageResForError(R.color.colorPrimaryDark)
                        .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                            @Override
                            public void onSliderClick(BaseSliderView slider) {
                                Toast.makeText(getApplicationContext(), urls.get(index), Toast.LENGTH_SHORT).show();
                            }
                        });
                indicator.addSlider(sliderView);
            }
            indicator.startAutoScroll(5000);
            indicator.setIndicatorPosition(position);
        }
    }

}
