package com.example.yourappname; // Replace with your package name

// ... other imports (AsyncTask, Log, Toast, JSONObject, Medicine POJO, ArrayList, List, etc.)
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // For status
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
// If you have a RecyclerView to display results:
// import androidx.recyclerview.widget.LinearLayoutManager;
// import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchMedicinesActivity extends AppCompatActivity {

    // --- URL for your search_medicines.php script ---
    // IMPORTANT: Replace with your server URL
    private static final String SEARCH_MEDICINES_URL = "http://YOUR_SERVER_IP_OR_DOMAIN/your_php_script_folder/search_medicines.php";

    private EditText editTextSearchQuery;
    private Button buttonSearch;
    // private RecyclerView recyclerViewSearchResults; // If displaying in a RecyclerView
    // private MedicineAdapter searchResultsAdapter; // Your custom adapter
    private TextView textViewSearchStatus;

    // This list will hold the search results
    public List<Medicine> searchResultsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Your layout file

        searchResultsList = new ArrayList<>();

        editTextSearchQuery = findViewById(R.id.editTextSearchQuery); // Add to your XML
        buttonSearch = findViewById(R.id.buttonSearch);             // Add to your XML
        textViewSearchStatus = findViewById(R.id.textViewSearchStatus); // Add to your XML
        // recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults); // Add to your XML

        // --- Setup for RecyclerView (if you use one) ---
        // if (recyclerViewSearchResults != null) {
        //     recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        //     searchResultsAdapter = new MedicineAdapter(this, searchResultsList); // Assuming you have a MedicineAdapter
        //     recyclerViewSearchResults.setAdapter(searchResultsAdapter);
        // }


        if (buttonSearch != null) {
            buttonSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String query = editTextSearchQuery.getText().toString().trim();
                    if (!query.isEmpty()) {
                        performSearch(query);
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a search term.", Toast.LENGTH_SHORT).show();
                        // Optionally clear previous results or show all medicines
                        // searchResultsList.clear();
                        // if(searchResultsAdapter != null) searchResultsAdapter.notifyDataSetChanged();
                        // if(textViewSearchStatus != null) textViewSearchStatus.setText("Enter search term.");
                    }
                }
            });
        }

        // --- Optional: Search as user types (with debounce) ---
        /*
        if (editTextSearchQuery != null) {
            editTextSearchQuery.addTextChangedListener(new TextWatcher() {
                private android.os.Handler handler = new android.os.Handler();
                private Runnable runnable;
                private final long DELAY = 700; // milliseconds

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (runnable != null) {
                        handler.removeCallbacks(runnable);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            String query = s.toString().trim();
                            if (query.length() > 1) { // Min length to trigger search
                                performSearch(query);
                            } else if (query.isEmpty()) {
                                searchResultsList.clear();
                                if(searchResultsAdapter != null) searchResultsAdapter.notifyDataSetChanged();
                                if(textViewSearchStatus != null) textViewSearchStatus.setText("Enter search term.");
                                // Or fetch all items if search is empty:
                                // fetchAllMedicines();
                            }
                        }
                    };
                    handler.postDelayed(runnable, DELAY);
                }
            });
        }
        */
    }

    private void performSearch(String query) {
        if (textViewSearchStatus != null) {
            textViewSearchStatus.setText("Searching for: " + query + "...");
        }
        new SearchMedicinesTask().execute(query);
    }

    // --- AsyncTask to search medicines in the background ---
    private class SearchMedicinesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (params.length == 0 || params[0] == null || params[0].isEmpty()) {
                return "{\"error\":true, \"message\":\"Search query not provided to task.\"}";
            }
            String searchQuery = params[0];
            HttpURLConnection conn = null;
            StringBuilder result = new StringBuilder();
            String fullUrl;

            try {
                // Encode the search query for the URL
                String encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString());
                fullUrl = SEARCH_MEDICINES_URL + "?search_query=" + encodedQuery;
                Log.d("SearchMedicinesTask", "Request URL: " + fullUrl);

                URL url = new URL(fullUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.connect();

                int responseCode = conn.getResponseCode();
                Log.d("SearchMedicinesTask", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        result.append(line);
                    }
                    in.close();
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        result.append(line);
                    }
                    in.close();
                    Log.e("SearchMedicinesTask", "Error response (" + responseCode + "): " + result.toString());
                    return "{\"error\":true, \"message\":\"Server error: " + responseCode + "\"}";
                }
            } catch (Exception e) {
                Log.e("SearchMedicinesTask", "Exception: " + e.getMessage(), e);
                return "{\"error\":true, \"message\":\"Network/Request Exception: " + e.getMessage() + "\"}";
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("SearchMedicinesTask", "Raw Server Response: " + result);
            searchResultsList.clear(); // Clear previous search results

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean error = jsonResponse.optBoolean("error", true);
                String message = jsonResponse.optString("message", "Unknown server response.");

                if (!error) {
                    JSONArray medicinesJsonArray = jsonResponse.getJSONArray("medicines");
                    for (int i = 0; i < medicinesJsonArray.length(); i++) {
                        JSONObject medJson = medicinesJsonArray.getJSONObject(i);
                        // Assuming you have the Medicine POJO and its constructor
                        int medId = medJson.getInt("MedicineID");
                        String brand = medJson.getString("Brand_Name");
                        String generic = medJson.getString("Generic_Name");
                        double price = medJson.getDouble("Price");
                        Integer catId = medJson.isNull("CategoryID") ? null : medJson.getInt("CategoryID");
                        Integer regId = medJson.isNull("RegulationID") ? null : medJson.getInt("RegulationID");
                        String desc = medJson.isNull("Description") ? null : medJson.getString("Description");

                        Medicine medicine = new Medicine(medId, brand, generic, catId, regId, price, desc);
                        searchResultsList.add(medicine);
                    }
                    if (textViewSearchStatus != null) {
                        textViewSearchStatus.setText(message); // "X medicine(s) found." or "No medicines found..."
                    }
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

                } else {
                    if (textViewSearchStatus != null) {
                        textViewSearchStatus.setText("Search Error: " + message);
                    }
                    Toast.makeText(MainActivity.this, "Search Error: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Log.e("SearchMedicinesTask", "JSON Parsing error: " + e.getMessage());
                if (textViewSearchStatus != null) {
                    textViewSearchStatus.setText("Error parsing search results.");
                }
                Toast.makeText(MainActivity.this, "Error parsing search results.", Toast.LENGTH_LONG).show();
            }

            // --- Update your RecyclerView Adapter (if used) ---
            // if (searchResultsAdapter != null) {
            //     searchResultsAdapter.notifyDataSetChanged(); // Or use DiffUtil for better performance
            // }

            // For debugging: Log the search results
            for(Medicine m : searchResultsList) {
                Log.d("SearchResult", m.toString());
            }
        }
    }
    // You would need a MedicineAdapter if you are using a RecyclerView
    // and the Medicine.java POJO class from previous examples.
}