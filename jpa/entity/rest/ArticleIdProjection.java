package cn.springlogic.blog.jpa.entity.rest;

import cn.springlogic.blog.jpa.entity.Article;
import org.springframework.data.rest.core.config.Projection;

/**
 * Created by admin on 2017/4/26.
 */
@Projection(types = {Article.class})
public interface ArticleIdProjection {
    int getId();
}
