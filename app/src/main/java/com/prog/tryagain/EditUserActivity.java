package com.prog.tryagain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Objects;

public class EditUserActivity extends AppCompatActivity {

    private TextView textViewCurrentUsername;
    private EditText editTextNewUsername;
    private EditText editTextCurrentPassword;
    private EditText editTextNewPassword;
    private EditText editTextConfirmPassword;
    private Button buttonUpdate;
    private ProgressBar progressBarUpdate;

    // IMPORTANT: Replace with your WAMP server's IP address and the path to your PHP script
    private static final String UPDATE_URL = "http://192.168.0.115/android_api/update_user.php";

    private String currentUsername; // Username passed from previous activity

    // Key for passing username in Intent
    public static final String EXTRA_USERNAME = "com.prog.tryagain.EXTRA_USERNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        // Bind views
        textViewCurrentUsername = findViewById(R.id.textViewCurrentUsername);
        editTextNewUsername = findViewById(R.id.editTextNewUsername);
        editTextCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        progressBarUpdate = findViewById(R.id.progressBarUpdate);

        // Get username from Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_USERNAME)) {
            currentUsername = intent.getStringExtra(EXTRA_USERNAME);
            if (currentUsername != null && !currentUsername.isEmpty()) {
                textViewCurrentUsername.setText(currentUsername);
            } else {
                handleErrorAndFinish("Invalid user data received.");
            }
        } else {
            handleErrorAndFinish("Could not get user information to edit.");
        }

        // Set button click listener
        buttonUpdate.setOnClickListener(v -> attemptUpdate());
    }

    private void attemptUpdate() {
        // Reset errors (if you were setting them)
        // editTextNewUsername.setError(null); // Example
        // editTextCurrentPassword.setError(null);
        // ... etc

        // Get values from EditTexts
        String newUsername = editTextNewUsername.getText().toString().trim();
        String currentPassword = editTextCurrentPassword.getText().toString().trim();
        String newPassword = editTextNewPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // --- Client-Side Validation ---
        if (TextUtils.isEmpty(currentPassword)) {
            editTextCurrentPassword.setError("Current password is required");
            editTextCurrentPassword.requestFocus();
            Toast.makeText(this, "Current password is required to save changes.", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if new password fields match (only if a new password was entered)
        if (!TextUtils.isEmpty(newPassword)) {
            if (!newPassword.equals(confirmPassword)) {
                editTextConfirmPassword.setError("Passwords do not match");
                editTextConfirmPassword.requestFocus();
                Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_LONG).show();
                return;
            }
            // Optional: Add password complexity checks here if desired
            if (newPassword.length() < 6) { // Example minimum length
                editTextNewPassword.setError("Password must be at least 6 characters");
                editTextNewPassword.requestFocus();
                Toast.makeText(this,"Password must be at least 6 characters", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Check if any actual changes are being submitted
        if (TextUtils.isEmpty(newUsername) && TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Please enter a new username or new password to update.", Toast.LENGTH_LONG).show();
            return;
        }
        // If new username is same as old and no new password, no change needed
        if (Objects.equals(newUsername, currentUsername) && TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "New username is the same as current, and no new password entered.", Toast.LENGTH_LONG).show();
            return;
        }


        // --- Passed Validation - Proceed with Network Request ---
        updateUserDetails(currentUsername, currentPassword, newUsername, newPassword);
    }

    private void updateUserDetails(final String currentUsername, final String currentPassword,
                                   final String newUsername, final String newPassword) {

        progressBarUpdate.setVisibility(View.VISIBLE);
        buttonUpdate.setEnabled(false); // Disable button during request

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBarUpdate.setVisibility(View.GONE);
                        buttonUpdate.setEnabled(true);
                        Log.d("EditUserActivity", "Response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean error = jsonResponse.getBoolean("error");
                            String message = jsonResponse.getString("message");

                            Toast.makeText(EditUserActivity.this, message, Toast.LENGTH_LONG).show();

                            if (!error) {
                                // Update successful!
                                // Check if username was changed and update UI/pass back result
                                if (jsonResponse.has("new_username")) {
                                    String updatedUsername = jsonResponse.getString("new_username");
                                    textViewCurrentUsername.setText(updatedUsername); // Update display
                                    EditUserActivity.this.currentUsername = updatedUsername; // Update instance variable
                                    editTextNewUsername.setText(""); // Clear input field

                                    // You might want to pass the new username back to the previous activity
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("UPDATED_USERNAME", updatedUsername);
                                    setResult(RESULT_OK, resultIntent);
                                }
                                // Clear password fields after successful update
                                editTextCurrentPassword.setText("");
                                editTextNewPassword.setText("");
                                editTextConfirmPassword.setText("");

                                // Optional: Finish activity after success
                                // finish();
                            } else {
                                // Handle specific errors if needed (e.g., focus field based on message)
                                if (message.toLowerCase().contains("incorrect current password")) {
                                    editTextCurrentPassword.setError("Incorrect");
                                    editTextCurrentPassword.requestFocus();
                                } else if (message.toLowerCase().contains("username is already taken")) {
                                    editTextNewUsername.setError("Username taken");
                                    editTextNewUsername.requestFocus();
                                }
                            }

                        } catch (JSONException e) {
                            Log.e("EditUserActivity", "JSON Parsing error: " + e.getMessage());
                            Toast.makeText(EditUserActivity.this, "Error parsing server response.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBarUpdate.setVisibility(View.GONE);
                        buttonUpdate.setEnabled(true);
                        Log.e("EditUserActivity", "Volley error: " + error.toString());
                        Toast.makeText(EditUserActivity.this, "Network Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("current_username", currentUsername);
                params.put("current_password", currentPassword); // Send current password for verification
                // Only send new values if they are not empty
                if (!TextUtils.isEmpty(newUsername)) {
                    params.put("new_username", newUsername);
                }
                if (!TextUtils.isEmpty(newPassword)) {
                    params.put("new_password", newPassword); // Send new password to be hashed
                }
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void handleErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish(); // Close the activity if essential data is missing
    }
}