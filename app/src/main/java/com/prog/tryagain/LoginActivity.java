package com.prog.tryagain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // Use standard EditText
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText editTextUsername, editTextPassword; // Changed type
    Button buttonLogin;
    TextView textViewGoToSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- Find Views By Id ---
        editTextUsername = findViewById(R.id.editTextLoginUsername); // ID matches XML
        editTextPassword = findViewById(R.id.editTextLoginPassword); // ID matches XML
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewGoToSignup = findViewById(R.id.textViewGoToSignup);

        // --- Set Click Listeners ---
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        textViewGoToSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty()) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            // Consider not focusing password field for UX
            return;
        }

        // --- !! Placeholder for Actual Login Logic !! ---
        // Replace this dummy check with your real authentication
        if (username.equals("testuser") && password.equals("password123")) {
            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AddMedicineActivity.class);
            startActivity(intent);

            // TODO: Navigate to main app screen
            // finish(); // Close login screen
        } else {
            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_LONG).show();
        }
        // --- End Placeholder ---
    }
}
