package cn.springlogic.blog.service.impl;

import cn.springlogic.blog.jpa.entity.Article;
import cn.springlogic.blog.jpa.repository.ArticleRepository;
import cn.springlogic.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by admin on 2017/4/24.
 */
@Component
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Override
    public void save(Article article) {
        articleRepository.save(article);
    }
}
