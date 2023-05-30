package com.example.mmuentrymobileapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class VisitorActivity extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    MyViewPagerAdapter myViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor);

        tabLayout = findViewById(R.id.tab_layout);
        viewPager2 = findViewById(R.id.view_pager);
        myViewPagerAdapter = new MyViewPagerAdapter(this);
        viewPager2.setAdapter(myViewPagerAdapter);

        // Fetch user information and role
        String token = LoginCache.getToken(this);
        if (token != null) {
            String apiUrl = "http://10.0.2.2:8000/api/user/me";
            new FetchUserInfoTask().execute(apiUrl, token);
        } else {
            // Handle case when token is null
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
    }

    private class FetchUserInfoTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            String apiUrl = params[0];
            String token = params[1];

            try {
                return HttpUtils.sendHttpGetRequest(apiUrl, token);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            if (response != null) {
                try {
                    String userRole = response.getJSONObject("message").getString("user_role_info");
                    updateViewPager(userRole);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateViewPager(String userRole) {
        if (userRole.equals("ADMIN") || userRole.equals("SUPERADMIN")) {
            myViewPagerAdapter.setAdminView(true);
        } else {
            myViewPagerAdapter.setAdminView(false);
        }
        myViewPagerAdapter.notifyDataSetChanged();
    }
}
