package com.github.mushanwb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;


public class Main {
    private static final String DATABASE_USER_NAME = "root";
    private static final String DATABASE_USER_PASSWORD = "root123";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {

        File projectDir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
        String jdbcUrl = "jdbc:h2:file:" + new File(projectDir, "news").getAbsolutePath();
        Connection connection = DriverManager.getConnection(jdbcUrl, DATABASE_USER_NAME, DATABASE_USER_PASSWORD);

        String link;
        // 从数据库中取出要被解析的链接并且删除，如果取出的为null，则表示没有要解析的链接
        while ((link = getNextLinkThenDelete(connection)) != null) {

            // 链接已经被解析过了
            if (isLinkProcessed(connection, link)) {
                continue;
            }

            // 如果链接时有效链接，则进行解析，有效链接即为分析得出来得链接规则
            if (isInterestingLink(link)) {
                // 获取链接的 html 文档
                Document doc = httpGetAndParseHtml(link);

                // 分析 html 文档并且从中获取新的新闻链接放入到即将处理链接的数据库中
                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);

                // 分析文档，如果时新闻，则插入到数据库中（有标题的则为新闻）
                storeIntoDatabaseIfItIsNewsPage(doc);

                // 处理完的链接加入到已处理的数据库中
                updateDatabase(connection, link, "INSERT INTO LINKS_ALREADY_PROCESSED (link) VALUES (?)");

            }
        }
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        ArrayList<Element> links = doc.select("a");
        for (Element aTag:links) {
            String href = aTag.attr("href");
            if (isNesPage(href)) {
                // 将爬取的链接放入即将处理的数据中
                updateDatabase(connection, href, "INSERT INTO LINKS_TO_BE_PROCESSED (link) VALUES (?)");
            }
        }
        //doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);
    }

    // 从数据库加载已经处理的链接
    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM LINKS_ALREADY_PROCESSED WHERE LINK = ?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static void updateDatabase(Connection connection, String href, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, href);
            statement.executeUpdate();
        }
    }

    private static String getNextLink(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getNString("LINK");
            }
        }
        return null;
    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = getNextLink(connection, "SELECT * FROM LINKS_TO_BE_PROCESSED LIMIT 1");
        System.out.println(link);
        if (link != null) {
            updateDatabase(connection, link, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE link = ?");
        }
        return link;
    }

    private static void storeIntoDatabaseIfItIsNewsPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag:articleTags) {
                System.out.println(articleTag.select(".art_tit_h1").text());
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
            System.out.println(response1.getStatusLine());
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
