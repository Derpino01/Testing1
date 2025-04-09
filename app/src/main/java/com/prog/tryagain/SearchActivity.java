package com.prog.tryagain;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo; // For keyboard search action
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Locale; // For lowercase conversion

public class SearchActivity extends AppCompatActivity {

    EditText editTextSearch;
    Button buttonSearch;
    ListView listViewSearchResults;
    TextView textViewEmptySearch;

    // List to hold all medicine objects fetched from DB
    ArrayList<Medicine> allMedicinesList;
    // List to hold strings formatted for display in ListView
    ArrayList<String> displayList;
    ArrayAdapter<String> adapter;

    // Use the same URL as DisplayMedicineActivity to get all medicines
    // !! REPLACE 'YOUR_COMPUTER_IP' with your actual WAMP server IP !!
    private static final String GET_MEDS_URL = "http://192.168.254.111/android_api/get_medicine.php";
    private static final String TAG = "SearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        editTextSearch = findViewById(R.id.editTextSearch);
        buttonSearch = findViewById(R.id.buttonSearch);
        listViewSearchResults = findViewById(R.id.listViewSearchResults);
        textViewEmptySearch = findViewById(R.id.textViewEmptySearch);

        allMedicinesList = new ArrayList<>();
        displayList = new ArrayList<>();

        // Using a simple built-in layout for the list items
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listViewSearchResults.setAdapter(adapter);

        // Initially show the empty text view instruction
        textViewEmptySearch.setText("Fetching medicines...");
        textViewEmptySearch.setVisibility(View.VISIBLE);
        listViewSearchResults.setVisibility(View.GONE);

        // Fetch all medicines when the activity starts
        fetchAllMedicines();

        // --- Listener for Search Button ---
        buttonSearch.setOnClickListener(v -> performSearch());

