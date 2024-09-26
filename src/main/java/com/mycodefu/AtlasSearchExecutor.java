package com.mycodefu;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.search.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.search.SearchFacet.stringFacet;
import static com.mongodb.client.model.search.SearchOperator.compound;
import static com.mongodb.client.model.search.SearchOperator.of;
import static com.mongodb.client.model.search.SearchPath.fieldPath;

public class AtlasSearchExecutor {
    MongoClient mongoClient = MongoClients.create("mongodb://localhost");
    MongoDatabase database = mongoClient.getDatabase("AtlasSearch");
    MongoCollection<Document> photo = database.getCollection("photo");

    ArrayList<Document> search(String text, String page, String colour, String breed, String size) {
        try {
            List<SearchOperator> clauses = new ArrayList<>();

            int skip = 0;
            int pageSize = 5;
            if (page != null) {
                skip = Integer.parseInt(page) * pageSize;
            }
            if (text != null) {
                clauses.add(SearchOperator.of(
                        new Document("text", new Document()
                                .append("path", "summary")
                                .append("query", text)
                        ))
                );
            }
            if (colour != null) {
                clauses.add(SearchOperator.of(
                        new Document("equals", new Document()
                                .append("path", "colours")
                                .append("value", colour)
                        ))
                );
            }
            if (breed != null) {
                clauses.add(SearchOperator.of(
                        new Document("equals", new Document()
                                .append("path", "breeds")
                                .append("value", breed)
                        ))
                );
            }
            if (size != null) {
                clauses.add(SearchOperator.of(
                        new Document("equals", new Document()
                                .append("path", "sizes")
                                .append("value", size)
                        ))
                );
            }
            ArrayList<Document> results = photo.aggregate(List.of(
                    Aggregates.search(
                        SearchCollector.facet(
                                SearchOperator.compound().filter(clauses),
                                List.of(
                                        stringFacet("colours", fieldPath("colours")).numBuckets(10),
                                        stringFacet("breeds", fieldPath("breeds")).numBuckets(10),
                                        stringFacet("sizes", fieldPath("sizes")).numBuckets(10)
                                )
                    ), SearchOptions.searchOptions().count(SearchCount.total())),
                    Aggregates.skip(skip),
                    Aggregates.limit(pageSize),
                    Aggregates.project(Document.parse("{_id: 0, summary: 1, caption: 1, url: 1, dogs: 1}")),
                    Aggregates.facet(
                            new Facet("docs", List.of()),
                            new Facet("meta", List.of(
                                    Aggregates.replaceWith("$$SEARCH_META"),
                                    Aggregates.limit(1)
                            ))
                    )

            )).into(new ArrayList<>());

            return results;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
