package org.stockexchange.data;


import org.stockexchange.model.Order;
import org.stockexchange.model.OrderType;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OrderBook {

    private final String tickerSymbol;
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;
    private final ReentrantReadWriteLock buyLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock sellLock = new ReentrantReadWriteLock();
    private final Lock matchingLock = new ReentrantLock();
    private final Condition matchFound = matchingLock.newCondition();

    private volatile boolean matchPending = false;

    public OrderBook(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
        this.sellOrders = new PriorityQueue<>(Comparator.comparingDouble(Order::getPrice));
        this.buyOrders = new PriorityQueue<>((o1, o2) -> Double.compare(o2.getPrice(), o1.getPrice()));
    }

    public void addOrder(Order order) {
        if (order.getType() == OrderType.BUY) addBuyOrder(order);
        else addSellOrder(order);
        findMatches();
    }

    private void findMatches() {
        matchingLock.lock();
        buyLock.writeLock().lock();
        sellLock.writeLock().lock();
        try {
            Order topBuy = buyOrders.peek();
            Order topSell = sellOrders.peek();

            System.out.println();
            System.out.println(Thread.currentThread().getName() + " - Trying to match orders...");
            System.out.println(Thread.currentThread().getName() + " - Buy Order=" + topBuy);
            System.out.println(Thread.currentThread().getName() + " - Sell Order=" + topSell);

            if (topBuy != null && topSell != null && topBuy.getPrice() >= topSell.getPrice()) {
                System.out.println(Thread.currentThread().getName() + " - Match found: Buy Order=" + topBuy + ", Sell Order=" + topSell);
                matchPending = true;
                matchFound.signal();
            } else {
                System.out.println(Thread.currentThread().getName() + " - No match found");
            }
        } finally {
            matchingLock.unlock();
            buyLock.writeLock().unlock();
            sellLock.writeLock().unlock();
        }
    }

    public Lock getMatchingLock() {
        return matchingLock;
    }

    public Condition getMatchFoundCondition() {
        return matchFound;
    }

    public boolean isMatchPending() {
        return matchPending;
    }

    public void setMatchPending(boolean matchPending) {
        this.matchPending = matchPending;
    }

    public void addBuyOrder(Order order) {
        buyLock.writeLock().lock();
        try {
            buyOrders.add(order);
        } finally {
            buyLock.writeLock().unlock();
        }
    }

    public void removeBuyOrder(Order order) {
        buyLock.writeLock().lock();
        try {
            buyOrders.remove(order);
        } finally {
            buyLock.writeLock().unlock();
        }
    }

    public void addSellOrder(Order order) {
        sellLock.writeLock().lock();
        try {
            sellOrders.add(order);
        } finally {
            sellLock.writeLock().unlock();
        }
    }

    public void removeSellOrder(Order order) {
        sellLock.writeLock().lock();
        try {
            sellOrders.remove(order);
        } finally {
            sellLock.writeLock().unlock();
        }
    }

    public Order viewTopBuyOrder() {
        buyLock.readLock().lock();
        try {
            return buyOrders.peek();
        } finally {
            buyLock.readLock().unlock();
        }
    }

    public Order viewTopSellOrder() {
        sellLock.readLock().lock();
        try {
            return sellOrders.peek();
        } finally {
            sellLock.readLock().unlock();
        }
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }
}
