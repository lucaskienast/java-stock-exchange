package org.stockexchange.service;


import org.stockexchange.data.OrderBook;
import org.stockexchange.dto.PriceDto;
import org.stockexchange.model.Order;

public class MatchingEngine implements Runnable {

    private final OrderBook orderBook;
    private final DataFeed dataFeed;
    private volatile boolean running = true;

    public MatchingEngine(OrderBook orderBook) {
        this.orderBook = orderBook;
        this.dataFeed = new DataFeed();
    }

    @Override
    public void run() {
        while (running) {
            orderBook.getMatchingLock().lock();
            try {
                while (!orderBook.isMatchPending() && running) {
                    orderBook.getMatchFoundCondition().await();
                }

                if (running && orderBook.isMatchPending()) {
                    System.out.println(Thread.currentThread().getName() + " - Received match signal");
                    matchOrders();
                    orderBook.setMatchPending(false);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                orderBook.getMatchingLock().unlock();
            }
        }
    }

    private void matchOrders() {
        Order topBuyOrder = orderBook.viewTopBuyOrder();
        Order topSellOrder = orderBook.viewTopSellOrder();

        if (topBuyOrder == null || topSellOrder == null) {
            System.out.println("No orders to match.");
            return;
        }

        // set default to buy order price assuming sell price is equal
        double executionPrice = topBuyOrder.getPrice();

        // check if buy and sell prices are unequal and pick first order price
        if (topBuyOrder.getPrice() != topSellOrder.getPrice()) {
            if (topBuyOrder.getTimestamp() < topSellOrder.getTimestamp()) {
                executionPrice = topBuyOrder.getPrice();
            } else {
                executionPrice = topSellOrder.getPrice();
            }
        }

        dataFeed.send(new PriceDto(orderBook.getTickerSymbol(), executionPrice));
        System.out.println(Thread.currentThread().getName() + " - Matched orders at execution price: " + executionPrice + ", orders: " + topBuyOrder + " and: " + topSellOrder);

        // handle differences in order quantities
        if (topBuyOrder.getQuantity() == topSellOrder.getQuantity()) {
            orderBook.removeBuyOrder(topBuyOrder);
            orderBook.removeSellOrder(topSellOrder);
            System.out.println(Thread.currentThread().getName() + " - Order quantities matched exactly");
            return;
        }

        if (topBuyOrder.getQuantity() > topSellOrder.getQuantity()) {
            int difference = topBuyOrder.getQuantity() - topSellOrder.getQuantity();
            Order residualOrder = new Order(orderBook.getTickerSymbol(), topBuyOrder.getPrice(), difference, topBuyOrder.getTimestamp(), topBuyOrder.getType());
            orderBook.removeSellOrder(topSellOrder);
            orderBook.removeBuyOrder(topBuyOrder);
            orderBook.addBuyOrder(residualOrder);
            System.out.println(Thread.currentThread().getName() + " - Added residual order: " + residualOrder);
        } else {
            int difference = topSellOrder.getQuantity() - topBuyOrder.getQuantity();
            Order residualOrder = new Order(orderBook.getTickerSymbol(),topSellOrder.getPrice(), difference, topSellOrder.getTimestamp(), topSellOrder.getType());
            orderBook.removeBuyOrder(topBuyOrder);
            orderBook.removeSellOrder(topSellOrder);
            orderBook.addSellOrder(residualOrder);
            System.out.println(Thread.currentThread().getName() + " - Added residual order: " + residualOrder);
        }
    }

    public void shutdown() {
        running = false;
        orderBook.getMatchingLock().lock();
        try {
            orderBook.getMatchFoundCondition().signalAll();
        } finally {
            orderBook.getMatchingLock().unlock();
        }
    }
}
