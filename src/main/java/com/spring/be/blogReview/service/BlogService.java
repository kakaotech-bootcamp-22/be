package com.spring.be.blogReview.service;

import com.spring.be.blogReview.repository.BlogRepository;
import com.spring.be.entity.Blog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    public Long saveBlog(String blogUrl) {

        Blog existingBlog = blogRepository.findByBlogUrl(blogUrl);
        if (existingBlog != null) {
            return existingBlog.getBlogId();
        }

        Blog blog = Blog.builder()
                        .blogUrl(blogUrl)
                        .reviewCount(0)
                        .build();
        Blog savedBlog = blogRepository.save(blog);
        return savedBlog.getBlogId();
    }
}
