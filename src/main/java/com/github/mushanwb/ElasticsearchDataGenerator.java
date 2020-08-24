package com.github.mushanwb;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchDataGenerator {

    public static void main(String[] args) throws IOException {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException();
        }

        List<News> newsFromMySql = getNewsFromMySql(sqlSessionFactory);

        for (int i = 0; i < 3; i++) {
            new Thread(()->writeSingleThread(newsFromMySql)).start();
        }

    }

    private static void writeSingleThread(List<News> newsFromMySql) {
        // 创建 Elasticsearch 客户端
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            for (int i = 0; i < 10; i++) {
                BulkRequest bulkRequest = new BulkRequest();
                for (News news:newsFromMySql) {
                    IndexRequest request = new IndexRequest("news").type("news");

                    Map<String,Object> data = new HashMap<>();
                    data.put("content",news.getContent());
                    data.put("url",news.getUrl());
                    data.put("title",news.getTitle());
                    data.put("createdAt",news.getCreatedAt());
                    data.put("updatedAt",news.getUpdatedAt());
                    request.source(data, XContentType.JSON);

                    // 使用块插入
                    bulkRequest.add(request);
                    // 如果不用块的方法
//                    IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
//                    System.out.println(indexResponse.status().getStatus());
                }

                // 使用块插入
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println(bulkResponse.status().getStatus());

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<News> getNewsFromMySql(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList("com.github.mushanwb.MockMapper.selectAllNews");
        }
    }

}
