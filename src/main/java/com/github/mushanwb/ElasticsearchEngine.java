package com.github.mushanwb;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ElasticsearchEngine {

    public static void main(String[] args) throws IOException {
        while (true) {

            System.out.println("keyword:");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));

            String keyword = reader.readLine();

            search(keyword);

        }
    }

    private static void search(String keyword) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            SearchRequest searchRequest = new SearchRequest("news");
            searchRequest.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder("title", "content", keyword)));

            SearchResponse result = client.search(searchRequest, RequestOptions.DEFAULT);

            result.getHits().forEach(hit -> System.out.println(hit.getSourceAsString()));
        }
    }
}
