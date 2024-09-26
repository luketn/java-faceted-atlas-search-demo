package com.mycodefu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AtlasSearchHandler implements HttpHandler {
    ObjectMapper objectMapper = new ObjectMapper();
    AtlasSearchExecutor executor = new AtlasSearchExecutor();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            query = "";
        }

        Map<String, String> queryMap = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                queryMap.put(entry[0], entry[1]);
            }
        }
        System.out.println("Received query: " + queryMap);

        ArrayList<Document> results = executor.search(
                queryMap.get("text"),
                queryMap.get("page"),
                queryMap.get("colour"),
                queryMap.get("breed"),
                queryMap.get("size")
        );

        String response = objectMapper.writeValueAsString(results);
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
