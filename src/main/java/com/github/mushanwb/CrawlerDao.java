package com.github.mushanwb;

import java.sql.SQLException;

public interface CrawlerDao {

    boolean isLinkProcessed(String link) throws SQLException;

    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDatabase(String title, String content, String link) throws SQLException;

    void insertProcessedLink(String link) throws SQLException;

    void insertLinkToBeProcessed(String href) throws SQLException;
}
