package com.github.mushanwb;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyBatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = (Integer) session.selectOne("com.github.mushanwb.MyMapper.countLink", link);
            return count != 0;
        }
    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        // openSession(true) 每次执行完自动提交,如果不设置 true ,默认为开启事务
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("com.github.mushanwb.MyMapper.selectNextAvailableLink");
            System.out.println(link);
            if (link != null) {
                session.delete("com.github.mushanwb.MyMapper.deleteLink", link);
            }
            return link;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.mushanwb.MyMapper.insertNews", new News(title, content, link));
        }
    }

    @Override
    public void insertProcessedLink(String link) throws SQLException {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "links_already_processed");
        param.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.mushanwb.MyMapper.insertLink", param);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String href) throws SQLException {
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", "links_to_be_processed");
        param.put("link", href);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.mushanwb.MyMapper.insertLink", param);
        }
    }
}
