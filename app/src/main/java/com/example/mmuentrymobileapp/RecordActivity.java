package com.example.mmuentrymobileapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class RecordActivity extends AppCompatActivity {

    private ImageView qrCodeImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        qrCodeImageView = findViewById(R.id.qrCodeImageView);

        String token = getIntent().getStringExtra("token");
        generateQRCode(token);
    }

    private void generateQRCode(String token) {
        if (token != null && !token.isEmpty()) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            try {
                BitMatrix bitMatrix = qrCodeWriter.encode(token, BarcodeFormat.QR_CODE, 200, 200);
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? getResources().getColor(R.color.black) : getResources().getColor(R.color.white));
                    }
                }
                qrCodeImageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

}

