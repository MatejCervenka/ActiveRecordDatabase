package cz.cervenka.databaseproject.services;

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
