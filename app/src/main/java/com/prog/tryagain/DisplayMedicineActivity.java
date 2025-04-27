package com.prog.tryagain;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DisplayMedicineActivity extends AppCompatActivity {

    ListView listViewMedicines;
    TextView textViewEmptyList;
    ArrayList<String> medicineDisplayList; // List to hold formatted strings for display
    ArrayAdapter<String> adapter;

    // !! REPLACE 'YOUR_COMPUTER_IP' with your actual WAMP server IP !!
    private static final String GET_MEDS_URL = "http://192.168.0.115/android_api/get_medicine.php";
    private static final String TAG = "DisplayMedicineActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_medicine);

        listViewMedicines = findViewById(R.id.listViewMedicines);
        textViewEmptyList = findViewById(R.id.textViewEmptyList);
        medicineDisplayList = new ArrayList<>();

        // Using a simple built-in layout for the list items
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, medicineDisplayList);
        listViewMedicines.setAdapter(adapter);

        // Set up click listener (optional - example to show details)
        listViewMedicines.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = medicineDisplayList.get(position);
            Toast.makeText(DisplayMedicineActivity.this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            // You could parse the string or pass full object data if using custom adapter
        });


        fetchMedicines(); // Load data when activity starts
    }

    // Fetch data also when activity resumes (optional, but good for updates)
    @Override
    protected void onResume() {
        super.onResume();
        // You might want to uncomment this fetchMedicines() call if you expect data
        // to change frequently while the user is in other parts of your app.
        // Be mindful of excessive network calls.
        // fetchMedicines();
    }


    private void fetchMedicines() {
        Toast.makeText(this, "Loading medicines...", Toast.LENGTH_SHORT).show();
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_MEDS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Get Meds Response: " + response);
                        medicineDisplayList.clear(); // Clear previous data

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean error = jsonResponse.getBoolean("error");

                            if (!error) {
                                if (jsonResponse.has("medicines")) {
                                    JSONArray medicinesArray = jsonResponse.getJSONArray("medicines");

                                    if (medicinesArray.length() > 0) {
                                        for (int i = 0; i < medicinesArray.length(); i++) {
                                            JSONObject med = medicinesArray.getJSONObject(i);
                                            String name = med.getString("med_name");
                                            String amount = med.optString("med_amount", ""); // Use optString for optional field

                                            // Format the string for display
                                            String displayString = name + (!amount.isEmpty() ? " (" + amount + ")" : "");
                                            medicineDisplayList.add(displayString);
                                        }
                                        listViewMedicines.setVisibility(View.VISIBLE);
                                        textViewEmptyList.setVisibility(View.GONE);
                                    } else {
                                        // No medicines found in array
                                        listViewMedicines.setVisibility(View.GONE);
                                        textViewEmptyList.setVisibility(View.VISIBLE);
                                        textViewEmptyList.setText("No medicines added yet.");
                                    }
                                } else {
                                    // "medicines" key missing, but no error flag? Treat as empty.
                                    listViewMedicines.setVisibility(View.GONE);
                                    textViewEmptyList.setVisibility(View.VISIBLE);
                                    textViewEmptyList.setText("No medicines found.");
                                }
                            } else {
                                // Error reported by PHP script
                                String message = jsonResponse.getString("message");
                                Toast.makeText(DisplayMedicineActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                                listViewMedicines.setVisibility(View.GONE);
                                textViewEmptyList.setVisibility(View.VISIBLE);
                                textViewEmptyList.setText("Error loading medicines.");
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
                            Toast.makeText(DisplayMedicineActivity.this, "Error processing server response.", Toast.LENGTH_LONG).show();
                            listViewMedicines.setVisibility(View.GONE);
                            textViewEmptyList.setVisibility(View.VISIBLE);
                            textViewEmptyList.setText("Error loading medicines.");
                        }
                        adapter.notifyDataSetChanged(); // Update the ListView UI
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: " + error.toString());
                        String errorMessage = "Load failed. ";
                        if (error.networkResponse == null) {
                            errorMessage += "Cannot connect to server. Check IP/Network.";
                        } else {
                            errorMessage += "Server error (" + error.networkResponse.statusCode + "). Please try again later.";
                        }
                        Toast.makeText(DisplayMedicineActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        listViewMedicines.setVisibility(View.GONE);
                        textViewEmptyList.setVisibility(View.VISIBLE);
                        textViewEmptyList.setText("Failed to load medicines.");
                        adapter.notifyDataSetChanged(); // Clear list if error occurs after data was present
                    }
                });

        queue.add(stringRequest);
    }
}