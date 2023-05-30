package com.example.mmuentrymobileapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.mmuentrymobileapp.HttpUtils;
import com.example.mmuentrymobileapp.LoginCache;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class QrActivity extends AppCompatActivity {

    private String baseUrl = "http://10.0.2.2:8000/api/";
    private String verifyEndpoint = "visitor/verify/record/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        scanCode();
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String token = result.getContents();
            String authToken = LoginCache.getToken(QrActivity.this);

            if (authToken != null) {
                String apiUrl = baseUrl + verifyEndpoint + token;
                verifyVisitorRecord(apiUrl, authToken);
            } else {
                showErrorMessage("Authentication token is null");
            }
        } else {
            onBackPressed(); // No QR code found or scanning canceled, go back
        }
    });

    private void verifyVisitorRecord(String apiUrl, String authToken) {
        new Thread(() -> {
            try {
                JSONObject response = HttpUtils.sendHttpGetRequest(apiUrl, authToken);
                if (response != null) {
                    boolean status = response.getBoolean("status");
                    int errCode = response.getInt("errCode");

                    if (status && errCode == 200) {
                        if (response.has("message")) {
                            JSONArray messageArray = response.getJSONArray("message");
                            if (messageArray.length() > 0) {
                                JSONObject recordObject = messageArray.getJSONObject(0);
                                String dateOfVisit = recordObject.optString("date_of_visit", "");
                                String reasonOfVisiting = recordObject.optString("reason_of_visiting", "");

                                if (!dateOfVisit.isEmpty() && !reasonOfVisiting.isEmpty()) {
                                    showVerificationDialog(dateOfVisit, reasonOfVisiting);
                                    return; // Exit the method after showing the verification dialog
                                }
                            }
                        }
                    }
                }

                showErrorMessage("Invalid QR Code!");

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                showErrorMessage("An error occurred: " + e.getMessage());
            }
        }).start();
    }

    private void showVerificationDialog(String dateOfVisit, String reasonOfVisiting) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(QrActivity.this);
            builder.setTitle("Visitor Verification");
            builder.setMessage("Date of Visit: " + dateOfVisit + "\nReason of Visiting: " + reasonOfVisiting);
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                finish(); // Restart the scanning process
            });
            builder.show();
        });
    }

    private void showErrorMessage(String errorMessage) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(QrActivity.this);
            builder.setTitle("Visitor Verification");
            builder.setMessage(errorMessage);
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                finish(); // Restart the scanning process
            });
            builder.show();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Perform any additional operations or navigation if needed
    }
}
