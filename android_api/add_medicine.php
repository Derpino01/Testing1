<?php
header("Content-Type: application/json");

// --- Database Configuration ---
$db_host = "localhost";
$db_user = "root";
$db_pass = ""; // Your WAMP MySQL password
$db_name = "android_auth"; // Your database name

$response = array();

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    $medicine_name = isset($_POST['medicine_name']) ? trim($_POST['medicine_name']) : '';
    $medicine_amount = isset($_POST['medicine_amount']) ? trim($_POST['medicine_amount']) : ''; // Amount is optional based on DB schema

    // Basic Validation
    if (empty($medicine_name)) {
        $response['error'] = true;
        $response['message'] = "Medicine Name cannot be empty.";
    } else {
        $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

        if ($conn->connect_error) {
            $response['error'] = true;
            $response['message'] = "Database Connection Failed: " . $conn->connect_error;
        } else {
            // Prepare INSERT statement
            $stmt = $conn->prepare("INSERT INTO tbl_Medicine (med_name, med_amount) VALUES (?, ?)");
            if ($stmt === false) {
                 $response['error'] = true;
                 $response['message'] = "Prepare failed: (" . $conn->errno . ") " . $conn->error;
            } else {
                // Bind parameters (s = string)
                $stmt->bind_param("ss", $medicine_name, $medicine_amount);

                if ($stmt->execute()) {
                    $response['error'] = false;
                    $response['message'] = "Medicine added successfully!";
                } else {
                    $response['error'] = true;
                    $response['message'] = "Failed to add medicine: " . $stmt->error;
                }
                $stmt->close();
            }
            $conn->close();
        }
    }
} else {
    $response['error'] = true;
    $response['message'] = "Invalid Request Method.";
}

echo json_encode($response);
?>