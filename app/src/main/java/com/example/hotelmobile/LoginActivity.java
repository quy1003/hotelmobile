package com.example.hotelmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelmobile.databaseHelper.UserDBHelper;
import com.example.hotelmobile.model.User;

public class LoginActivity extends AppCompatActivity {

    EditText edtUsername, edtPassword;
    Button btnLogin;
    UserDBHelper dbHelper;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ ID
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        dbHelper = new UserDBHelper();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Sự kiện click đăng nhập
        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();

            // Kiểm tra thông tin đăng nhập
            dbHelper.checkLogin(username, password, new UserDBHelper.LoginCallback() {
                @Override
                public void onLoginSuccess(User user) {
                    // Nếu đăng nhập thành công, lưu thông tin người dùng vào SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("user_id", user.getId());
                    editor.putString("user_name", user.getName());
                    editor.putString("user_username", user.getUserName());
                    editor.putString("user_role", user.getRole());
                    editor.putString("user_avatar", user.getAvatar());
                    editor.putString("user_email", user.getEmail());
                    editor.putBoolean("is_logged_in", true); // Đánh dấu trạng thái đăng nhập
                    editor.apply();

                    // Thông báo và chuyển màn hình
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(LoginActivity.this, UserBookingsActivity.class));
                    finish(); // Đóng màn hình đăng nhập
                }

                @Override
                public void onLoginFailed(String errorMessage) {
                    // Nếu đăng nhập thất bại
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
