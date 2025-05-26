package com.example.smartparking;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartparking.Models.ApiClient;
import com.example.smartparking.Models.OtpResponse;
import com.example.smartparking.Models.VerifyOtpRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class provide_emai_code extends AppCompatActivity {
    private EditText otpEditText;
    private Button confirmButton;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Áp dụng theme dialog trước setContentView
        EdgeToEdge.enable(this);
        setContentView(R.layout.provide_mail_code);

        // Căn giữa và đặt kích thước pop-up
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 90% chiều rộng màn hình
            window.setAttributes(params);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        otpEditText = findViewById(R.id.otpEditText);
        confirmButton = findViewById(R.id.confirmButton);

        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy email", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        confirmButton.setOnClickListener(v -> {
            String otp = otpEditText.getText().toString().trim();
            if (otp.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập OTP", Toast.LENGTH_SHORT).show();
            } else {
                verifyOtp(email, otp);
            }
        });
    }

    private void verifyOtp(String email, String otp) {
        OtpApi otpApi = ApiClient.getClient().create(OtpApi.class);
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest(email, otp);

        Call<OtpResponse> call = otpApi.verifyOtp(verifyOtpRequest);
        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OtpResponse otpResponse = response.body();
                    if (otpResponse.isSuccess()) {
                        Toast.makeText(provide_emai_code.this, otpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        // Trả kết quả và đóng pop-up
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(provide_emai_code.this, otpResponse.getError(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(provide_emai_code.this, "Lỗi server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                Toast.makeText(provide_emai_code.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}