package org.stockexchange.model;

import java.util.Objects;

public class Order {

    private final String tickerSymbol;
    private final double price;
    private final int quantity;
    private final long timestamp;
    private final OrderType type;


    public Order(String tickerSymbol, double price, int quantity, long timestamp, OrderType type) {
        this.tickerSymbol = tickerSymbol;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public OrderType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Double.compare(price, order.price) == 0 && quantity == order.quantity && timestamp == order.timestamp && Objects.equals(tickerSymbol, order.tickerSymbol) && type == order.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tickerSymbol, price, quantity, timestamp, type);
    }

    @Override
    public String toString() {
        return "Order{" +
                "tickerSymbol='" + tickerSymbol + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                ", type=" + type +
                '}';
    }
}
