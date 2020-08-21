package com.github.mushanwb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.*;


public class JdbcCrawlerDao implements CrawlerDao {

    private static final String DATABASE_USER_NAME = "homestead";
    private static final String DATABASE_USER_PASSWORD = "secret";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        String jdbcUrl = "jdbc:mysql://192.168.10.10:3306/news?characterEncoding=utf-8";
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, DATABASE_USER_NAME, DATABASE_USER_PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    // 判断连接是否被处理过
    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM links_already_processed WHERE link = ?")) {
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

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink("SELECT * FROM links_to_be_processed LIMIT 1");
        System.out.println(link);
        if (link != null) {
            updateDatabase(link, "DELETE FROM links_to_be_processed WHERE link = ?");
        }
        return link;
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO news (title, content, url, created_at, updated_at) values ( ?, ?, ?, now(), now() ) ")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, link);
            statement.executeUpdate();
        }
    }

    @Override
    public void insertProcessedLink(String link) throws SQLException {
        updateDatabase(link, "INSERT INTO links_already_processed (link) VALUES (?)");
    }

    @Override
    public void insertLinkToBeProcessed(String link) throws SQLException {
        updateDatabase(link, "INSERT INTO links_to_be_processed (link) VALUES (?)");
    }

    private void updateDatabase(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private String getNextLink(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString("link");
            }
        }
        return null;
    }

}
