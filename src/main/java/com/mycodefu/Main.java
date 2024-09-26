package com.mycodefu;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Starting server at http://localhost:8080/search?text=dog&colour=Black&breed=Labrador&size=Large&page=0");
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/search", new AtlasSearchHandler());
        server.start();
    }
}