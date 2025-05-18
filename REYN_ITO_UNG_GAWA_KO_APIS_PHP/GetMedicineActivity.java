package com.example.yourappname; // Replace with your package name

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button; // Assuming you have a button to trigger fetch
import android.widget.TextView; // To display status or count
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GetMedicineActivity extends AppCompatActivity {

    // --- URL for your get_medicines.php script ---
    // IMPORTANT: Replace with your server URL
    // If using emulator and WAMP/XAMPP on host machine: "http://10.0.2.2/path/to/your/script/get_medicines.php"
    // If using physical device on same Wi-Fi: "http://YOUR_COMPUTER_LAN_IP/path/to/your/script/get_medicines.php"
    // If deployed on a live server: "http://www.yourdomain.com/path/to/your/script/get_medicines.php"
    private static final String FETCH_MEDICINES_URL = "http://YOUR_SERVER_IP_OR_DOMAIN/your_php_script_folder/get_medicines.php";

    // --- "Public" variable to store the fetched medicines ---
    // It's a member variable of the Activity.
    // For true global accessibility, consider a Singleton or Application class,
    // but for use within this Activity, a member variable is fine.
    public List<Medicine> allMedicinesList;

    private Button buttonFetchMedicines; // Example button
    private TextView textViewStatus;     // Example TextView to show status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure you have a layout

        allMedicinesList = new ArrayList<>(); // Initialize the list

        // --- Example: Find UI elements (adjust to your layout) ---
        buttonFetchMedicines = findViewById(R.id.buttonFetchMedicines); // Make sure this ID exists in your XML
        textViewStatus = findViewById(R.id.textViewStatus);         // Make sure this ID exists in your XML

        if (buttonFetchMedicines != null) {
            buttonFetchMedicines.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fetchMedicinesData();
                }
            });
        } else {
            Log.e("MainActivity", "buttonFetchMedicines is null. Check your layout XML.");
        }
        // You might want to fetch data automatically on create
        // fetchMedicinesData();
    }

    // --- Method to initiate fetching medicines ---
    public void fetchMedicinesData() {
        if (textViewStatus != null) {
            textViewStatus.setText("Fetching medicines...");
        }
        new FetchMedicinesTask().execute();
    }

    // --- AsyncTask to fetch medicines in the background ---
    private class FetchMedicinesTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(FETCH_MEDICINES_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET"); // GET request for fetching
                conn.setReadTimeout(15000); // 15 seconds
                conn.setConnectTimeout(15000); // 15 seconds
                conn.connect(); // For GET, connect() is often implicit with getInputStream()

                int responseCode = conn.getResponseCode();
                Log.d("FetchMedicinesTask", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        result.append(line);
                    }
                    in.close();
                } else {
                    // Read error stream if not HTTP_OK
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    String line;
                    while ((line = in.readLine()) != null) {
                        result.append(line);
                    }
                    in.close();
                    Log.e("FetchMedicinesTask", "Error response: " + result.toString());
                    return "{\"error\":true, \"message\":\"Server error: " + responseCode + "\"}";
                }
            } catch (Exception e) {
                Log.e("FetchMedicinesTask", "Exception: " + e.getMessage(), e);
                return "{\"error\":true, \"message\":\"Exception: " + e.getMessage() + "\"}";
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
            Log.d("FetchMedicinesTask", "Raw Response: " + result);

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean error = jsonResponse.getBoolean("error");
                String message = jsonResponse.getString("message");

                if (!error) {
                    JSONArray medicinesJsonArray = jsonResponse.getJSONArray("medicines");
                    allMedicinesList.clear(); // Clear previous data

                    for (int i = 0; i < medicinesJsonArray.length(); i++) {
                        JSONObject medJson = medicinesJsonArray.getJSONObject(i);

                        int medId = medJson.getInt("MedicineID");
                        String brand = medJson.getString("Brand_Name");
                        String generic = medJson.getString("Generic_Name");
                        double price = medJson.getDouble("Price");

                        // Handle nullable integers for CategoryID and RegulationID
                        Integer catId = medJson.isNull("CategoryID") ? null : medJson.getInt("CategoryID");
                        Integer regId = medJson.isNull("RegulationID") ? null : medJson.getInt("RegulationID");

                        // Handle nullable string for Description
                        String desc = medJson.isNull("Description") ? null : medJson.getString("Description");

                        Medicine medicine = new Medicine(medId, brand, generic, catId, regId, price, desc);
                        allMedicinesList.add(medicine);
                    }

                    String successMsg = "Success: " + message + " Fetched " + allMedicinesList.size() + " medicines.";
                    Toast.makeText(MainActivity.this, successMsg, Toast.LENGTH_LONG).show();
                    if (textViewStatus != null) {
                        textViewStatus.setText(successMsg);
                    }
                    Log.i("FetchMedicinesTask", "Fetched " + allMedicinesList.size() + " medicines.");
                    // Now allMedicinesList is populated. You can use it to update a ListView, RecyclerView, etc.
                    // Example: Log all fetched medicines
                    for (Medicine m : allMedicinesList) {
                        Log.d("FetchedMedicine", m.toString());
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                    if (textViewStatus != null) {
                        textViewStatus.setText("Error: " + message);
                    }
                }
            } catch (JSONException e) {
                Log.e("FetchMedicinesTask", "JSON Parsing error: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Error parsing server response.", Toast.LENGTH_LONG).show();
                if (textViewStatus != null) {
                    textViewStatus.setText("Error parsing data: " + result.substring(0, Math.min(result.length(), 100)));
                }
            }
        }
    }
}