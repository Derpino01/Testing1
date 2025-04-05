package com.example.draft1;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity"; // For logging

    // UI Elements
    private EditText editTextUserLogin;
    private EditText editTextPasswordLogin;
    private Button buttonLogin;
    private TextView textViewForgotPassword;
    private TextView textViewSignUpLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Link to your XML layout

        // Initialize UI elements
        initViews();

        // Set up listeners for buttons and text views
        setupListeners();
    }

    private void initViews() {
        editTextUserLogin = findViewById(R.id.editTextUserLogin);
        editTextPasswordLogin = findViewById(R.id.editTextPasswordLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewSignUpLink = findViewById(R.id.textViewSignUpLink);

        // Basic null check (good practice)
        if (editTextUserLogin == null || editTextPasswordLogin == null || buttonLogin == null ||
                textViewForgotPassword == null || textViewSignUpLink == null) {
            Log.e(TAG, "Initialization failed: One or more views not found.");
            // Handle error appropriately, maybe finish activity or show error message
            Toast.makeText(this, "Layout error. Please try again later.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if essential views are missing
        }
    }

    private void setupListeners() {
        // Login Button Click
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        // Forgot Password Click
        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgotPassword();
            }
        });

        // Sign Up Link Click
        textViewSignUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSignUp();
            }
        });
    }

    private void handleLogin() {
        String email = editTextUserLogin.getText().toString().trim();
        String password = editTextPasswordLogin.getText().toString().trim();

        // --- Basic Validation ---
        if (TextUtils.isEmpty(email)) {
            editTextUserLogin.setError("Email is required");
            editTextUserLogin.requestFocus(); // Focus the field
            return; // Stop the login process
        }

        // Simple email pattern check (not foolproof, use Patterns.EMAIL_ADDRESS for better check)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextUserLogin.setError("Enter a valid email address");
            editTextUserLogin.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(password)) {
            editTextPasswordLogin.setError("Password is required");
            editTextPasswordLogin.requestFocus();
            return;
        }
        // --- End Basic Validation ---


        // --- Placeholder for Actual Login Logic ---
        // Here you would typically:
        // 1. Show a progress indicator.
        // 2. Call your backend API or Firebase Auth to verify credentials.
        // 3. Handle success: Navigate to the main part of your app.
        // 4. Handle failure: Show an error message (e.g., "Invalid credentials").
        Log.d(TAG, "Attempting login with Email: " + email); // Log for debugging
        Toast.makeText(this, "Login attempt for: " + email, Toast.LENGTH_SHORT).show();

        // Example: Simulate successful login and navigate to a MainActivity
        // if (loginSuccessful) {
        //    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        //    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        //    startActivity(intent);
        //    finish(); // Prevent user from going back to login screen
        // } else {
        //    Toast.makeText(this, "Login Failed. Check credentials.", Toast.LENGTH_LONG).show();
        // }
        // --- End Placeholder ---
    }

    private void handleForgotPassword() {
        // --- Placeholder for Forgot Password Logic ---
        // Typically navigate to a new screen/dialog to handle password reset
        Log.d(TAG, "Forgot Password Clicked");
        Toast.makeText(this, "Forgot Password Clicked (Implement logic)", Toast.LENGTH_SHORT).show();
        // Example:
        // Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        // startActivity(intent);
        // --- End Placeholder ---
    }

    private void navigateToSignUp() {
        Log.d(TAG, "Navigating to Sign Up Activity");
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        // Optional: finish(); // if you don't want users to go back to login from sign up easily
    }
}