package com.prog.tryagain;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog; // For confirmation dialog

import android.content.DialogInterface; // For dialog button clicks
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class ShowMedInfo extends AppCompatActivity {

    // Define constants for Intent extras
    public static final String EXTRA_MED_ID = "com.prog.tryagain.extra.MED_ID";
    public static final String EXTRA_MED_NAME = "com.prog.tryagain.extra.MED_NAME";
    public static final String EXTRA_MED_AMOUNT = "com.prog.tryagain.extra.MED_AMOUNT";

    // !! IMPORTANT: Replace with your actual WAMP server IP and script path !!
    private static final String DELETE_MED_URL = "http://192.168.0.115/android_api/delete_medicine.php";
    private static final String TAG = "ShowMedInfo";

    TextView textViewId;
    TextView textViewName;
    TextView textViewAmount;
    Button buttonDelete;

    private int currentMedId = -1; // Store the ID for delete operation
    private RequestQueue queue; // Volley request queue

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmedinfo);

        textViewId = findViewById(R.id.textViewMedId);
        textViewName = findViewById(R.id.textViewMedName);
        textViewAmount = findViewById(R.id.textViewMedAmount);
        buttonDelete = findViewById(R.id.buttonDeleteMed); // Find the delete button

        queue = Volley.newRequestQueue(this); // Initialize Volley queue

        Intent intent = getIntent();

        if (intent != null && intent.hasExtra(EXTRA_MED_ID) && intent.hasExtra(EXTRA_MED_NAME) && intent.hasExtra(EXTRA_MED_AMOUNT)) {
            currentMedId = intent.getIntExtra(EXTRA_MED_ID, -1); // Store the ID
            String medName = intent.getStringExtra(EXTRA_MED_NAME);
            String medAmount = intent.getStringExtra(EXTRA_MED_AMOUNT);

            if (currentMedId != -1) {
                textViewId.setText(String.valueOf(currentMedId));
            } else {
                textViewId.setText("N/A");
                buttonDelete.setEnabled(false); // Disable delete if ID is invalid
            }

            if (medName != null) {
                textViewName.setText(medName);
                setTitle("Info: " + medName); // Set title
            } else {
                textViewName.setText("N/A");
                setTitle("Medicine Information");
            }

            if (medAmount != null && !medAmount.isEmpty()) {
                textViewAmount.setText(medAmount);
            } else {
                textViewAmount.setText("N/A");
            }

        } else {
            Toast.makeText(this, "Error: Medicine data not received.", Toast.LENGTH_LONG).show();
            textViewId.setText("Error");
            textViewName.setText("Error");
            textViewAmount.setText("Error");
            setTitle("Medicine Information");
            buttonDelete.setEnabled(false); // Disable delete if data is missing
        }

        // --- Set OnClickListener for the Delete Button ---
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog before deleting
                showDeleteConfirmationDialog();
            }
        });
    }

    // --- Confirmation Dialog Method ---
    private void showDeleteConfirmationDialog() {
        if (currentMedId == -1) {
            Toast.makeText(this, "Cannot delete: Invalid Medicine ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete '" + textViewName.getText() + "'?")
                .setIcon(android.R.drawable.ic_dialog_alert) // Optional warning icon
                .setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // User clicked Yes, proceed with deletion
                        deleteMedicineOnServer();
                    }
                })
                .setNegativeButton("No", null) // Do nothing on "No"
                .show();
    }

    // --- Method to perform the delete request ---
    private void deleteMedicineOnServer() {
        // Disable button to prevent multiple clicks while processing
        buttonDelete.setEnabled(false);
        buttonDelete.setText("Deleting...");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_MED_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Delete Response: " + response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean error = jsonResponse.getBoolean("error");

                            if (!error) {
                                // Deletion successful
                                Toast.makeText(ShowMedInfo.this, "Medicine deleted successfully.", Toast.LENGTH_SHORT).show();
                                // TODO: Optionally, use setResult() if started with startActivityForResult
                                // to notify the previous activity (SearchActivity) to refresh its list.
                                finish(); // Close this activity after successful deletion
                            } else {
                                // Deletion failed on server side
                                String message = jsonResponse.optString("message", "Unknown error during delete.");
                                Toast.makeText(ShowMedInfo.this, "Delete failed: " + message, Toast.LENGTH_LONG).show();
                                // Re-enable button on failure
                                buttonDelete.setEnabled(true);
                                buttonDelete.setText("Delete Medicine");
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing Error on Delete Response: " + e.getMessage());
                            Toast.makeText(ShowMedInfo.this, "Error processing delete response.", Toast.LENGTH_LONG).show();
                            buttonDelete.setEnabled(true);
                            buttonDelete.setText("Delete Medicine");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error during Delete: " + error.toString());
                        String errorMessage = "Delete failed. ";
                        if (error.networkResponse == null) {
                            errorMessage += "Cannot connect to server. Check IP/Network.";
                        } else {
                            int statusCode = error.networkResponse.statusCode;
                            Log.e(TAG, "Delete Status Code: " + statusCode);
                            errorMessage += "Server error (" + statusCode + ").";
                        }
                        Toast.makeText(ShowMedInfo.this, errorMessage, Toast.LENGTH_LONG).show();
                        // Re-enable button on error
                        buttonDelete.setEnabled(true);
                        buttonDelete.setText("Delete Medicine");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Send the medicine ID as a POST parameter
                Map<String, String> params = new HashMap<>();
                params.put("med_id", String.valueOf(currentMedId)); // The key "med_id" must match what your PHP script expects
                return params;
            }
        };

        // Add the request to the Volley queue
        queue.add(stringRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel any pending Volley requests if the activity is stopped
        if (queue != null) {
            queue.cancelAll(TAG); // Use the TAG to identify requests for this activity
        }
    }
}