//package com.example.yourappname; // Replace with your package name

// ... other imports (AsyncTask, Log, Toast, JSONObject, Medicine POJO, etc.)
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
// Import other necessary classes: Button, TextView, etc. if you have UI for this

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class EditMedicineActivity extends AppCompatActivity { // Or your relevant Activity

    // --- URL for your update_medicine.php script ---
    // IMPORTANT: Replace with your server URL
    private static final String UPDATE_MEDICINE_URL = "http://YOUR_SERVER_IP_OR_DOMAIN/your_php_script_folder/update_medicine.php";

    // Assume allMedicinesList is your local list of Medicine objects from previous examples
    // public List<Medicine> allMedicinesList;
    // private TextView textViewUpdateStatus;

    // ... (onCreate, other methods like fetchMedicinesData, addMedicine, deleteMedicineById)

    /**
     * Initiates updating a medicine.
     * You would typically call this after a user edits medicine details in a form
     * and clicks a "Save" or "Update" button.
     *
     * @param medicineToUpdate The Medicine object containing the ID and the new/updated values.
     */
    public void updateMedicine(Medicine medicineToUpdate) {
        if (medicineToUpdate == null) {
            Toast.makeText(this, "Medicine data is null.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Basic client-side validation (similar to add, but for an existing object)
        if (medicineToUpdate.getMedicineID() <= 0) {
            Toast.makeText(this, "Invalid Medicine ID for update.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(medicineToUpdate.getBrandName())) {
            Toast.makeText(this, "Brand Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return; // Or set error on an EditText if you have a form
        }
        if (TextUtils.isEmpty(medicineToUpdate.getGenericName())) {
            Toast.makeText(this, "Generic Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (medicineToUpdate.getPrice() <= 0) { // Or just check if a valid number was entered
            Toast.makeText(this, "Price must be a valid positive number.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Optional: Add checks for CategoryID/RegulationID if they have specific client-side rules

        // if (textViewUpdateStatus != null) textViewUpdateStatus.setText("Updating medicine...");
        new UpdateMedicineTask().execute(medicineToUpdate);
    }

    // --- AsyncTask to update medicine in the background ---
    private class UpdateMedicineTask extends AsyncTask<Medicine, Void, String> {

        private Medicine updatedMedicine; // To hold the medicine object for onPostExecute

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Optional: Show progress, disable UI elements
        }

        @Override
        protected String doInBackground(Medicine... params) {
            if (params.length == 0 || params[0] == null) {
                return "{\"error\":true, \"message\":\"Medicine data not provided to task.\"}";
            }
            updatedMedicine = params[0]; // Store for later use in onPostExecute

            HashMap<String, String> postDataParams = new HashMap<>();
            postDataParams.put("medicine_id", String.valueOf(updatedMedicine.getMedicineID()));
            postDataParams.put("brand_name", updatedMedicine.getBrandName());
            postDataParams.put("generic_name", updatedMedicine.getGenericName());
            postDataParams.put("price", String.valueOf(updatedMedicine.getPrice()));

            // Handle nullable Integer fields for CategoryID and RegulationID
            if (updatedMedicine.getCategoryID() != null) {
                postDataParams.put("category_id", String.valueOf(updatedMedicine.getCategoryID()));
            } else {
                postDataParams.put("category_id", ""); // Send empty string, PHP will handle as NULL
            }

            if (updatedMedicine.getRegulationID() != null) {
                postDataParams.put("regulation_id", String.valueOf(updatedMedicine.getRegulationID()));
            } else {
                postDataParams.put("regulation_id", ""); // Send empty string
            }

            // Handle nullable String for Description
            if (updatedMedicine.getDescription() != null) {
                postDataParams.put("description", updatedMedicine.getDescription());
            } else {
                postDataParams.put("description", ""); // Send empty string
            }

            StringBuilder result = new StringBuilder();
            HttpURLConnection conn = null;

            try {
                URL url = new URL(UPDATE_MEDICINE_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                StringBuilder postDataString = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : postDataParams.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        postDataString.append("&");
                    }
                    postDataString.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    postDataString.append("=");
                    postDataString.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                Log.d("UpdateMedicineTask", "POST Data: " + postDataString.toString());

                writer.write(postDataString.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("UpdateMedicineTask", "Response Code: " + responseCode);

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
                    Log.e("UpdateMedicineTask", "Error response (" + responseCode + "): " + result.toString());
                    return "{\"error\":true, \"message\":\"Server error: " + responseCode + "\"}";
                }

            } catch (Exception e) {
                Log.e("UpdateMedicineTask", "Exception: " + e.getMessage(), e);
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
            Log.d("UpdateMedicineTask", "Raw Server Response: " + result);

            // if (textViewUpdateStatus != null) textViewUpdateStatus.setText(""); // Clear status

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean error = jsonResponse.optBoolean("error", true);
                String message = jsonResponse.optString("message", "Unknown server response.");

                if (error) {
                    Toast.makeText(MainActivity.this, "Update Failed: " + message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Update Success: " + message, Toast.LENGTH_LONG).show();

                    // IMPORTANT: After successful update, update your local list
                    // (e.g., allMedicinesList) and refresh your UI (e.g., RecyclerView adapter).
                    if (updatedMedicine != null && allMedicinesList != null) { // allMedicinesList from previous example
                        boolean found = false;
                        for (int i = 0; i < allMedicinesList.size(); i++) {
                            if (allMedicinesList.get(i).getMedicineID() == updatedMedicine.getMedicineID()) {
                                allMedicinesList.set(i, updatedMedicine); // Replace with the updated object
                                // If using RecyclerView: yourAdapter.notifyItemChanged(i);
                                // If using ListView: yourAdapter.notifyDataSetChanged();
                                found = true;
                                break;
                            }
                        }
                        if (!found) { // Should not happen if UI is consistent with data
                             Log.w("UpdateMedicineTask", "Updated medicine not found in local list for UI update.");
                             // Optionally, re-fetch all data as a fallback:
                             // fetchMedicinesData();
                        }
                    }
                }
            } catch (JSONException e) {
                Log.e("UpdateMedicineTask", "JSON Parsing error: " + e.getMessage() + " for response: " + result);
                Toast.makeText(MainActivity.this, "Error parsing update response.", Toast.LENGTH_LONG).show();
            }
        }
    }
}