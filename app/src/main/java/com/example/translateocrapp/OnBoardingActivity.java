package com.example.translateocrapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

//import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class OnBoardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    TextView btn_next,btn_prev;
    Button startNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        viewPager2 = findViewById(R.id.viewPager2);
        DotsIndicator dots_indicator = findViewById(R.id.dots_indicator);
        btn_prev = findViewById(R.id.btn_prev);
        btn_next = findViewById(R.id.btn_next);
        startNow = findViewById(R.id.startNow);

        int[] layouts = new int[]{
                R.layout.screen_one,
                R.layout.screen_two,
                R.layout.screen_three};

        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter(this, layouts);
        viewPager2.setAdapter(myViewPagerAdapter);
        final int[] currentItem = {viewPager2.getCurrentItem()};
        final int[] totalPages = {viewPager2.getAdapter().getItemCount()};
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                currentItem[0] = viewPager2.getCurrentItem();
                totalPages[0] = viewPager2.getAdapter().getItemCount();


                if (currentItem[0] == 0) {
                    // First page
                    btn_prev.setVisibility(View.GONE);
                    btn_next.setVisibility(View.VISIBLE);
                    startNow.setVisibility(View.GONE);
                } else if (currentItem[0] == totalPages[0] - 1) {
                    // Last page
                    btn_next.setVisibility(View.GONE);
                    btn_prev.setVisibility(View.VISIBLE);
                    startNow.setVisibility(View.VISIBLE);
                } else {
                    // Any middle page
                    startNow.setVisibility(View.GONE);
                    btn_prev.setVisibility(View.VISIBLE);
                    btn_next.setVisibility(View.VISIBLE);
                }

            }
        });

        btn_next.setOnClickListener(v -> {
            if (currentItem[0] < totalPages[0] - 1) {
                viewPager2.setCurrentItem(currentItem[0] + 1, true);
            }
        });

        btn_prev.setOnClickListener(v -> {
            if (currentItem[0] > 0) {
                viewPager2.setCurrentItem(currentItem[0] - 1, true);
            }
        });
        startNow.setOnClickListener(v -> {
            setClickAnimation(v);
//            saveState("is-FirstTime", false);
            startActivity(new Intent(OnBoardingActivity.this, MainActivity.class));
            finish();
        });
        dots_indicator.attachTo(viewPager2);
    }
    private static class MyViewPagerAdapter extends RecyclerView.Adapter<MyViewPagerAdapter.ViewHolder> {

        private LayoutInflater layoutInflater;
        private int[] layouts;

        public MyViewPagerAdapter(Context context, int[] layouts) {
            this.layoutInflater = LayoutInflater.from(context);
            this.layouts = layouts;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = layoutInflater.inflate(layouts[viewType], parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // No need to bind data as layouts are static
        }

        @Override
        public int getItemCount() {
            return layouts.length;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
    public void setClickAnimation(View v) {
        v.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(25)
                .withEndAction(() -> {
                    v.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(25)
                            .start();
                })
                .start();
    }
}