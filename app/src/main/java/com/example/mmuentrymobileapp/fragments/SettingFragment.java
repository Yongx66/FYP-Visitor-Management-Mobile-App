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
import com.example.mmuentrymobileapp.RegadminActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingFragment extends Fragment {
    private TextView userEmailTextView;
    private String userRole;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        userEmailTextView = view.findViewById(R.id.user_email_text_view);

        // Fetch and display user email and role
        fetchUserEmailAndRole();

        return view;
    }

    private void fetchUserEmailAndRole() {
        String token = LoginCache.getToken(requireContext());

        if (token != null) {
            new GetUserEmailAndRoleTask().execute(token);
        }
    }

    private class GetUserEmailAndRoleTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String token = params[0];
            String apiUrl = "http://10.0.2.2:8000/api/user/me";

            try {
                // Make HTTP GET request to the API endpoint
                JSONObject response = HttpUtils.sendHttpGetRequest(apiUrl, token);

                if (response != null && response.getBoolean("status")) {
                    // Extract user email and role from the response
                    JSONObject message = response.getJSONObject("message");
                    JSONObject userInfo = message.getJSONObject("user_info");
                    userRole = message.getString("user_role_info"); // Store the user role
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
                setupSettingView(); // Call this method after fetching user email and role
            } else {
                Toast.makeText(requireContext(), "Failed to fetch user email", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupSettingView() {
        ListView settingView = getView().findViewById(R.id.settingview);

        List<String> list = new ArrayList<>();
        if ("ADMIN".equals(userRole) || "SUPERADMIN".equals(userRole)) {
            list.add("Scan Qr Code");
        }
        if ("SUPERADMIN".equals(userRole)) {
            list.add("Register admin");
        }
        list.add("Logout");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView textView = (TextView) v;
                        String clickedItemText = textView.getText().toString();

                        if ("Scan Qr Code".equals(clickedItemText)) {
                            // clicked scan qr
                            startActivity(new Intent(requireContext(), QrActivity.class));
                        } else if ("Register admin".equals(clickedItemText)) {
                            // clicked Register admin
                            startActivity(new Intent(requireContext(), RegadminActivity.class));
                        } else if ("Logout".equals(clickedItemText)) {
                            // clicked Logout
                            logoutUser();
                        }
                    }
                });

                return view;
            }
        };

        settingView.setAdapter(arrayAdapter);
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
                JSONObject response = HttpUtils.sendHttpPostRequest(apiUrl, token, null);

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
                // Navigate back to MainActivity and clear activity stack
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else {
                Toast.makeText(requireContext(), "Failed to logout", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
