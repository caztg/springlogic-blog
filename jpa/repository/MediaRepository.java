package cn.springlogic.blog.jpa.repository;

import cn.springlogic.blog.jpa.entity.Media;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by admin on 2017/4/18.
 */
@Configuration
@RepositoryRestResource(path="medias")
public interface MediaRepository extends JpaRepository<Media,Integer>{
}
