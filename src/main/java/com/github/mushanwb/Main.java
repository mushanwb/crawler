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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {

        List<String> linkPool = new ArrayList<>();
        Set<String> processedLinks = new HashSet<>();

        linkPool.add("https://sina.cn");

        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }

            String link = linkPool.remove(linkPool.size()-1);
            System.out.println(link);

            if (processedLinks.contains(link)) {
                continue;
            }

            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);

                ArrayList<Element> links = doc.select("a");
                for (Element aTag:links) {
                    if (isNesPage(aTag.attr("href"))) {
                        linkPool.add(aTag.attr("href"));
                    }
                }
//                doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);

                storeIntoDatabaseIfItIsNewsPage(doc);

                processedLinks.add(link);
            } else {

            }
        }
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
        httpGet.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestingLink(String link) {
        return isIndexPage(link) || isNesPage(link);
    }

    private static boolean isNesPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isIndexPage(String link) {
        return link.equals("https://sina.cn");
    }
}
