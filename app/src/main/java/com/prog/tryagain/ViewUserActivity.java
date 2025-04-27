package com.prog.tryagain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class ViewUserActivity extends AppCompatActivity {

    private TextView textViewUsernameValue;
    private TextView textViewPasswordValue;
    private ProgressBar progressBar;

    // IMPORTANT: Replace with your WAMP server's IP address and the path to your PHP script
    // Use 10.0.2.2 for localhost when running from Android Emulator
    // Use your computer's network IP if testing on a real device on the same Wi-Fi
    private static final String FETCH_URL = "http://192.168.0.115/android_api/get_user_data.php";

    private String usernameToFetch; // Username passed from previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_view);

        textViewUsernameValue = findViewById(R.id.textViewUsernameValue);
        textViewPasswordValue = findViewById(R.id.textViewPasswordValue); // Will show '**********'
        progressBar = findViewById(R.id.progressBar);

        // --- Get username from the Intent that started this activity ---
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USERNAME_EXTRA")) { // Use a constant for the key
            usernameToFetch = intent.getStringExtra("USERNAME_EXTRA");
            if (usernameToFetch != null && !usernameToFetch.isEmpty()) {
                fetchUserData(usernameToFetch);
            } else {
                Toast.makeText(this, "Username not provided.", Toast.LENGTH_LONG).show();
                finish(); // Close activity if no username
            }
        } else {
            Toast.makeText(this, "Could not get user information.", Toast.LENGTH_LONG).show();
            finish(); // Close activity if intent is missing data
        }
    }

    private void fetchUserData(final String username) {
        progressBar.setVisibility(View.VISIBLE);
        textViewUsernameValue.setText("Loading...");
        // Keep password as '**********'

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, FETCH_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("ViewUserActivity", "Response: " + response); // Log server response

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean error = jsonResponse.getBoolean("error");
                            String message = jsonResponse.getString("message");

                            if (!error) {
                                JSONObject userObject = jsonResponse.getJSONObject("user");
                                String fetchedUsername = userObject.getString("username");
                                // String passwordHash = userObject.getString("password_hash"); // We get the hash
                                // String createdAt = userObject.getString("created_at");

                                textViewUsernameValue.setText(fetchedUsername);
                                // !!! DO NOT DISPLAY HASH OR ORIGINAL PASSWORD !!!
                                textViewPasswordValue.setText("**********"); // Keep placeholder

                                Toast.makeText(ViewUserActivity.this, message, Toast.LENGTH_SHORT).show();

                            } else {
                                // Server returned an error (e.g., user not found)
                                Toast.makeText(ViewUserActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                textViewUsernameValue.setText("Not found");
                            }

                        } catch (JSONException e) {
                            progressBar.setVisibility(View.GONE);
                            Log.e("ViewUserActivity", "JSON Parsing error: " + e.getMessage());
                            Toast.makeText(ViewUserActivity.this, "Error parsing server data.", Toast.LENGTH_LONG).show();
                            textViewUsernameValue.setText("Error");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("ViewUserActivity", "Volley error: " + error.toString());
                        Toast.makeText(ViewUserActivity.this, "Network Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        textViewUsernameValue.setText("Network Error");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Parameters sent to the PHP script
                Map<String, String> params = new HashMap<>();
                params.put("username", username); // Send the username to fetch
                return params;
            }
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}