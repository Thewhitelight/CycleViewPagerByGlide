package cn.libery.cycleviewpager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import cn.lightsky.infiniteindicator.InfiniteIndicatorLayout;
import cn.lightsky.infiniteindicator.slideview.BaseSliderView;
import cn.lightsky.infiniteindicator.slideview.DefaultSliderView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<String> pics = Arrays.asList("http://cjwdata.superwan.cn/AABWVCcAmQc6Q9Ew.jpg", "http://cjwdata.superwan" +
                ".cn/AABW%2FOSo9oVlGXKm.jpg", "http://cjwdata.superwan.cn/AABXSApBcuUhkEzX.jpg", "http://cjwdata" +
                ".superwan.cn/AABWrwl3rfMfdu0X.jpg", "sss");
        initCircleViewPager(pics);
    }

    private void initCircleViewPager(final List<String> urls) {
        InfiniteIndicatorLayout indicator = (InfiniteIndicatorLayout) findViewById(R.id.indicator_default_circle);
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
            indicator.startAutoScroll(5000);
            indicator.addSlider(sliderView);
        }
        indicator.setIndicatorPosition();
    }
}
