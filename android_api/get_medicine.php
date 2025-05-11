<?php
header("Content-Type: application/json");

// --- Database Configuration ---
$db_host = "localhost";
$db_user = "root";
$db_pass = ""; // Your WAMP MySQL password
$db_name = "android_auth"; // Your database name

$response = array();
$medicines_list = array(); // Array to hold medicine data

$conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

if ($conn->connect_error) {
    $response['error'] = true;
    $response['message'] = "Database Connection Failed: " . $conn->connect_error;
} else {
    // Prepare SELECT statement - fetch all medicines, order by name
    $stmt = $conn->prepare("SELECT med_id, med_name, med_amount FROM tbl_Medicine ORDER BY med_name ASC");

    if ($stmt === false) {
        $response['error'] = true;
        $response['message'] = "Prepare failed: (" . $conn->errno . ") " . $conn->error;
    } else {
        if ($stmt->execute()) {
            $result = $stmt->get_result(); // Get the result set

            if ($result->num_rows > 0) {
                // Fetch associative array
                while ($row = $result->fetch_assoc()) {
                    $medicines_list[] = $row; // Add each row (medicine object) to the list
                }
                $response['error'] = false;
                $response['medicines'] = $medicines_list; // Add the list to the response

            } else {
                 $response['error'] = false; // Not an error, just no data
                 $response['message'] = "No medicines found.";
                 $response['medicines'] = array(); // Send empty array
            }
        } else {
            $response['error'] = true;
            $response['message'] = "Failed to retrieve medicines: " . $stmt->error;
        }
        $stmt->close();
    }
    $conn->close();
}

echo json_encode($response);
?>