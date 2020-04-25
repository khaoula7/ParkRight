package com.charikati.parkright.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.charikati.parkright.R;

public class ViewPagerAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private Integer [] images = {R.drawable.image_1,R.drawable.image_4,R.drawable.image_3};
    private Integer[] titles = {R.string.title_1, R.string.title_2, R.string.title_3};
    private Integer[] subtitles = {R.string.subtitle_1, R.string.subtitle_2, R.string.subtitle_3};

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.viewpager_layout, null);
        ImageView imageView = view.findViewById(R.id.imageView);
        imageView.setImageResource(images[position]);
        TextView title = view.findViewById(R.id.title_txt);
        title.setText(titles[position]);
        TextView subtitle = view.findViewById(R.id.subtitle_txt);
        subtitle.setText(subtitles[position]);
        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}
