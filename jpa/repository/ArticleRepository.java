package cn.springlogic.blog.jpa.repository;

import cn.springlogic.blog.jpa.entity.Article;
import cn.springlogic.blog.jpa.entity.rest.ArticleProjection;
import cn.springlogic.user.jpa.entity.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by admin on 2017/4/18.
 */
@Configuration
@RepositoryRestResource(path="blog:articles",excerptProjection = ArticleProjection.class)
public interface ArticleRepository extends JpaRepository<Article,Integer> {
    //后台用
   //public Page<Article> findBycontentContainsAndIdInAndUserIdIn(@Param("content")String content,@Param("id") List<Integer> ids,@Param("userId") List<Integer>userids,Pageable pageable);


    /**
     * 条件查询 根据话题名称查找出话题.
     * @param topic
     * @param pageable
     * @return
     */
    @RestResource(path = "all",rel = "all")
    @Query("Select distinct a from Article a,Tag t where :topic IS NULL OR t.topic.name LIKE CONCAT('%',:topic,'%')  and t.publication.article.id=a.id order by a.createTime DESC")
    public Page<Article> findByAll(@Param("topic")String topic, Pageable pageable);
 // select a from Article a,Tag t where t.topic.name like %:topic% and t.publication.article.id=a.id order by a.createTime ASC


    /*
    根据 搜索出所有的喜欢该article的用户
    @Query("select a from Article a, Topic topic ,Tag t,publication p where a.id=t.publication.article.id and topic.name=?1")
    @RestResource(path = "a",rel = "a")
    public List<Article> findArticleByTopicName(@Param("name") String name);
    */
    /*
     根据 搜索出所有的喜欢该article的用户


     */
    /*
    @Query("select u from User u,Article a where u in elements(a.favors) and a.id=?1")
    @RestResource(path = "a",rel = "a")
    public List<User> findLikeUser(@Param("id")Integer id);
  */
    /*
      delete from Favor l,Article a where a.id=?1 and l in elements(a.favors) and l.user.id=?2
*/

/*
    @Transactional
    @Modifying
   // @Query("delete from Favor f,Article a where f.user.id=?1 and f in elements(a.favors) and a.id=?2")
   // @Query("delete from Favor f inner join Article.favors a where a.user.id=?1")
    // @Query("delete f from Favor f left join Article a on f in elements(a.favors) where f.user.id=?1 and a.id=?2")
    //取消点赞 , 根绝用户id删除favor表记录,同时根据favor表id删除article_favor中间表记录
    //  select f from Article a,Favor f where f.id = a.favors.id
    //@Query("delete f from Favor f where f in elements(select f from Favor f,Article a where f in elements(a.favors) and f.user.id=?1 and a.id=?2)")
    //@Query("delete f from Favor f,Article a where f in elements(a.favors) and f.user.id=?1 and a.id=?2")
    @Query("delete  from Favor f1 where f1.id =(select f.id from Favor f,Article a where f in elements(a.favors) and f.user.id=?1 and a.id=?2)")
    @RestResource(path = "delete",rel = "delete")
    public void deleteLike(@Param("userid")int userId,@Param("articleid")int articleId);


    //根据用户id和文章id 查找出 该文章的该用户的favor
    @Query("select f from Favor f,Article a where f in elements(a.favors) and f.user.id=?1 and a.id=?2")
    @RestResource(path = "b",rel = "b")
    public Favor findFavor(@Param("userid")int userId,@Param("articleid")int articleId);

    @Query("select f.id from Favor f,Article a where f in elements(a.favors) and f.user.id=?1 and a.id=?2")
    @RestResource(path = "c",rel = "c")
    public Integer getFavorId(@Param("userid")int userId,@Param("articleid")int articleId);
*/




}
