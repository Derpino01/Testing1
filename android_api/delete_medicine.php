<?php
header('Content-Type: application/json'); // Set response type to JSON

// --- Database Configuration ---
$db_host = 'localhost'; // Or your DB host
$db_user = 'root';      // Your DB username
$db_pass = '';          // Your DB password
$db_name = 'android_auth'; // CHANGE TO YOUR DATABASE NAME

$response = array('error' => true, 'message' => 'An unknown error occurred.');

// --- Check if med_id is provided via POST ---
if (isset($_POST['med_id'])) {
    $med_id = $_POST['med_id'];

    // Validate if med_id is a number
    if (!is_numeric($med_id)) {
        $response['message'] = 'Invalid Medicine ID provided.';
        echo json_encode($response);
        exit; // Stop script execution
    }

    // --- Connect to Database ---
    $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

    // Check connection
    if ($conn->connect_error) {
        $response['message'] = 'Database Connection Failed: ' . $conn->connect_error;
        // In production, log the detailed error but don't show it to the user
        // error_log('DB Connection Error: ' . $conn->connect_error);
        // $response['message'] = 'Database connection error.';
         echo json_encode($response);
         exit;
    }

    // --- Prepare Delete Statement (Prevent SQL Injection) ---
    $sql = "DELETE FROM medicines WHERE med_id = ?"; // CHANGE 'medicines' to your table name
    $stmt = $conn->prepare($sql);

    if ($stmt) {
        // Bind the integer parameter 'i'
        $stmt->bind_param("i", $med_id);

        // Execute the statement
        if ($stmt->execute()) {
            // Check if any row was actually deleted
            if ($stmt->affected_rows > 0) {
                $response['error'] = false;
                $response['message'] = 'Medicine deleted successfully.';
            } else {
                // ID might not exist
                $response['message'] = 'Medicine not found or already deleted.';
                // You might consider setting error to false here too if "not found" isn't an error state
                // $response['error'] = false;
            }
        } else {
            // Execution failed
            $response['message'] = 'Database Error: Could not execute delete statement.';
             // error_log('SQL Execute Error: ' . $stmt->error); // Log detailed error
        }
        // Close statement
        $stmt->close();
    } else {
        // Statement preparation failed
        $response['message'] = 'Database Error: Could not prepare delete statement.';
        // error_log('SQL Prepare Error: ' . $conn->error); // Log detailed error
    }

    // Close connection
    $conn->close();

} else {
    // med_id not provided in POST request
    $response['message'] = 'Required parameter (med_id) missing.';
}

// --- Send JSON response back to the Android app ---
echo json_encode($response);
?>