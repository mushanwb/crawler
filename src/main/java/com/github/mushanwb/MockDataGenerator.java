package com.github.mushanwb;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {

    private static final int TARGET_ROW_COUNT = 100_0000;

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException();
        }

        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<News> currentNews = session.selectList("com.github.mushanwb.MockMapper.selectAllNews");

            int count = TARGET_ROW_COUNT - currentNews.size();
            Random random = new Random();
            try {
                while (count > 0) {
                    int index = random.nextInt(currentNews.size());
                    News newsToBeInserted = currentNews.get(index);
                    Instant currentTime = newsToBeInserted.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600*24*365));
                    newsToBeInserted.setCreatedAt(currentTime);
                    newsToBeInserted.setUpdatedAt(currentTime);
                    session.insert("com.github.mushanwb.MockMapper.insertNews", newsToBeInserted);
                    count--;
                }
                // 事务操作,如果不需要,将 openSession 的参数更改为 true,每次执行就会插入
                // openSession 的参数可以为 ExecutorType.BATCH ,表示先预读 sql,读完之后一次性全部插入
                session.commit();
            } catch (Exception e) {
                session.rollback();
                throw new RuntimeException();
            }
        }
    }

}
