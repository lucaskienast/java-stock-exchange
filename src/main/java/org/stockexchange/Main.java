package org.stockexchange;


import org.stockexchange.data.OrderBook;
import org.stockexchange.receiver.OrderReceiver;
import org.stockexchange.service.MatchingEngine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {

        String tickerSymbol = args[0];

        if (tickerSymbol == null)
            throw new RuntimeException("No ticker symbol provided. Shutting down.");

        OrderBook amznOrderBook = new OrderBook(tickerSymbol);

        // start matching engine which listens for match signals and resolves them
        MatchingEngine amznMatchingEngine = new MatchingEngine(amznOrderBook);

        int coreCount = Runtime.getRuntime().availableProcessors();
        ExecutorService matchingExecutorService = Executors.newFixedThreadPool(2);
        matchingExecutorService.submit(amznMatchingEngine);

        ExecutorService receiverExecutorService = Executors.newFixedThreadPool( coreCount - 2);

        // create 100 order receivers, which will place orders in order book
        for (int i = 0; i < 100; i++) {
            receiverExecutorService.execute(new OrderReceiver(amznOrderBook));
        }

        receiverExecutorService.shutdown();
        matchingExecutorService.shutdown();
    }

}