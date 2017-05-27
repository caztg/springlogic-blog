package cn.springlogic.blog.jpa.entity.rest;

import cn.springlogic.blog.jpa.entity.Article;
import org.springframework.data.rest.core.config.Projection;

import java.util.Date;
import java.util.Set;

/**
 * Created by admin on 2017/4/26.
 */
@Projection(name = "articlefull",types = {Article.class})
public interface ArticleProjection {
    int getId();
    String getContent();
    Date getCreateTime();
    Set<MediaProjection> getMedias();
    UserProjection getUser();

}
