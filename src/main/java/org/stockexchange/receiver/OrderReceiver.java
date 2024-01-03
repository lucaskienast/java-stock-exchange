package org.stockexchange.receiver;


import org.stockexchange.data.OrderBook;
import org.stockexchange.model.Order;
import org.stockexchange.model.OrderType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class OrderReceiver implements Runnable {

    private final OrderBook orderBook;
    private final Random random = new Random();

    public OrderReceiver(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Generate a random order
                BigDecimal price = BigDecimal.valueOf(50 + random.nextDouble())
                        .setScale(2, RoundingMode.HALF_UP);
                int quantity = random.nextInt(1, 1000);
                long timestamp = System.currentTimeMillis();
                OrderType orderType = random.nextBoolean() ? OrderType.BUY : OrderType.SELL;

                Order order = new Order(orderBook.getTickerSymbol(), price.doubleValue(), quantity, timestamp, orderType);
                orderBook.addOrder(order);
                System.out.println(Thread.currentThread().getName() + " - Placed order: " + order);

                Thread.sleep(100); // Sleep for a second between orders
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                System.out.println("OrderReceiver interrupted");
            }
        }
    }
}
