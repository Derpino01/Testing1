package com.example.yourappname; // Replace with your package name

public class Medicine {
    private int medicineID; // Assuming MedicineID is your primary key and an INT
    private String brandName;
    private String genericName;
    private Integer categoryID; // Use Integer to allow for null
    private Integer regulationID; // Use Integer to allow for null
    private double price;
    private String description;

    // Constructor (optional, but good practice)
    public Medicine(int medicineID, String brandName, String genericName, Integer categoryID, Integer regulationID, double price, String description) {
        this.medicineID = medicineID;
        this.brandName = brandName;
        this.genericName = genericName;
        this.categoryID = categoryID;
        this.regulationID = regulationID;
        this.price = price;
        this.description = description;
    }

    // Getters (and Setters if you need them later)
    public int getMedicineID() { return medicineID; }
    public String getBrandName() { return brandName; }
    public String getGenericName() { return genericName; }
    public Integer getCategoryID() { return categoryID; }
    public Integer getRegulationID() { return regulationID; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "Medicine{" +
                "medicineID=" + medicineID +
                ", brandName='" + brandName + '\'' +
                ", genericName='" + genericName + '\'' +
                ", price=" + price +
                (categoryID != null ? ", categoryID=" + categoryID : "") +
                (regulationID != null ? ", regulationID=" + regulationID : "") +
                (description != null && !description.isEmpty() ? ", description='" + description + '\'' : "") +
                '}';
    }
}