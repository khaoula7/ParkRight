package com.charikati.parkright;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import com.charikati.parkright.adapter.ViewPagerAdapter;

import java.util.Objects;

import static com.facebook.FacebookSdk.getApplicationContext;

public class HomeFragment extends Fragment {
    private int dotsCount;
    private ImageView[] dots;

    // Required empty public constructor
    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        ViewPager viewPager = v.findViewById(R.id.viewPager);
        TextView heading = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar_title);
        heading.setText(R.string.app_name);
        LinearLayout sliderDotsPanel = v.findViewById(R.id.SliderDots);
        //Populate ViewPager
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getContext());
        viewPager.setAdapter(viewPagerAdapter);
        //manage dots
        dotsCount = viewPagerAdapter.getCount();
        dots = new ImageView[dotsCount];
        //Add dots to the SliderDots linear layout
        for(int i = 0; i < dotsCount; i++){
            dots[i] = new ImageView(getContext());
            dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(12, 0, 12, 0);
            sliderDotsPanel.addView(dots[i], params);
        }
        dots[0].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                for(int i = 0; i< dotsCount; i++){
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.non_active_dot));
                }
                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.active_dot));

            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        //Click on Report button opens Type activity
        Button startBtn = v.findViewById(R.id.start_button);
        startBtn.setOnClickListener(v1 -> {
            TypeFragment typeFragment = new TypeFragment();
            FragmentManager manager = getFragmentManager();
            assert manager != null;
            manager.beginTransaction().replace(R.id.fragment_container,typeFragment,typeFragment.getTag()).commit();
        });
        return v;
    }
}
