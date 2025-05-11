<?php
header("Content-Type: application/json"); // Indicate we're sending JSON back

// --- Database Configuration ---
$db_host = "localhost"; // Or 127.0.0.1
$db_user = "root";      // Your WAMP MySQL username (default is often root)
$db_pass = "";          // Your WAMP MySQL password (default is often empty)
$db_name = "android_auth"; // The database you created

// --- Response Array ---
$response = array();

// --- Check if data was sent via POST ---
if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    // --- Get POST data ---
    // Basic check if parameters exist
    $username = isset($_POST['username']) ? trim($_POST['username']) : '';
    $password = isset($_POST['password']) ? trim($_POST['password']) : '';

    // --- Basic Validation ---
    if (empty($username) || empty($password)) {
        $response['error'] = true;
        $response['message'] = "Username or Password cannot be empty.";
    } else {
        // --- Establish Database Connection ---
        $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

        // Check connection
        if ($conn->connect_error) {
            $response['error'] = true;
            $response['message'] = "Database Connection Failed: " . $conn->connect_error;
        } else {
            // --- Check if username already exists (using prepared statement) ---
            $stmt = $conn->prepare("SELECT id FROM users WHERE username = ?");
            if ($stmt === false) {
                 $response['error'] = true;
                 $response['message'] = "Prepare failed: (" . $conn->errno . ") " . $conn->error;
            } else {
                $stmt->bind_param("s", $username);
                $stmt->execute();
                $stmt->store_result();

                if ($stmt->num_rows > 0) {
                    // Username already exists
                    $response['error'] = true;
                    $response['message'] = "Username already taken.";
                    $stmt->close();
                } else {
                    // Username is available, proceed with insertion
                    $stmt->close(); // Close previous statement

                    // --- **IMPORTANT: Hash the password** ---
                    $password_hash = password_hash($password, PASSWORD_DEFAULT);

                    // --- Prepare INSERT statement ---
                    $stmt_insert = $conn->prepare("INSERT INTO users (username, password_hash) VALUES (?, ?)");
                     if ($stmt_insert === false) {
                         $response['error'] = true;
                         $response['message'] = "Prepare failed (insert): (" . $conn->errno . ") " . $conn->error;
                     } else {
                        $stmt_insert->bind_param("ss", $username, $password_hash);

                        // --- Execute and Check ---
                        if ($stmt_insert->execute()) {
                            $response['error'] = false;
                            $response['message'] = "User registered successfully!";
                        } else {
                            $response['error'] = true;
                            $response['message'] = "Registration Failed: " . $stmt_insert->error;
                        }
                        $stmt_insert->close();
                    }
                }
            }
             // --- Close Connection ---
             $conn->close();
        }
    }
} else {
    // Not a POST request
    $response['error'] = true;
    $response['message'] = "Invalid Request Method. MATTHEW";
}

// --- Send JSON Response Back to Android ---
echo json_encode($response);
?>