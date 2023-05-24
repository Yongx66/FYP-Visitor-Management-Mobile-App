package com.example.mmuentrymobileapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mmuentrymobileapp.SignupActivity;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.SharedPreferences;


public class MainActivity extends AppCompatActivity {

    private static final int SIGNUP_REQUEST_CODE = 1;

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        MaterialButton signinbtn = findViewById(R.id.signinbtn);
        MaterialButton signupbtn = findViewById(R.id.signupbtn);

        signinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVisitorActivity();
            }
        });

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSignupActivity();
            }
        });
    }

    public void openVisitorActivity() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
        } else {
            loginUser(username, password);
        }
    }

    private void loginUser(String username, String password) {
        String apiUrl = "http://10.0.2.2:8000/api/user/login";

        try {
            JSONObject requestData = new JSONObject();
            requestData.put("email", username);
            requestData.put("password", password);

            new LoginUserTask(apiUrl, requestData).execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void openSignupActivity() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivityForResult(intent, SIGNUP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGNUP_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String username = data.getStringExtra("username");
                String password = data.getStringExtra("password");
                loginUser(username, password);
            }
        }
    }

    private class LoginUserTask extends AsyncTask<Void, Void, String> {
        private final String apiUrl;
        private final JSONObject requestData;

        public LoginUserTask(String apiUrl, JSONObject requestData) {
            this.apiUrl = apiUrl;
            this.requestData = requestData;
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String response = null;

            try {
                URL url = new URL(apiUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);

                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(requestData.toString().getBytes("UTF-8"));
                outputStream.close();

                // Read the response
                InputStream inputStream = urlConnection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                response = stringBuilder.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            if (response != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("status");
                    if (success) {
                        String token = jsonResponse.getString("message");
                        SharedPreferences sharedPreferences = getSharedPreferences("LoginCache", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.apply();
                        // Handle successful login
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, VisitorActivity.class);
                        startActivity(intent); // Navigate to VisitorActivity
                        finish(); // Finish the current activity to prevent going back to login screen
                    } else {
                        String errorMessage = jsonResponse.getString("message");
                        Toast.makeText(MainActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        // Handle login failure
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(MainActivity.this, "Wrong Email or Password", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
