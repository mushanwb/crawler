package com.github.mushanwb;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.*;


public class JdbcCrawlerDao implements CrawlerDao {

    private static final String DATABASE_USER_NAME = "root";
    private static final String DATABASE_USER_PASSWORD = "root123";
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        String jdbcUrl = "jdbc:h2:file:D:\\wb\\my\\fork\\crawler\\news";
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

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLink("SELECT * FROM LINKS_TO_BE_PROCESSED LIMIT 1");
        System.out.println(link);
        if (link != null) {
            updateDatabase(link, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE link = ?");
        }
        return link;
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO NEWS (TITLE, CONTENT, URL, CREATED_AT, MODIFIED_AT) values ( ?, ?, ?, now(), now() ) ")) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, link);
            statement.executeUpdate();
        }
    }

    @Override
    public void insertProcessedLink(String link) throws SQLException {
        updateDatabase(link, "INSERT INTO LINKS_ALREADY_PROCESSED (link) VALUES (?)");
    }

    @Override
    public void insertLinkToBeProcessed(String link) throws SQLException {
        updateDatabase(link, "INSERT INTO LINKS_TO_BE_PROCESSED (link) VALUES (?)");
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
                return resultSet.getNString("LINK");
            }
        }
        return null;
    }

}
