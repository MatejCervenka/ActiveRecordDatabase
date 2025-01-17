package cz.cervenka.databaseproject.services;

/**
 * Represents a summary report containing details about customer orders, most sold products, and highest value products.
 * The `SummaryReport` class is used to store aggregated information about customer orders, product sales, and revenue.
 * This class contains various fields to represent data such as the customer's name, total orders, total revenue,
 * most sold product, highest value product, and their respective quantities or values.
 */
public class SummaryReport {
    private String customerName;
    private int totalOrders;
    private double totalRevenue;

    private String mostSoldProduct;
    private int totalSold;

    private String highestValueProduct;
    private double maxValue;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public String getMostSoldProduct() {
        return mostSoldProduct;
    }

    public void setMostSoldProduct(String mostSoldProduct) {
        this.mostSoldProduct = mostSoldProduct;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    public String getHighestValueProduct() {
        return highestValueProduct;
    }

    public void setHighestValueProduct(String highestValueProduct) {
        this.highestValueProduct = highestValueProduct;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
}
