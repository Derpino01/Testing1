package com.prog.tryagain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class AddMedicineActivity extends AppCompatActivity {

    EditText editTextMedicineName, editTextMedicineAmount;
    Button buttonSaveMedicine, buttonSeeMedicine;

    // !! REPLACE 'YOUR_COMPUTER_IP' with your actual WAMP server IP !!
    private static final String ADD_MED_URL = "http://192.168.0.115/android_api/add_medicine.php";
    private static final String TAG = "AddMedicineActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        editTextMedicineName = findViewById(R.id.editTextMedicineName);
        editTextMedicineAmount = findViewById(R.id.editTextMedicineAmount);
        buttonSaveMedicine = findViewById(R.id.buttonSaveMedicine);
        buttonSeeMedicine = findViewById(R.id.buttonSeeDisplay);


        buttonSeeMedicine.setOnClickListener(v -> {
            Intent intent = new Intent(this, DisplayMedicineActivity.class);
            startActivity(intent);
        });


        buttonSaveMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSaveMedicine();
            }
        });
    }

    private void validateAndSaveMedicine() {
        editTextMedicineName.setError(null); // Clear previous errors

        String name = editTextMedicineName.getText().toString().trim();
        String amount = editTextMedicineAmount.getText().toString().trim(); // Amount is optional

        if (TextUtils.isEmpty(name)) {
            editTextMedicineName.setError("Medicine name is required");
            editTextMedicineName.requestFocus();
            return; // Stop if name is empty
        }

        // If validation passes, proceed to save
        saveMedicineToDatabase(name, amount);
    }

    private void saveMedicineToDatabase(final String name, final String amount) {
        buttonSaveMedicine.setEnabled(false); // Disable button during request
        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_MED_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Add Med Response: " + response);
                        buttonSaveMedicine.setEnabled(true);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean error = jsonResponse.getBoolean("error");
                            String message = jsonResponse.getString("message");

                            Toast.makeText(AddMedicineActivity.this, message, Toast.LENGTH_LONG).show();

                            if (!error) {
                                // Clear fields on success
                                editTextMedicineName.setText("");
                                editTextMedicineAmount.setText("");
                                // Optional: finish(); // or navigate elsewhere
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
                            Toast.makeText(AddMedicineActivity.this, "Error processing server response.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: " + error.toString());
                        buttonSaveMedicine.setEnabled(true);

                        String errorMessage = "Save failed. ";
                        if (error.networkResponse == null) {
                            errorMessage += "Cannot connect to server. Check IP/Network.";
                        } else {
                            // You could try reading error.networkResponse.data here for more details if needed
                            errorMessage += "Server error (" + error.networkResponse.statusCode + "). Please try again later.";
                        }
                        Toast.makeText(AddMedicineActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("medicine_name", name);
                params.put("medicine_amount", amount); // Send amount even if empty, PHP handles it
                return params;
            }
        };

        queue.add(stringRequest);
    }
}