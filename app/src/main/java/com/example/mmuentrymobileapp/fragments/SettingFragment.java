package com.example.mmuentrymobileapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.mmuentrymobileapp.QrActivity;
import com.example.mmuentrymobileapp.R;
import com.example.mmuentrymobileapp.TestActivity;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        ListView settingView = view.findViewById(R.id.settingview);

        List<String> list = new ArrayList<>();
        list.add("Scan Qr Code");
        list.add("Logout");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, list);
        settingView.setAdapter(arrayAdapter);

        settingView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // clicked scan qr
                    startActivity(new Intent(requireContext(), QrActivity.class));
                } else {
                    // Handle other item clicks
                }
            }
        });

        return view;
    }
}