<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *"); // Optional: Allows cross-origin requests (e.g., from a web app)

// --- Database Configuration ---
$db_host = "localhost";
$db_user = "root";
$db_pass = ""; // Your WAMP MySQL password (if any)
$db_name = "android_auth"; // Your database name

$response = array('error' => false, 'message' => '', 'medicines' => array());

$conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

if ($conn->connect_error) {
    $response['error'] = true;
    $response['message'] = "Database Connection Failed: " . $conn->connect_error;
} else {
    // Consider adding ORDER BY if you want a specific order
    $sql = "SELECT MedicineID, Brand_Name, Generic_Name, CategoryID, RegulationID, Price, Description FROM tbl_Medicine";
    $result = $conn->query($sql);

    if ($result) {
        if ($result->num_rows > 0) {
            $medicines_array = array();
            while ($row = $result->fetch_assoc()) {
                // Ensure numeric types are cast correctly if needed by client, though JSON usually handles them.
                // PHP will send them as strings or numbers based on their DB type and how fetch_assoc works.
                // Explicit casting can be done here if strict typing is needed on the client side before parsing.
                // Example: $row['Price'] = (float)$row['Price'];
                // Example: $row['CategoryID'] = $row['CategoryID'] === null ? null : (int)$row['CategoryID'];

                $medicines_array[] = $row;
            }
            $response['error'] = false;
            $response['message'] = "Medicines fetched successfully.";
            $response['medicines'] = $medicines_array;
        } else {
            $response['error'] = false; // Not an error, just no data
            $response['message'] = "No medicines found.";
        }
        $result->close();
    } else {
        $response['error'] = true;
        $response['message'] = "Failed to execute query: " . $conn->error;
    }
    $conn->close();
}

echo json_encode($response);
?>