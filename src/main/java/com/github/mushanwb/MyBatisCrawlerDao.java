package com.github.mushanwb;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

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
        return false;
    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            String link = session.selectOne("com.github.mushanwb.MyMapper.selectNextAvailableLink");
            if (link != null) {
                session.delete("com.github.mushanwb.MyMapper.deleteLink", link);
            }
            return link;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        
    }

    @Override
    public void insertProcessedLink(String link) throws SQLException {

    }

    @Override
    public void insertLinkToBeProcessed(String href) throws SQLException {

    }
}
