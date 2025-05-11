<?php
header("Content-Type: application/json");

// --- Database Configuration ---
$db_host = "localhost";
$db_user = "root";
$db_pass = ""; // Your WAMP MySQL password
$db_name = "android_auth"; // Your database name

$response = array('error' => true, 'message' => 'Username not provided.');

// Check if username is sent via POST
if (isset($_POST['username'])) {
    $username_to_fetch = trim($_POST['username']);

    if (empty($username_to_fetch)) {
        $response['message'] = 'Username cannot be empty.';
    } else {
        $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

        if ($conn->connect_error) {
            $response['message'] = "Database Connection Failed: " . $conn->connect_error;
        } else {
            // Prepare SELECT statement - Fetch only username and hash
            // DO NOT SELECT or try to send the original password
            $stmt = $conn->prepare("SELECT username, password_hash, created_at FROM users WHERE username = ?");

            if ($stmt === false) {
                 $response['message'] = "Prepare failed: (" . $conn->errno . ") " . $conn->error;
            } else {
                $stmt->bind_param("s", $username_to_fetch);
                $stmt->execute();
                $result = $stmt->get_result(); // Get result set

                if ($result->num_rows > 0) {
                    $user_data = $result->fetch_assoc(); // Fetch data as an associative array

                    $response['error'] = false;
                    $response['message'] = "User data retrieved successfully.";
                    // Send back ONLY username and the HASH (or maybe just username)
                    // NEVER send the original password
                    $response['user'] = [
                        'username' => $user_data['username'],
                        'password_hash' => $user_data['password_hash'], // Sending hash for demo, but not displaying it
                        'created_at' => $user_data['created_at']
                    ];

                } else {
                    // User not found
                    $response['message'] = "User not found.";
                }
                $stmt->close();
            }
            $conn->close();
        }
    }
} else {
     $response['message'] = "Invalid request method or missing username.";
}

echo json_encode($response);
?>