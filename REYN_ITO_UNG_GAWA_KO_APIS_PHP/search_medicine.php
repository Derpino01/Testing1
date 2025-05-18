<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *"); // Optional

// --- Database Configuration ---
$db_host = "localhost";
$db_user = "root";
$db_pass = ""; // Your WAMP MySQL password (if any)
$db_name = "android_auth"; // Your database name

$response = array('error' => false, 'message' => '', 'medicines' => array());

if ($_SERVER['REQUEST_METHOD'] == 'GET') {
    // --- Retrieve GET data ---
    $search_query = isset($_GET['search_query']) ? trim($_GET['search_query']) : '';

    if (empty($search_query)) {
        $response['error'] = true;
        $response['message'] = "Search query cannot be empty.";
    } else {
        $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

        if ($conn->connect_error) {
            $response['error'] = true;
            $response['message'] = "Database Connection Failed: " . $conn->connect_error;
        } else {
            // Prepare the search term for SQL LIKE query
            // Adding '%' wildcards to search for the query anywhere in the names
            $searchTerm = "%" . $conn->real_escape_string($search_query) . "%";

            // Prepare SELECT statement
            // Searching in Brand_Name OR Generic_Name
            // You can add more fields to search in if needed
            $stmt = $conn->prepare("SELECT MedicineID, Brand_Name, Generic_Name, CategoryID, RegulationID, Price, Description
                                    FROM tbl_Medicine
                                    WHERE Brand_Name LIKE ? OR Generic_Name LIKE ?
                                    ORDER BY Brand_Name"); // Optional: Order results

            if ($stmt === false) {
                 $response['error'] = true;
                 $response['message'] = "Prepare failed: (" . $conn->errno . ") " . $conn->error;
            } else {
                // Bind parameters: 's' for string (for both LIKE clauses)
                $stmt->bind_param("ss", $searchTerm, $searchTerm);

                if ($stmt->execute()) {
                    $result = $stmt->get_result(); // Get result set from prepared statement
                    if ($result->num_rows > 0) {
                        $medicines_array = array();
                        while ($row = $result->fetch_assoc()) {
                            $medicines_array[] = $row;
                        }
                        $response['error'] = false;
                        $response['message'] = $result->num_rows . " medicine(s) found.";
                        $response['medicines'] = $medicines_array;
                    } else {
                        $response['error'] = false; // Not an error, just no results
                        $response['message'] = "No medicines found matching your search.";
                    }
                    $result->close();
                } else {
                    $response['error'] = true;
                    $response['message'] = "Failed to execute search: " . $stmt->error;
                }
                $stmt->close();
            }
            $conn->close();
        }
    }
} else {
    $response['error'] = true;
    $response['message'] = "Invalid Request Method. Only GET is accepted for search.";
}

echo json_encode($response);
?>