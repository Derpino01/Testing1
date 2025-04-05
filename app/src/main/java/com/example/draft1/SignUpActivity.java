package com.example.draft1;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar; // Import ProgressBar
import android.widget.TextView;
import android.widget.Toast;

// Import Volley classes
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    // --- IMPORTANT: Replace with your computer's local IP address ---
    // Find this using 'ipconfig' (Windows) or 'ifconfig'/'ip addr' (Mac/Linux)
    // Make sure your phone/emulator and computer are on the SAME network.
    // The path should point to where you place your PHP script on your server.
    private static final String SIGNUP_URL = "http://192.168.254.100/signup_script.php";
    // Example: private static final String SIGNUP_URL = "http://192.168.1.10/php_scripts/register.php";

    // UI Elements
    private EditText editTextNameSignUp;
    private EditText editTextPasswordSignUp;
    private EditText editTextConfirmPasswordSignUp;
    private Button buttonSignUp;
    private TextView textViewLoginLink;
    private ProgressBar progressBarSignUp; // Add ProgressBar

    private RequestQueue requestQueue; // Volley Request Queue

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // Link to your XML layout

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);


        initViews();
        setupListeners();
    }


    private void initViews() {
        editTextNameSignUp = findViewById(R.id.editTextNameSignUp);
        editTextPasswordSignUp = findViewById(R.id.editTextPasswordSignUp);
        editTextConfirmPasswordSignUp = findViewById(R.id.editTextConfirmPasswordSignUp);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLoginLink = findViewById(R.id.textViewLoginLink);
        // --- Initialize ProgressBar (Make sure you add it to your XML layout) ---
        progressBarSignUp = findViewById(R.id.progressBarSignUp); // Example ID
        if (progressBarSignUp == null) {
            Log.w(TAG, "ProgressBar with ID 'progressBarSignUp' not found in layout.");
            // Handle this case if ProgressBar is optional or essential
        }


        if (editTextNameSignUp == null || editTextPasswordSignUp == null ||
                editTextConfirmPasswordSignUp == null || buttonSignUp == null || textViewLoginLink == null) {
            Log.e(TAG, "Initialization failed: One or more essential views not found.");
            Toast.makeText(this, "Layout error. Please try again later.", Toast.LENGTH_LONG).show();
            finish(); // Finish if essential views are missing
        }
    }

    // --- Add a ProgressBar to your activity_signup.xml ---
     /*
     Example ProgressBar in activity_signup.xml (place it appropriately):
     <ProgressBar
         android:id="@+id/progressBarSignUp"
         style="?android:attr/progressBarStyle"
         android:layout_width="wrap_content"
         android:layout_height="wrap_content"
         android:visibility="gone"  // Initially hidden
         app:layout_constraintBottom_toBottomOf="parent"
         app:layout_constraintEnd_toEndOf="parent"
         app:layout_constraintStart_toStartOf="parent"
         app:layout_constraintTop_toTopOf="parent" />
     */


    private void setupListeners() {
        buttonSignUp.setOnClickListener(v -> handleSignUp()); // Using Lambda for brevity
        textViewLoginLink.setOnClickListener(v -> navigateToLogin()); // Using Lambda
    }

    private void handleSignUp() {
        final String name = editTextNameSignUp.getText().toString().trim();
        final String password = editTextPasswordSignUp.getText().toString().trim();
        String confirmPassword = editTextConfirmPasswordSignUp.getText().toString().trim();

        // --- Basic Validation ---
        if (!validateInput(name, password, confirmPassword)) {
            return; // Stop if validation fails
        }
        // --- End Basic Validation ---

        // Show progress bar and disable button
        showProgress(true);

        // --- Create Volley Request ---
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGNUP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        showProgress(false); // Hide progress
                        Log.d(TAG, "Server Response: " + response);

                        // --- Handle Server Response ---
                        // Adjust this based on the exact response strings from your PHP script
                        if (response.trim().equalsIgnoreCase("success")) {
                            Toast.makeText(SignUpActivity.this, "Sign Up Successful! Please Log In.", Toast.LENGTH_LONG).show();
                            navigateToLogin(); // Or directly login if desired
                            finish(); // Close the sign up activity
                        } else if (response.trim().equalsIgnoreCase("error: username exists")) {
                            editTextNameSignUp.setError("Username already taken");
                            editTextNameSignUp.requestFocus();
                            Toast.makeText(SignUpActivity.this, "Username already exists.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Handle other specific errors from PHP if needed
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + response, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showProgress(false); // Hide progress
                        Log.e(TAG, "Volley Error: " + error.toString());
                        // More detailed error logging
                        if (error.networkResponse != null) {
                            Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                            try {
                                String body = new String(error.networkResponse.data, "UTF-8");
                                Log.e(TAG,"Response Body: " + body);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error response body", e);
                            }
                        } else {
                            Log.e(TAG, "Error message ni matthew: " + error.getMessage());
                        }
                        Toast.makeText(SignUpActivity.this, "Sign Up Failed. Network error or server issue.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Send parameters to the PHP script
                Map<String, String> params = new HashMap<>();
                params.put("name", name);       // Key must match $_POST key in PHP
                params.put("password", password); // Key must match $_POST key in PHP
                return params;
            }
        };

        // Add the request to the RequestQueue
        requestQueue.add(stringRequest);
    }

    private boolean validateInput(String name, String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            editTextNameSignUp.setError("Name is required");
            editTextNameSignUp.requestFocus();
            return false;
        }
        // Optional: Add more specific username validation (e.g., no spaces, length)

        if (TextUtils.isEmpty(password)) {
            editTextPasswordSignUp.setError("Password is required");
            editTextPasswordSignUp.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPasswordSignUp.setError("Password must be at least 6 characters");
            editTextPasswordSignUp.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPasswordSignUp.setError("Please confirm your password");
            editTextConfirmPasswordSignUp.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPasswordSignUp.setError("Passwords do not match");
            editTextConfirmPasswordSignUp.requestFocus();
            return false;
        }
        return true; // Validation passed
    }


    private void navigateToLogin() {
        Log.d(TAG, "Navigating back to Login Activity");
        finish(); // Close current activity to go back
    }

    private void showProgress(boolean show) {
        if (progressBarSignUp != null) {
            progressBarSignUp.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        buttonSignUp.setEnabled(!show); // Disable button while loading
        editTextNameSignUp.setEnabled(!show);
        editTextPasswordSignUp.setEnabled(!show);
        editTextConfirmPasswordSignUp.setEnabled(!show);
    }
}