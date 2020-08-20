package com.github.mushanwb;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class Crawler {

    private CrawlerDao dao = new MyBatisCrawlerDao();

    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }

    public void run() throws IOException, SQLException {

        String link;
        // 从数据库中取出要被解析的链接并且删除，如果取出的为null，则表示没有要解析的链接
        while ((link = dao.getNextLinkThenDelete()) != null) {

            // 链接已经被解析过了
            if (dao.isLinkProcessed(link)) {
                continue;
            }

            // 如果链接时有效链接，则进行解析，有效链接即为分析得出来得链接规则
            if (isInterestingLink(link)) {
                // 获取链接的 html 文档
                Document doc = httpGetAndParseHtml(link);

                // 分析 html 文档并且从中获取新的新闻链接放入到即将处理链接的数据库中
                parseUrlsFromPageAndStoreIntoDatabase(doc);

                // 分析文档，如果时新闻，则插入到数据库中（有标题的则为新闻）
                storeIntoDatabaseIfItIsNewsPage(doc, link);

                // 处理完的链接加入到已处理的数据库中
                dao.insertProcessedLink(link);
            }
        }
    }

    private void parseUrlsFromPageAndStoreIntoDatabase(Document doc) throws SQLException {
        ArrayList<Element> links = doc.select("a");
        for (Element aTag:links) {
            String href = aTag.attr("href");
            if (isNesPage(href)) {
                // 将爬取的链接放入即将处理的数据中
                dao.insertLinkToBeProcessed(href);
            }
        }
    }

    private void storeIntoDatabaseIfItIsNewsPage(Document doc, String link) throws SQLException {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag:articleTags) {
                String title = articleTag.select(".art_tit_h1").text();
                String content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewsIntoDatabase(title, content, link);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestingLink(String link) {
        return isIndexPage(link) || isNesPage(link) || !isLoginPage(link);
    }

    private static boolean isNesPage(String link) {
        return link.contains("https://news.sina.cn/") || link.contains("http://news.sina.cn/");
    }

    private static boolean isIndexPage(String link) {
        return link.equals("https://sina.cn");
    }

    private static boolean isLoginPage(String link) {
        return link.contains("/signin/signin");
    }
}
