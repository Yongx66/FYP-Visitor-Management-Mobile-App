package com.example.mmuentrymobileapp.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.mmuentrymobileapp.MainActivity;
import com.example.mmuentrymobileapp.QrActivity;
import com.example.mmuentrymobileapp.R;
import com.example.mmuentrymobileapp.LoginCache;
import com.example.mmuentrymobileapp.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {
    private TextView userEmailTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        userEmailTextView = view.findViewById(R.id.user_email_text_view);

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
                            logoutUser();
                        }
                    }
                });

                return view;
            }
        };

        settingView.setAdapter(arrayAdapter);

        // Fetch and display user email
        fetchUserEmail();

        return view;
    }

    private void fetchUserEmail() {
        String token = LoginCache.getToken(requireContext());

        if (token != null) {
            new GetUserEmailTask().execute(token);
        }
    }

    private class GetUserEmailTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String token = params[0];
            String apiUrl = "http://10.0.2.2:8000/api/user/me";

            try {
                // Make HTTP GET request to the API endpoint
                JSONObject response = HttpUtils.sendHttpGetRequest(apiUrl, token);

                if (response != null && response.getBoolean("status")) {
                    // Extract user email from the response
                    JSONObject message = response.getJSONObject("message");
                    JSONObject userInfo = message.getJSONObject("user_info");
                    return userInfo.getString("email");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String userEmail) {
            if (userEmail != null) {
                userEmailTextView.setText("Hi, " + userEmail);
            } else {
                Toast.makeText(requireContext(), "Failed to fetch user email", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logoutUser() {
        String token = LoginCache.getToken(requireContext());

        if (token != null) {
            new LogoutUserTask().execute(token);
        }
    }

    private class LogoutUserTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String token = params[0];
            String apiUrl = "http://10.0.2.2:8000/api/user/logout";

            try {
                // Make HTTP POST request to the API endpoint
                JSONObject response = HttpUtils.sendHttpPostRequest(apiUrl, token);

                if (response != null && response.getBoolean("status")) {
                    return response.getString("message");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            if (message != null) {
                // Clear the token from LoginCache
                LoginCache.clearToken(requireContext());

                Toast.makeText(requireContext(), "Logout successful", Toast.LENGTH_SHORT).show();

                // Navigate back to MainActivity
                startActivity(new Intent(requireContext(), MainActivity.class));
            } else {
                Toast.makeText(requireContext(), "Failed to logout", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
