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

    // --- Retrieve POST data for updating ---
    // Mandatory: MedicineID to identify the record
    $medicine_id_str = isset($_POST['medicine_id']) ? trim($_POST['medicine_id']) : '';

    // Fields to update (mirroring the add script for consistency)
    $brand_name = isset($_POST['brand_name']) ? trim($_POST['brand_name']) : '';
    $generic_name = isset($_POST['generic_name']) ? trim($_POST['generic_name']) : '';
    $price_str = isset($_POST['price']) ? trim($_POST['price']) : '';

    // Optional fields
    $category_id_str = isset($_POST['category_id']) ? trim($_POST['category_id']) : '';
    $regulation_id_str = isset($_POST['regulation_id']) ? trim($_POST['regulation_id']) : '';
    $description = isset($_POST['description']) ? trim($_POST['description']) : null;

    // --- Basic Validation ---
    if (empty($medicine_id_str) || !is_numeric($medicine_id_str)) {
        $response['error'] = true;
        $response['message'] = "Valid Medicine ID is required for update.";
    } elseif (empty($brand_name)) {
        $response['error'] = true;
        $response['message'] = "Brand Name cannot be empty.";
    } elseif (empty($generic_name)) {
        $response['error'] = true;
        $response['message'] = "Generic Name cannot be empty.";
    } elseif ($price_str === '' || !is_numeric($price_str)) {
        $response['error'] = true;
        $response['message'] = "Price cannot be empty and must be a valid number.";
    } else {
        $medicine_id = (int)$medicine_id_str;
        $price = (double)$price_str;
        $category_id = null;
        $regulation_id = null;

        // Validate and convert CategoryID if provided
        if ($category_id_str !== '') {
            if (is_numeric($category_id_str)) {
                $category_id = (int)$category_id_str;
            } else {
                $response['error'] = true;
                $response['message'] = "CategoryID must be a valid number or empty.";
            }
        }

        // Validate and convert RegulationID if provided (only if no previous error)
        if (!$response['error'] && $regulation_id_str !== '') {
            if (is_numeric($regulation_id_str)) {
                $regulation_id = (int)$regulation_id_str;
            } else {
                $response['error'] = true;
                $response['message'] = "RegulationID must be a valid number or empty.";
            }
        }

        // Proceed only if there are no validation errors
        if (!$response['error']) {
            $conn = new mysqli($db_host, $db_user, $db_pass, $db_name);

            if ($conn->connect_error) {
                $response['error'] = true;
                $response['message'] = "Database Connection Failed: " . $conn->connect_error;
            } else {
                // Prepare UPDATE statement
                // Column order for SET: Brand_Name, Generic_Name, CategoryID, RegulationID, Price, Description
                // WHERE clause uses MedicineID
                $stmt = $conn->prepare("UPDATE tbl_Medicine SET Brand_Name = ?, Generic_Name = ?, CategoryID = ?, RegulationID = ?, Price = ?, Description = ? WHERE MedicineID = ?");

                if ($stmt === false) {
                     $response['error'] = true;
                     $response['message'] = "Prepare failed: (" . $conn->errno . ") " . $conn->error;
                } else {
                    // Bind parameters:
                    // Types: Brand_Name (s), Generic_Name (s), CategoryID (i), RegulationID (i), Price (d), Description (s), MedicineID (i)
                    $stmt->bind_param("ssiidsi",
                        $brand_name,
                        $generic_name,
                        $category_id,
                        $regulation_id,
                        $price,
                        $description,
                        $medicine_id // This is for the WHERE clause
                    );

                    if ($stmt->execute()) {
                        if ($stmt->affected_rows > 0) {
                            $response['error'] = false;
                            $response['message'] = "Medicine updated successfully!";
                        } else {
                            // This can happen if the MedicineID doesn't exist OR if the data submitted is identical to what's already in the DB
                            $response['error'] = false; // Or true, depending on desired behavior for "no change"
                            $response['message'] = "Medicine not found or no changes were made.";
                        }
                    } else {
                        $response['error'] = true;
                        $response['message'] = "Failed to update medicine: " . $stmt->error;
                    }
                    $stmt->close();
                }
                $conn->close();
            }
        }
    }
} else {
    $response['error'] = true;
    $response['message'] = "Invalid Request Method. Only POST is accepted.";
}

echo json_encode($response);
?>