package com.example.mmuentrymobileapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mmuentrymobileapp.fragments.HomeFragment;
import com.example.mmuentrymobileapp.fragments.ListFragment;
import com.example.mmuentrymobileapp.fragments.SettingFragment;
import com.example.mmuentrymobileapp.fragments.AdminListFragment;

public class MyViewPagerAdapter extends FragmentStateAdapter {
    private boolean isAdminView = false;

    public MyViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (isAdminView) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new AdminListFragment();
                case 2:
                    return new SettingFragment();
                default:
                    return new HomeFragment();
            }
        } else {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new ListFragment();
                case 2:
                    return new SettingFragment();
                default:
                    return new HomeFragment();
            }
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    public void setAdminView(boolean isAdminView) {
        this.isAdminView = isAdminView;
    }
}

