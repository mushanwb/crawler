package com.github.mushanwb;

public class Main {
    public static void main(String[] args) {
        CrawlerDao crawlerDao = new MyBatisCrawlerDao();

        for (int i = 0; i < 8; i++) {
            new Crawler(crawlerDao).start();
        }
    }
}
