package com.example.yourappname; // Replace with your package name

// ... other imports (AsyncTask, Log, Toast, JSONObject, etc.)
import android.os.AsyncTask;
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

public class MainActivity extends AppCompatActivity { // Or your relevant Activity

    // --- URL for your delete_medicine.php script ---
    // IMPORTANT: Replace with your server URL
    private static final String DELETE_MEDICINE_URL = "http://YOUR_SERVER_IP_OR_DOMAIN/your_php_script_folder/delete_medicine.php";

    // You might have a TextView to show results
    // private TextView textViewDeleteStatus;

    // ... (onCreate, other methods, allMedicinesList if you're integrating with fetch)

    // --- Method to initiate deleting a medicine ---
    // You would call this method, perhaps from an OnClickListener of a delete button
    // associated with a specific medicine item in a list.
    public void deleteMedicineById(int medicineId) {
        if (medicineId <= 0) {
            Toast.makeText(this, "Invalid Medicine ID for deletion.", Toast.LENGTH_SHORT).show();
            return;
        }

        // if (textViewDeleteStatus != null) textViewDeleteStatus.setText("Deleting medicine...");
        new DeleteMedicineTask().execute(medicineId);
    }


    // --- AsyncTask to delete medicine in the background ---
    private class DeleteMedicineTask extends AsyncTask<Integer, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Optional: Show progress, disable UI elements
            // For example, if you have a global status TextView:
            // if (textViewStatus != null) {
            //     textViewStatus.setText("Attempting to delete medicine...");
            // }
        }

        @Override
        protected String doInBackground(Integer... params) {
            if (params.length == 0 || params[0] == null) {
                return "{\"error\":true, \"message\":\"Medicine ID not provided to task.\"}";
            }
            int medicineIdToDelete = params[0];

            HashMap<String, String> postDataParams = new HashMap<>();
            postDataParams.put("medicine_id", String.valueOf(medicineIdToDelete)); // Key must match PHP's $_POST key

            StringBuilder result = new StringBuilder();
            HttpURLConnection conn = null;

            try {
                URL url = new URL(DELETE_MEDICINE_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setReadTimeout(10000); // 10 seconds
                conn.setConnectTimeout(10000); // 10 seconds

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
                StringBuilder postData = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> entry : postDataParams.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        postData.append("&");
                    }
                    postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    postData.append("=");
                    postData.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                }
                Log.d("DeleteMedicineTask", "POST Data: " + postData.toString());

                writer.write(postData.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("DeleteMedicineTask", "Response Code: " + responseCode);

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
                    Log.e("DeleteMedicineTask", "Error response (" + responseCode + "): " + result.toString());
                    return "{\"error\":true, \"message\":\"Server error: " + responseCode + "\"}";
                }

            } catch (Exception e) {
                Log.e("DeleteMedicineTask", "Exception: " + e.getMessage(), e);
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
            Log.d("DeleteMedicineTask", "Raw Server Response: " + result);

            // if (textViewDeleteStatus != null) textViewDeleteStatus.setText(""); // Clear status

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean error = jsonResponse.optBoolean("error", true);
                String message = jsonResponse.optString("message", "Unknown server response.");

                if (error) {
                    Toast.makeText(MainActivity.this, "Deletion Failed: " + message, Toast.LENGTH_LONG).show();
                    // if (textViewStatus != null) textViewStatus.setText("Deletion Failed: " + message);
                } else {
                    Toast.makeText(MainActivity.this, "Deletion Success: " + message, Toast.LENGTH_LONG).show();
                    // if (textViewStatus != null) textViewStatus.setText("Deletion Success: " + message);

                    // IMPORTANT: After successful deletion, you should update your local list
                    // (e.g., allMedicinesList) and refresh your UI (e.g., RecyclerView adapter).
                    // For example, you might call fetchMedicinesData() again,
                    // or if you know the ID, remove it from your 'allMedicinesList' and notify adapter.
                    // Example:
                    // refreshLocalDataAfterDeletion(idOfTheDeletedItem);
                }
            } catch (JSONException e) {
                Log.e("DeleteMedicineTask", "JSON Parsing error: " + e.getMessage() + " for response: " + result);
                Toast.makeText(MainActivity.this, "Error parsing delete response.", Toast.LENGTH_LONG).show();
                // if (textViewStatus != null) textViewStatus.setText("Error parsing delete response.");
            }
        }
    }

    // --- Example placeholder for how you might refresh data ---
    // You'll need to implement this based on how you're managing your list of medicines
    /*
    private void refreshLocalDataAfterDeletion(int deletedMedicineId) {
        if (allMedicinesList != null) {
            for (int i = 0; i < allMedicinesList.size(); i++) {
                if (allMedicinesList.get(i).getMedicineID() == deletedMedicineId) {
                    allMedicinesList.remove(i);
                    // If using RecyclerView: yourAdapter.notifyItemRemoved(i);
                    // If using ListView: yourAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
        // Or, simply re-fetch all data:
        // fetchMedicinesData();
    }
    */
}