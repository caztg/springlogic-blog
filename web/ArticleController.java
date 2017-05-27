package cn.springlogic.blog.web;

import cn.springlogic.blog.jpa.entity.Article;
import cn.springlogic.blog.jpa.entity.BlogParam;
import cn.springlogic.blog.jpa.repository.ArticleRepository;
import cn.springlogic.blog.service.ArticleService;
import cn.springlogic.social.jpa.entity.*;
import cn.springlogic.social.jpa.entity.Publication;
import cn.springlogic.social.jpa.repository.*;
import cn.springlogic.social.service.TagService;
import cn.springlogic.social.service.TopicService;
import cn.springlogic.user.jpa.repository.UserRepository;
import com.fitcooker.app.BussinessException;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by admin on 2017/4/21.
 */
@RequestMapping("/api/article")
@Controller
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private TagService tagService;
    @Autowired
    private TopicService topicService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepositoryEntityLinks entityLinks;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private PublicationFavorRepository publicationFavorRepository;

    @Autowired
    private PublicationCommentRepository publicationCommentRepository;

    /**
     * BlogParam是一个自定义接收饭圈参数的实体
     *
     * @param blogParam
     * @return
     */
    @RequestMapping(value = "/publish", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<Map<String,Object>> publishArticle(@RequestBody BlogParam blogParam) throws BussinessException {

        try {
            //拿出组合类里面的 article ,持久化到数据库
            Article article = blogParam.getArticle();
            articleService.save(article);// 去Article配置了 cascade = CascadeType.PERSIST 成功,如果不配置 会因为 Media是一个瞬态,保存失败.

        /*拿出组合类里面的 tags .但注意,有可能该 饭圈 没有包含 标签,但是 还是要保存publication这个发布状态.*/
            List<Tag> tags = blogParam.getTags();

            Publication publication = new Publication();
            publication.setArticle(article);
            publication.setUser(article.getUser());
             Tag t=null;
            if (tags != null) {

                for (int i = 0; i < tags.size(); i++) {

                    Tag tag = tags.get(i);
                    //判断是否已经创建了该topic,决定是否设置user创建者
                    Topic tempTopic = topicService.findByname(tag.getTopic().getName());
                    if (tempTopic != null) {
                        //说明有这个topic了,就设置该topic的创建者userid
                        tag.setTopic(tempTopic);
                    }

                    tag.setPublication(publication);
                    t= tagService.save(tag);
                }
            } else {

                Tag tag = new Tag();
                tag.setPublication(publication);
                t=tagService.save(tag);

            }
            ConcurrentHashMap<String,Object>map=new ConcurrentHashMap<>();
            ConcurrentHashMap<String,Object>publicationmap=new ConcurrentHashMap<>();
            ConcurrentHashMap<String,Object>articlemap=new ConcurrentHashMap<>();
            map.put("publication",publicationmap);
            publicationmap.put("id",t.getPublication().getId());
            articlemap.put("id",t.getPublication().getArticle().getId());
            publicationmap.put("article",articlemap);



            return ResponseEntity.ok(map);
        } catch (Exception e) {
            throw new BussinessException("发布失败");
        }

    }

                /*************************************************************/

         /*
         广场 饭圈列表
         */
    @RequestMapping(value = "/square")
    public ResponseEntity<Map<String,Object>> del(@PageableDefault(value = 20, sort = {"createTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                                                  @RequestParam( required = false, name = "topic") String topic,
                                                  @RequestParam(name = "user_id")int userId) {

        Page<Publication> byAll = publicationRepository.findByAll(topic, pageable);

        /**
         *   处理.
         */

        for (Publication p : byAll.getContent()) {

            //获取评论总数
            p.setPublicationCommentsTotal(p.getPublicationComments().size());
            //获取点赞总数
            p.setPublicationFavorsTotal(p.getPublicationFavors().size());

            //处理所有评论列表
            List<PublicationComment> tempComments = publicationCommentRepository.findFirst2BypublicationId(p.getId());



            for (PublicationComment c : tempComments) {
                c.setPublication(null);

            }


            //处理点赞列表
            List<PublicationFavor> tempFavors = publicationFavorRepository.findBypublicationId(p.getId());
            for (PublicationFavor f : tempFavors) {
                f.setPublication(null);
                if(f.getUser().getId()==userId){
                    p.setFavor(f);
                }
            }


            //处理 标签列表
            List<Tag> tempTags = tagRepository.findBypublicationId(p.getId());

            for (Tag t : tempTags) {
                t.setPublication(null);
                Topic temp = t.getTopic();
                if (temp!=null) {
                    Topic tempTopic = topicRepository.getOne(temp.getId());
                    tempTopic.setTags(null);
                    t.setTopic(null);
                    t.setTopic(tempTopic);
                }

            }

            p.setPublicationComments(null);
            /*设置评论列表*/
            p.setPublicationComments(tempComments);

            p.setPublicationFavors(null);

            /*设置点赞列表*/
            //p.setPublicationFavors(tempFavors);

            p.setTags(null);
            p.setTags(tempTags);

            //设置该publication的作者 当前用户是否关注了
            Follow tempfollow = followRepository.findByuserIdAndFollowUserId(userId, p.getUser().getId());
            if(tempfollow!=null){
                p.setFollow(tempfollow);
            }

        }

        //最外层Map
        Map<String,Object> map=new HashedMap();

        //publications组装
        List<Publication> publications;
        publications=byAll.getContent();
        Map<String,List<Publication>> listMap=new HashedMap();
        listMap.put("publications",publications);

        //page :组装
        Map<String,Object> pageMap=new HashedMap();
        pageMap.put("number",byAll.getNumber());
        pageMap.put("size",byAll.getSize());
        pageMap.put("totalElements",byAll.getTotalElements());
        pageMap.put("totalPages",byAll.getTotalPages());


        //_links;组装
        Map<String,Map<String,String>>linkMap=new HashedMap();
        Map<String,String>selfMap=new HashedMap();
        selfMap.put("href","");
        linkMap.put("self",selfMap);

        map.put("_embedded",listMap);
        map.put("_links",linkMap);
        map.put("page",pageMap);


        return ResponseEntity.ok(map);

    }


    /**
     * 读取已经关注用户的饭圈
     * @param pageable
     * @param topic
     * @param userId
     * @return
     */
    @RequestMapping("/follow")
    public ResponseEntity<Map<String,Object>> del2(@PageableDefault(value = 20, sort = {"createTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                                                   @RequestParam( required = false, name = "topic") String topic,
                                                   @RequestParam(name = "user_id")int userId) {

        Page<Publication> byAll = publicationRepository.findByFollow(topic,userId,pageable);

        /**
         *   处理.
         */

        for (Publication p : byAll.getContent()) {

            //获取评论总数
            p.setPublicationCommentsTotal(p.getPublicationComments().size());
            //获取点赞总数
            p.setPublicationFavorsTotal(p.getPublicationFavors().size());

            //处理所有评论列表
            List<PublicationComment> tempComments = publicationCommentRepository.findFirst2BypublicationId(p.getId());



            for (PublicationComment c : tempComments) {
                c.setPublication(null);
                PublicationComment tempReply = (PublicationComment) c.getReplyComment();
                System.out.println("!!!!replycomment==" + tempReply);
                   /*
                   !!!!replycomment==null
                   !!!!replycomment==PublicationComment@8953(publication=null,id=1,content="上一条评论内容",replyComment=null)
                   !!!!replycomment==PublicationComment@8998(publication=null,id,content="上一条评论内容",replyComment="上一个评论@8953")
                   !!!!replycomment==PublicationComment@9091(publication=null,   replyComment="@8998 --@8953")
                    if(tempReply!=null){
                       tempReply.setReplyComment(null); // 只有最后一条
                    }
                  */
            }


            //处理点赞列表
            List<PublicationFavor> tempFavors = publicationFavorRepository.findBypublicationId(p.getId());
            for (PublicationFavor f : tempFavors) {
                f.setPublication(null);
                if(f.getUser().getId()==userId){
                    p.setFavor(f);
                }
            }


            //处理 标签列表
            List<Tag> tempTags = tagRepository.findBypublicationId(p.getId());

            for (Tag t : tempTags) {
                t.setPublication(null);
                Topic temp = t.getTopic();
                if (temp!=null) {
                    Topic tempTopic = topicRepository.getOne(temp.getId());
                    tempTopic.setTags(null);
                    t.setTopic(null);
                    t.setTopic(tempTopic);
                }

                System.out.println("!!!!!tag=" + t);
            }

            p.setPublicationComments(null);
            p.setPublicationComments(tempComments);

            p.setPublicationFavors(null);

            //p.setPublicationFavors(tempFavors);

            p.setTags(null);
            p.setTags(tempTags);

            //设置该publication的作者 当前用户是否关注了
            Follow tempfollow = followRepository.findByuserIdAndFollowUserId(userId, p.getUser().getId());
            if(tempfollow!=null){
                p.setFollow(tempfollow);
            }

        }

        //最外层Map
        Map<String,Object> map=new HashedMap();

        //publications组装
        List<Publication> publications;
        publications=byAll.getContent();
        Map<String,List<Publication>> listMap=new HashedMap();
        listMap.put("publications",publications);

        //page :组装
        Map<String,Object> pageMap=new HashedMap();
        pageMap.put("number",byAll.getNumber());
        pageMap.put("size",byAll.getSize());
        pageMap.put("totalElements",byAll.getTotalElements());
        pageMap.put("totalPages",byAll.getTotalPages());


        //_links;组装
        Map<String,Map<String,String>>linkMap=new HashedMap();
        Map<String,String>selfMap=new HashedMap();
        selfMap.put("href","");
        linkMap.put("self",selfMap);

        map.put("_embedded",listMap);
        map.put("_links",linkMap);
        map.put("page",pageMap);


        return ResponseEntity.ok(map);

    }


    @RequestMapping("/user")
    public ResponseEntity<Map<String,Object>> del3(@PageableDefault(value = 20, sort = {"createTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                                                   @RequestParam(name = "current_uid")int userId,
                                                   @RequestParam(name="target_uid")int targetUserId) {

        Page<Publication> byAll = publicationRepository.findByUserId(targetUserId,pageable);

        /**
         *   处理.
         */

        for (Publication p : byAll.getContent()) {

            //获取评论总数
            p.setPublicationCommentsTotal(p.getPublicationComments().size());
            //获取点赞总数
            p.setPublicationFavorsTotal(p.getPublicationFavors().size());

            //处理所有评论列表
            List<PublicationComment> tempComments = publicationCommentRepository.findFirst2BypublicationId(p.getId());



            for (PublicationComment c : tempComments) {
                c.setPublication(null);
                PublicationComment tempReply = (PublicationComment) c.getReplyComment();
                System.out.println("!!!!replycomment==" + tempReply);
                   /*
                   !!!!replycomment==null
                   !!!!replycomment==PublicationComment@8953(publication=null,id=1,content="上一条评论内容",replyComment=null)
                   !!!!replycomment==PublicationComment@8998(publication=null,id,content="上一条评论内容",replyComment="上一个评论@8953")
                   !!!!replycomment==PublicationComment@9091(publication=null,   replyComment="@8998 --@8953")
                    if(tempReply!=null){
                       tempReply.setReplyComment(null); // 只有最后一条
                    }
                  */
            }


            //处理点赞列表
            List<PublicationFavor> tempFavors = publicationFavorRepository.findBypublicationId(p.getId());
            for (PublicationFavor f : tempFavors) {
                f.setPublication(null);
                if(f.getUser().getId()==userId){
                    p.setFavor(f);
                }
            }


            //处理 标签列表
            List<Tag> tempTags = tagRepository.findBypublicationId(p.getId());

            for (Tag t : tempTags) {
                t.setPublication(null);
                Topic temp = t.getTopic();
                if (temp!=null) {
                    Topic tempTopic = topicRepository.getOne(temp.getId());
                    tempTopic.setTags(null);
                    t.setTopic(null);
                    t.setTopic(tempTopic);
                }

                System.out.println("!!!!!tag=" + t);
            }

            p.setPublicationComments(null);
            p.setPublicationComments(tempComments);

            p.setPublicationFavors(null);

            //p.setPublicationFavors(tempFavors);

            p.setTags(null);
            p.setTags(tempTags);

            //设置该publication的作者 当前用户是否关注了
            Follow tempfollow = followRepository.findByuserIdAndFollowUserId(userId, p.getUser().getId());
            if(tempfollow!=null){
                p.setFollow(tempfollow);
            }

        }

        //最外层Map
        Map<String,Object> map=new HashedMap();

        //publications组装
        List<Publication> publications;
        publications=byAll.getContent();
        Map<String,List<Publication>> listMap=new HashedMap();
        listMap.put("publications",publications);

        //page :组装
        Map<String,Object> pageMap=new HashedMap();
        pageMap.put("number",byAll.getNumber());
        pageMap.put("size",byAll.getSize());
        pageMap.put("totalElements",byAll.getTotalElements());
        pageMap.put("totalPages",byAll.getTotalPages());


        //_links;组装
        Map<String,Map<String,String>>linkMap=new HashedMap();
        Map<String,String>selfMap=new HashedMap();
        selfMap.put("href","");
        linkMap.put("self",selfMap);

        map.put("_embedded",listMap);
        map.put("_links",linkMap);
        map.put("page",pageMap);


        return ResponseEntity.ok(map);

    }


    //显示一条饭圈详情
    @RequestMapping("/publication")
    public ResponseEntity<Publication> del4(
            @RequestParam(name = "current_uid")int userId,
            @RequestParam(name="target_pid")int publicationId){

        Publication p = publicationRepository.findOneById(publicationId);
        p.setTags(null);

        //获取评论总数
        p.setPublicationCommentsTotal(p.getPublicationComments().size());
        //获取点赞总数
        p.setPublicationFavorsTotal(p.getPublicationFavors().size());


        //处理所有评论列表
        List<PublicationComment> tempComments = publicationCommentRepository.findFirst2BypublicationId(p.getId());



        for (PublicationComment c : tempComments) {
            c.setPublication(null);

        }


        //处理点赞列表
        List<PublicationFavor> tempFavors = publicationFavorRepository.findBypublicationId(p.getId());
        for (PublicationFavor f : tempFavors) {
            f.setPublication(null);
            if(f.getUser().getId()==userId){
                p.setFavor(f);
            }
        }


        //处理 标签列表
        List<Tag> tempTags = tagRepository.findBypublicationId(p.getId());

        for (Tag t : tempTags) {
            t.setPublication(null);
            Topic temp = t.getTopic();
            if (temp!=null) {
                Topic tempTopic = topicRepository.getOne(temp.getId());
                tempTopic.setTags(null);
                t.setTopic(null);
                t.setTopic(tempTopic);
            }


        }

        p.setPublicationComments(null);
       // p.setPublicationComments(tempComments);

        p.setPublicationFavors(null);

        p.setFollow(null);

       // p.setPublicationFavors(tempFavors);





        p.setTags(tempTags);


        //设置该publication的作者 当前用户是否关注了
        Follow tempfollow = followRepository.findByuserIdAndFollowUserId(userId, p.getUser().getId());
        if(tempfollow!=null){
            p.setFollow(tempfollow);
        }


        Map<String,Object> map=new HashedMap();
        map.put("",p);

        return ResponseEntity.ok(p);
    }

    @RequestMapping("/favors")
    public ResponseEntity<Map<String,Object>> del5(@PageableDefault(value = 20, sort = {"createTime"}, direction = Sort.Direction.DESC) Pageable pageable,
                                                   @RequestParam(name = "current_uid")int userId,
                                                   @RequestParam(name="target_pid")int targetPublicationId) {

        //Page<Publication> byAll = publicationRepository.findByUserId(targetUserId,pageable);
        Page<PublicationFavor> byAll = publicationFavorRepository.findByPublicationIdOrderByCreateTimeDesc(targetPublicationId, pageable);
        /**
         *   处理.
         */
        List<PublicationFavor> tempFavor = byAll.getContent();
        for (PublicationFavor p:tempFavor) {
           p.setPublication(null);

            List<Follow> tempFollow = followRepository.findByUserId(userId);
            if(tempFollow.size()>0) {
                for (Follow f:tempFollow) {
                    if(p.getUser().getId()==f.getFollowUser().getId()){
                        f.setUser(null);
                        p.setFollow(f);
                    }
                }
            }
        }


        //最外层Map
        Map<String,Object> map=new HashedMap();

        //publications组装
        //List<Publication> publications;
        //publications=byAll.getContent();
        Map<String,List<PublicationFavor>> listMap=new HashedMap();
        listMap.put("publicationfavors",tempFavor);

        //page :组装
        Map<String,Object> pageMap=new HashedMap();
        pageMap.put("number",byAll.getNumber());
        pageMap.put("size",byAll.getSize());
        pageMap.put("totalElements",byAll.getTotalElements());
        pageMap.put("totalPages",byAll.getTotalPages());


        //_links;组装
        Map<String,Map<String,String>>linkMap=new HashedMap();
        Map<String,String>selfMap=new HashedMap();
        selfMap.put("href","");
        linkMap.put("self",selfMap);

        map.put("_embedded",listMap);
        map.put("_links",linkMap);
        map.put("page",pageMap);


        return ResponseEntity.ok(map);

    }



}
