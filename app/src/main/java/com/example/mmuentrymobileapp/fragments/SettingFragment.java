package com.example.mmuentrymobileapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.example.mmuentrymobileapp.MainActivity;
import com.example.mmuentrymobileapp.QrActivity;
import com.example.mmuentrymobileapp.R;
import com.example.mmuentrymobileapp.TestActivity;

import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        ListView settingView = view.findViewById(R.id.settingview);

        List<String> list = new ArrayList<>();
        list.add("Scan Qr Code");
        list.add("Logout");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (position == 0) {
                            // clicked scan qr
                            startActivity(new Intent(requireContext(), QrActivity.class));
                        } else if (position == 1) {
                            // clicked Logout
                            startActivity(new Intent(requireContext(), MainActivity.class));
                        }
                    }
                });

                return view;
            }
        };

        settingView.setAdapter(arrayAdapter);

        return view;
    }
}
