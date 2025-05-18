<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *"); // Optional

// --- Database Configuration ---
$db_host = "localhost";
$db_user = "root";
$db_pass = ""; // Your WAMP MySQL password (if any)
$db_name = "android_auth"; // Your database name

$response = array('error' => false, 'message' => '');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    // --- Retrieve POST data ---
    // Assuming your primary key is 'MedicineID' and it's an integer
    $medicine_id_str = isset($_POST['medicine_id']) ? trim($_POST['medicine_id']) : '';

    if (empty($medicine_id_str)) {
        $response['error'] = true;
        $response['message'] = "Medicine ID cannot be empty.";
    } elseif (!is_numeric($medicine_id_str)) {
        $response['error'] = true;
        $response['message'] = "Medicine ID must be a valid number.";
    } else {
        $medicine_id = (int)$medicine_id_str;

        $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

        if ($conn->connect_error) {
            $response['error'] = true;
            $response['message'] = "Database Connection Failed: " . $conn->connect_error;
        } else {
            // Prepare DELETE statement
            // IMPORTANT: Ensure 'MedicineID' is the correct name of your primary key column
            $stmt = $conn->prepare("DELETE FROM tbl_Medicine WHERE MedicineID = ?");

            if ($stmt === false) {
                 $response['error'] = true;
                 $response['message'] = "Prepare failed: (" . $conn->errno . ") " . $conn->error;
            } else {
                // Bind parameters: 'i' for integer
                $stmt->bind_param("i", $medicine_id);

                if ($stmt->execute()) {
                    if ($stmt->affected_rows > 0) {
                        $response['error'] = false;
                        $response['message'] = "Medicine deleted successfully!";
                    } else {
                        $response['error'] = true; // Or false, depending on how you want to treat "not found"
                        $response['message'] = "Medicine not found or already deleted.";
                    }
                } else {
                    $response['error'] = true;
                    $response['message'] = "Failed to delete medicine: " . $stmt->error;
                }
                $stmt->close();
            }
            $conn->close();
        }
    }
} else {
    $response['error'] = true;
    $response['message'] = "Invalid Request Method. Only POST is accepted.";
}

echo json_encode($response);
?>