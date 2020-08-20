package com.github.mushanwb;

import java.sql.SQLException;

public interface CrawlerDao {

    boolean isLinkProcessed(String link) throws SQLException;

    void updateDatabase(String href, String sql) throws SQLException;

    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDatabase(String title, String content, String link) throws SQLException;

}
