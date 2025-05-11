<?php
header("Content-Type: application/json");

// --- Database Configuration ---
$db_host = "localhost";
$db_user = "root";
$db_pass = ""; // Your WAMP MySQL password
$db_name = "android_auth";

$response = ['error' => true, 'message' => 'Invalid request.'];

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // --- Get Required Data ---
    $current_username = isset($_POST['current_username']) ? trim($_POST['current_username']) : '';
    $current_password = isset($_POST['current_password']) ? trim($_POST['current_password']) : '';
    $new_username = isset($_POST['new_username']) ? trim($_POST['new_username']) : ''; // Optional change
    $new_password = isset($_POST['new_password']) ? trim($_POST['new_password']) : ''; // Optional change

    // --- Basic Validation ---
    if (empty($current_username) || empty($current_password)) {
        $response['message'] = 'Current username and password are required to make changes.';
        echo json_encode($response);
        exit;
    }

    // No changes requested?
    if (empty($new_username) && empty($new_password)) {
         $response['error'] = false; // Not really an error, just nothing to do
         $response['message'] = 'No changes submitted.';
         echo json_encode($response);
         exit;
    }

    // --- Database Connection ---
    $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);
    if ($conn->connect_error) {
        $response['message'] = "Database Connection Failed: " . $conn->connect_error;
        echo json_encode($response);
        exit;
    }

    // --- Verify Current Password ---
    $stmt_verify = $conn->prepare("SELECT password_hash FROM users WHERE username = ?");
    if (!$stmt_verify) {
        $response['message'] = "DB Prepare Error (Verify): " . $conn->error;
        echo json_encode($response);
        $conn->close();
        exit;
    }
    $stmt_verify->bind_param("s", $current_username);
    $stmt_verify->execute();
    $result = $stmt_verify->get_result();

    if ($result->num_rows === 0) {
        $response['message'] = "User not found.";
        $stmt_verify->close();
        $conn->close();
        echo json_encode($response);
        exit;
    }

    $user_data = $result->fetch_assoc();
    $stored_hash = $user_data['password_hash'];
    $stmt_verify->close();

    if (!password_verify($current_password, $stored_hash)) {
        $response['message'] = "Incorrect current password.";
        $conn->close();
        echo json_encode($response);
        exit;
    }

    // --- Current Password Verified - Proceed with Updates ---

    $update_fields = [];
    $bind_types = "";
    $bind_params = [];

    // --- Check if New Username is Provided and Different ---
    if (!empty($new_username) && $new_username !== $current_username) {
        // Check if the NEW username already exists for ANOTHER user
        $stmt_check_user = $conn->prepare("SELECT id FROM users WHERE username = ? AND username != ?");
        if(!$stmt_check_user){
             $response['message'] = "DB Prepare Error (Check User): " . $conn->error;
             echo json_encode($response);
             $conn->close();
             exit;
        }
        $stmt_check_user->bind_param("ss", $new_username, $current_username);
        $stmt_check_user->execute();
        $stmt_check_user->store_result();

        if ($stmt_check_user->num_rows > 0) {
            $response['message'] = "New username is already taken.";
            $stmt_check_user->close();
            $conn->close();
            echo json_encode($response);
            exit;
        }
        $stmt_check_user->close();

        // Add username to update list
        $update_fields[] = "username = ?";
        $bind_types .= "s";
        $bind_params[] = $new_username;
    }

    // --- Check if New Password is Provided ---
    if (!empty($new_password)) {
        $new_password_hash = password_hash($new_password, PASSWORD_DEFAULT);
        $update_fields[] = "password_hash = ?";
        $bind_types .= "s";
        $bind_params[] = $new_password_hash;
    }

    // --- Perform Update if there are fields to update ---
    if (!empty($update_fields)) {
        $sql_update = "UPDATE users SET " . implode(", ", $update_fields) . " WHERE username = ?";
        $bind_types .= "s"; // Add type for the WHERE clause username
        $bind_params[] = $current_username; // Add value for the WHERE clause username

        $stmt_update = $conn->prepare($sql_update);
        if(!$stmt_update){
             $response['message'] = "DB Prepare Error (Update): " . $conn->error;
             echo json_encode($response);
             $conn->close();
             exit;
        }

        // Dynamically bind parameters
        $stmt_update->bind_param($bind_types, ...$bind_params); // Using argument unpacking (...)

        if ($stmt_update->execute()) {
            if ($stmt_update->affected_rows > 0) {
                $response['error'] = false;
                $response['message'] = "User details updated successfully.";
                // Optionally return the new username if it changed
                if (!empty($new_username) && $new_username !== $current_username) {
                    $response['new_username'] = $new_username;
                }
            } else {
                 // This might happen if the submitted new username/password hash was the same as the existing one
                 $response['error'] = false; // Technically not an error
                 $response['message'] = "No changes were needed.";
            }
        } else {
            $response['message'] = "Database update failed: " . $stmt_update->error;
        }
        $stmt_update->close();

    } else {
         $response['error'] = false; // No fields were actually modified
         $response['message'] = "No valid changes submitted.";
    }

    $conn->close();

} else {
    $response['message'] = 'Invalid request method.';
}

echo json_encode($response);
?>