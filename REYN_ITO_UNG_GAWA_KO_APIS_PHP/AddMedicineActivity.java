package com.example.yourappname; // Replace with your actual package name

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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
import javax.net.ssl.HttpsURLConnection; // Use this if your server uses HTTPS
import javax.swing.text.View;

public class AddMedicineActivity extends AppCompatActivity {

    // IMPORTANT: Replace with your server URL
    // If using emulator and WAMP/XAMPP on host machine: "http://10.0.2.2/path/to/your/script/add_medicine.php"
    // If using physical device on same Wi-Fi: "http://YOUR_COMPUTER_LAN_IP/path/to/your/script/add_medicine.php"
    // If deployed on a live server: "http://www.yourdomain.com/path/to/your/script/add_medicine.php"
    

    //change mo nlang ung IP4 address
    private static final String ADD_MEDICINE_URL = "http://192.168.0.115/android_api/add_medicine.php";

    private EditText editTextBrandName, editTextGenericName, editTextPrice,
            editTextCategoryId, editTextRegulationId, editTextDescription;
    private Button buttonAddMedicine;
    private TextView textViewResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextBrandName = findViewById(R.id.editTextBrandName);
        editTextGenericName = findViewById(R.id.editTextGenericName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextCategoryId = findViewById(R.id.editTextCategoryId);
        editTextRegulationId = findViewById(R.id.editTextRegulationId);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonAddMedicine = findViewById(R.id.buttonAddMedicine);
        textViewResponse = findViewById(R.id.textViewResponse);

        buttonAddMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMedicine();
            }
        });
    }

    private void addMedicine() {
        String brandName = editTextBrandName.getText().toString().trim();
        String genericName = editTextGenericName.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String categoryIdStr = editTextCategoryId.getText().toString().trim();
        String regulationIdStr = editTextRegulationId.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        // --- Basic Client-Side Validation (mirroring PHP but less strict on optional fields) ---
        if (TextUtils.isEmpty(brandName)) {
            editTextBrandName.setError("Brand Name is required");
            editTextBrandName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(genericName)) {
            editTextGenericName.setError("Generic Name is required");
            editTextGenericName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(priceStr)) {
            editTextPrice.setError("Price is required");
            editTextPrice.requestFocus();
            return;
        }
        try {
            Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            editTextPrice.setError("Price must be a valid number");
            editTextPrice.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(categoryIdStr)) {
            try {
                Integer.parseInt(categoryIdStr);
            } catch (NumberFormatException e) {
                editTextCategoryId.setError("Category ID must be a number or empty");
                editTextCategoryId.requestFocus();
                return;
            }
        }
        if (!TextUtils.isEmpty(regulationIdStr)) {
            try {
                Integer.parseInt(regulationIdStr);
            } catch (NumberFormatException e) {
                editTextRegulationId.setError("Regulation ID must be a number or empty");
                editTextRegulationId.requestFocus();
                return;
            }
        }

        // --- Prepare data for POST request ---
        HashMap<String, String> params = new HashMap<>();
        params.put("brand_name", brandName);
        params.put("generic_name", genericName);
        params.put("price", priceStr);
        // Send optional fields even if empty, PHP script handles them
        params.put("category_id", categoryIdStr);
        params.put("regulation_id", regulationIdStr);
        params.put("description", description);


        // --- Execute AsyncTask to perform network operation ---
        new AddMedicineTask().execute(params);
    }


    private class AddMedicineTask extends AsyncTask<HashMap<String, String>, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            textViewResponse.setText("Adding medicine...");
            buttonAddMedicine.setEnabled(false);
        }

        @Override
        protected String doInBackground(HashMap<String, String>... params) {
            HashMap<String, String> postDataParams = params[0];
            StringBuilder result = new StringBuilder();
            HttpURLConnection conn = null; // Change to HttpsURLConnection if using HTTPS

            try {
                URL url = new URL(ADD_MEDICINE_URL);
                conn = (HttpURLConnection) url.openConnection(); // Use HttpsURLConnection for HTTPS
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setReadTimeout(15000); // 15 seconds
                conn.setConnectTimeout(15000); // 15 seconds

                // --- Building POST parameters ---
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
                Log.d("AddMedicineTask", "POST Data: " + postData.toString());

                writer.write(postData.toString());
                writer.flush();
                writer.close();
                os.close();

                // --- Reading response ---
                int responseCode = conn.getResponseCode();
                Log.d("AddMedicineTask", "Response Code: " + responseCode);

                // Check if the request was successful (HTTP 200 OK)
                if (responseCode == HttpURLConnection.HTTP_OK) { // or HttpsURLConnection.HTTP_OK
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
                    // You might want to prepend an error indicator or parse this differently
                    Log.e("AddMedicineTask", "Error response: " + result.toString());
                    return "{\"error\":true, \"message\":\"Server error: " + responseCode + "\"}"; // Default error JSON
                }

            } catch (Exception e) {
                Log.e("AddMedicineTask", "Exception: " + e.getMessage(), e);
                return "{\"error\":true, \"message\":\"Exception: " + e.getMessage() + "\"}"; // Default error JSON
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
            buttonAddMedicine.setEnabled(true);
            Log.d("AddMedicineTask", "Raw Response: " + result);
            textViewResponse.setText("Raw: " + result); // Show raw response for debugging

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean error = jsonResponse.getBoolean("error");
                String message = jsonResponse.getString("message");

                if (error) {
                    Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
                    textViewResponse.setText("Error: " + message);
                } else {
                    Toast.makeText(MainActivity.this, "Success: " + message, Toast.LENGTH_LONG).show();
                    textViewResponse.setText("Success: " + message);
                    // Optionally clear fields on success
                    editTextBrandName.setText("");
                    editTextGenericName.setText("");
                    editTextPrice.setText("");
                    editTextCategoryId.setText("");
                    editTextRegulationId.setText("");
                    editTextDescription.setText("");
                    editTextBrandName.requestFocus();
                }
            } catch (JSONException e) {
                Log.e("AddMedicineTask", "JSON Parsing error: " + e.getMessage());
                Toast.makeText(MainActivity.this, "Error parsing server response.", Toast.LENGTH_LONG).show();
                textViewResponse.setText("Error parsing server response: " + result);
            }
        }
    }
}