        // --- Optional: Listener for Search Action on Keyboard ---
        editTextSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                // Hide keyboard (optional)
                // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                // imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
                return true; // Indicate we handled the action
            }
            return false;
        });

        // Optional: Click listener for list items
        listViewSearchResults.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDisplayString = displayList.get(position);
            // You might want to find the original Medicine object if you need more data
            // This requires iterating through allMedicinesList or using a custom adapter

            Medicine selectedMedicine = null;
            for (Medicine med : allMedicinesList) {
                String medDisplayString = med.getName() + (!med.getAmount().isEmpty() ? " (" + med.getAmount() + ")" : "");
                // OR a more robust way if using toString() in Medicine class:
                // String medDisplayString = med.toString();
                if (medDisplayString.equals(selectedDisplayString)) {
                    selectedMedicine = med;
                    break;
                }
            }

            if (selectedMedicine != null) {
                // Create an Intent to start ShowMedInfo Activity
                Intent intent = new Intent(SearchActivity.this, ShowMedInfo.class);

                // Put the medicine data as extras
                intent.putExtra(ShowMedInfo.EXTRA_MED_ID, selectedMedicine.getId());
                intent.putExtra(ShowMedInfo.EXTRA_MED_NAME, selectedMedicine.getName());
                intent.putExtra(ShowMedInfo.EXTRA_MED_AMOUNT, selectedMedicine.getAmount());

                // Start the activity
                startActivity(intent);

            } else {
                Toast.makeText(SearchActivity.this, "Could not find medicine data.", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(SearchActivity.this, "Selected: " + selectedDisplayString, Toast.LENGTH_SHORT).show();
        });

    }

    private void fetchAllMedicines() {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_MEDS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Fetch All Meds Response: " + response);
                        allMedicinesList.clear(); // Clear previous data

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean error = jsonResponse.getBoolean("error");

                            if (!error && jsonResponse.has("medicines")) {
                                JSONArray medicinesArray = jsonResponse.getJSONArray("medicines");

                                if (medicinesArray.length() > 0) {
                                    for (int i = 0; i < medicinesArray.length(); i++) {
                                        JSONObject medJson = medicinesArray.getJSONObject(i);
                                        int id = medJson.getInt("med_id");
                                        String name = medJson.getString("med_name");
                                        String amount = medJson.optString("med_amount", "");

                                        // Create Medicine object and add to the master list
                                        allMedicinesList.add(new Medicine(id, name, amount));
                                    }
                                    textViewEmptySearch.setText("Enter text and click Search."); // Update instruction
                                    // Don't show list results until search is performed
                                    listViewSearchResults.setVisibility(View.GONE);
                                    textViewEmptySearch.setVisibility(View.VISIBLE);


                                } else {
                                    // No medicines found on server
                                    textViewEmptySearch.setText("No medicines found in the database.");
                                    textViewEmptySearch.setVisibility(View.VISIBLE);
                                    listViewSearchResults.setVisibility(View.GONE);
                                }
                            } else {
                                // Error reported by PHP or "medicines" key missing
                                String message = jsonResponse.optString("message", "Unknown server error.");
                                Toast.makeText(SearchActivity.this, "Error fetching: " + message, Toast.LENGTH_LONG).show();
                                textViewEmptySearch.setText("Error loading medicines from server.");
                                textViewEmptySearch.setVisibility(View.VISIBLE);
                                listViewSearchResults.setVisibility(View.GONE);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing Error during fetch: " + e.getMessage());
                            Toast.makeText(SearchActivity.this, "Error processing server response.", Toast.LENGTH_LONG).show();
                            textViewEmptySearch.setText("Error loading medicines.");
                            textViewEmptySearch.setVisibility(View.VISIBLE);
                            listViewSearchResults.setVisibility(View.GONE);
                        }
                        // Don't update adapter yet, wait for search
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error during fetch: " + error.toString());
                        String errorMessage = "Load failed. ";
                        int statusCode = error.networkResponse != null ? error.networkResponse.statusCode : -1;
                        if (error.networkResponse == null) {
                            errorMessage += "Cannot connect to server. Check IP/Network.";
                        } else {
                            Log.e(TAG, "Fetch Status Code: " + statusCode);
                            errorMessage += "Server error (" + statusCode + ").";
                        }
                        Toast.makeText(SearchActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        textViewEmptySearch.setText("Failed to load medicines. " + (statusCode != -1 ? "(Code: " + statusCode + ")" : ""));
                        textViewEmptySearch.setVisibility(View.VISIBLE);
                        listViewSearchResults.setVisibility(View.GONE);
                    }
                });

        queue.add(stringRequest);
    }


    private void performSearch() {
        String query = editTextSearch.getText().toString().trim().toLowerCase(Locale.getDefault());
        displayList.clear(); // Clear previous search results

        if (allMedicinesList.isEmpty()) {
            textViewEmptySearch.setText("No medicine data loaded to search.");
            textViewEmptySearch.setVisibility(View.VISIBLE);
            listViewSearchResults.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            return;
        }

        if (TextUtils.isEmpty(query)) {
            // If search query is empty, show all medicines
            for (Medicine med : allMedicinesList) {
                String displayString = med.getName() + (!med.getAmount().isEmpty() ? " (" + med.getAmount() + ")" : "");
                displayList.add(displayString);
            }
            textViewEmptySearch.setText("Showing all medicines."); // Or hide empty view
            textViewEmptySearch.setVisibility(View.VISIBLE); // Keep visible to show status
            listViewSearchResults.setVisibility(View.VISIBLE);

        } else {
            // Filter the master list based on the query
            for (Medicine med : allMedicinesList) {
                // Simple "contains" check (case-insensitive)
                if (med.getName().toLowerCase(Locale.getDefault()).contains(query)) {
                    String displayString = med.getName() + (!med.getAmount().isEmpty() ? " (" + med.getAmount() + ")" : "");
                    displayList.add(displayString);
                }
            }

            // Update UI based on whether results were found
            if(displayList.isEmpty()){
                textViewEmptySearch.setText("No medicines found matching '" + query + "'.");
                textViewEmptySearch.setVisibility(View.VISIBLE);
                listViewSearchResults.setVisibility(View.GONE);
            } else {
                textViewEmptySearch.setVisibility(View.GONE); // Hide empty text view
                listViewSearchResults.setVisibility(View.VISIBLE); // Show list view
            }
        }

        // Notify the adapter that the data set (displayList) has changed
        adapter.notifyDataSetChanged();
    }
}