package cn.springlogic.blog.jpa.repository;

import cn.springlogic.blog.jpa.entity.Category;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Created by admin on 2017/4/18.
 */
@Configuration
@RepositoryRestResource(path="category")
public interface CategoryRepository extends JpaRepository<Category,Integer>{

    @Query(value = "select t.article_id from category as c,article_category as t where c.id = t.category_id and c.name like %?1% ",nativeQuery = true)
    List<Integer> findArticlesIdByCategoryName(String name);

}
