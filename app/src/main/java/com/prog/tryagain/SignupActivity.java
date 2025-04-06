package com.prog.tryagain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // For logging
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Volley Imports
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SignupActivity extends AppCompatActivity {

    EditText editTextUsername, editTextPassword, editTextConfirmPassword;
    Button buttonSignup;
    TextView textViewGoToLogin;

    // Define the URL of your PHP script
    // !! REPLACE 'YOUR_COMPUTER_IP' with the actual IP address found in Step 3.4 !!
    private static final String SIGNUP_URL = "http://192.168.254.111/android_api/signup.php";
    private static final String TAG = "SignupActivity"; // For Logcat192.168.0.106

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextUsername = findViewById(R.id.editTextSignupUsername);
        editTextPassword = findViewById(R.id.editTextSignupPassword);
        editTextConfirmPassword = findViewById(R.id.editTextSignupConfirmPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        textViewGoToLogin = findViewById(R.id.textViewGoToLogin);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the validation and registration method
                validateAndRegisterUser();
            }
        });

        textViewGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to Login screen
            }
        });
    }

    private void validateAndRegisterUser() {
        // Clear previous errors
        editTextUsername.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);

        // Get Input
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // --- Local Client-Side Validation ---
        boolean isValid = true;

        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Username is required");
            editTextUsername.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required");
            if (isValid) editTextPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            if (isValid) editTextPassword.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Please confirm your password");
            if (isValid) editTextConfirmPassword.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            if (isValid) editTextConfirmPassword.requestFocus();
            isValid = false;
        }

        // --- If Client-Side Validation Passes, attempt registration ---
        if (isValid) {
            // Disable button to prevent multiple clicks during request
            buttonSignup.setEnabled(false);
            Toast.makeText(this, "Attempting registration...", Toast.LENGTH_SHORT).show();

            registerUser(username, password);
        }
    }

    private void registerUser(final String username, final String password) {
        // Create a new RequestQueue instance (can be singleton for efficiency)
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create StringRequest for POST
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SIGNUP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Register Response: " + response);
                        buttonSignup.setEnabled(true); // Re-enable button

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean error = jsonResponse.getBoolean("error");
                            String message = jsonResponse.getString("message");

                            if (!error) {
                                // Registration successful
                                Toast.makeText(SignupActivity.this, message, Toast.LENGTH_LONG).show();

                                // Optional: Navigate back to Login screen or directly to main app
                                // For now, just finish this activity
                                finish();


                            } else {
                                // Registration failed (e.g., username taken, db error)
                                Toast.makeText(SignupActivity.this, "Registration Failed: " + message, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
                            Toast.makeText(SignupActivity.this, "Error processing server response.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: " + error.toString());
                        buttonSignup.setEnabled(true); // Re-enable button

                        // Handle specific Volley errors if needed (network, timeout, etc.)
                        String errorMessage = "Registration failed. ";
                        if (error.networkResponse == null) {
                            errorMessage += "Cannot connect to the server. Check IP Address and network connection.";
                        } else {
                            errorMessage += "Server error. Please try again later.";
                            // You could potentially read error.networkResponse.data for more details
                        }
                        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                // Add other parameters if your PHP script expects more
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}