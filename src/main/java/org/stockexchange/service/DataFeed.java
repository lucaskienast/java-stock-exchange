package org.stockexchange.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.stockexchange.dto.PriceDto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DataFeed {

    private static final int SERVER_PORT = 5000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void send(PriceDto priceDto) {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            InetAddress address = InetAddress.getLocalHost();

            String json = objectMapper.writeValueAsString(priceDto);
            byte[] buffer = json.getBytes();

            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, address, SERVER_PORT);
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
