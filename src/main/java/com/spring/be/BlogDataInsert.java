package com.spring.be;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BlogDataInsert implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 초기 데이터 삽입
        String sql = "INSERT INTO blogs (blog_id, blog_url, review_count, created_at) " +
                "VALUES (1, 'blog.naver.com/kakao_food_fighter', 0, current_timestamp) " +
                "ON CONFLICT (blog_id) DO NOTHING"; // 중복 방지

        entityManager.createNativeQuery(sql).executeUpdate();
        System.out.println("초기 데이터가 삽입되었습니다.");
    }
}

