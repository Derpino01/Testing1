package com.prog.tryagain;

public class Medicine {
    private int id;
    private String name;
    private String amount;

    public Medicine(int id, String name, String amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAmount() {
        return amount;
    }

    // Optional: Override toString() for simple display if needed elsewhere
    @Override
    public String toString() {
        return name + (!amount.isEmpty() ? " (" + amount + ")" : "");
    }
}