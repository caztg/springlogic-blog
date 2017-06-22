package cn.springlogic.blog.web;

import cn.springlogic.social.jpa.entity.Follow;
import cn.springlogic.social.jpa.entity.Publication;
import cn.springlogic.social.jpa.entity.PublicationFavor;
import cn.springlogic.social.jpa.repository.FollowRepository;
import cn.springlogic.social.jpa.repository.PublicationFavorRepository;
import cn.springlogic.social.jpa.repository.PublicationRepository;
import cn.springlogic.social.util.SortListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResource;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by admin on 2017/5/2.
 */
@RepositoryRestController
@RequestMapping(value = "social:publication")
public class BlogRestController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private PagedResourcesAssembler pagedResourcesAssembler;

    @Autowired
    private PublicationFavorRepository publicationFavorRepository;

    @Autowired
    private FollowRepository followRepository;

    /**
     * 所有饭圈
     *
     * @param topic
     * @param userId
     * @param pageable
     * @param resourceAssembler
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/search/all")
    public ResponseEntity<PagedResources<PersistentEntityResource>> publicationSearchByTopic(@RequestParam(name = "topic", required = false) String topic,
                                                                                             @RequestParam(name = "user_id", required = false) Integer userId,
                                                                                             Pageable pageable,
                                                                                             PersistentEntityResourceAssembler resourceAssembler) {
        //查询出所有的饭圈列表
        Page<Publication> page = null;
        page = publicationRepository.findByAllNoTopic(pageable);
        if (StringUtils.isNotBlank(topic)) {
            page = publicationRepository.findByAll(topic, pageable);
        }


        //通过工具类转化器组装
        Page<Publication> publicationPage = page.map(new PublicationsConverter(publicationFavorRepository, followRepository, userId));


        return ResponseEntity.ok(pagedResourcesAssembler.toResource(publicationPage, resourceAssembler));
    }

    /**
     * 关注列表饭圈
     *
     * @param topic
     * @param userId            当前用户id
     * @param pageable
     * @param resourceAssembler
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/search/follow")
    public ResponseEntity<PagedResources<PersistentEntityResource>> publicationByFollowUser(@RequestParam(name = "topic", required = false) String topic,
                                                                                            @RequestParam(name = "user_id", required = false) Integer userId,
                                                                                            Pageable pageable,
                                                                                            PersistentEntityResourceAssembler resourceAssembler) {
        Page<Publication> page = null;
        page = publicationRepository.findAllByFollow(userId, pageable);
        if (StringUtils.isNotBlank(topic)) {
            page = publicationRepository.findByFollow(topic, userId, pageable);
        }
        Page<Publication> publicationPage = page.map(new PublicationsConverter(publicationFavorRepository, followRepository, userId));

        return ResponseEntity.ok(pagedResourcesAssembler.toResource(publicationPage, resourceAssembler));
    }

    /**
     * 读取一个用户的所有饭圈
     *
     * @param topic
     * @param userId            当前用户id
     * @param targetUserId      目标用户id
     * @param pageable
     * @param resourceAssembler
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/search/user")
    public ResponseEntity<PagedResources<PersistentEntityResource>> customSearchBySubject3(@RequestParam(name = "topic", required = false) String topic,
                                                                                           @RequestParam(name = "user_id", required = false) Integer userId,
                                                                                           @RequestParam(name = "target_uid") Integer targetUserId,
                                                                                           Pageable pageable,
                                                                                           PersistentEntityResourceAssembler resourceAssembler) {

        Page<Publication> page = publicationRepository.findByUserId(targetUserId, pageable);

        Page<Publication> publicationPage = page.map(new PublicationsConverter(publicationFavorRepository, followRepository, userId));

        return ResponseEntity.ok(pagedResourcesAssembler.toResource(publicationPage, resourceAssembler));
    }

    /**
     * 读取一条饭圈
     *
     * @param userId              当前用户id
     * @param targetPublicationId
     * @param resourceAssembler
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/search/one")
    public ResponseEntity<PersistentEntityResource> customSearchBySubject4(
            @RequestParam(name = "user_id", required = false) Integer userId,
            @RequestParam(name = "target_pid") Integer targetPublicationId,
            PersistentEntityResourceAssembler resourceAssembler) {

        Publication publication = publicationRepository.findOneById2(targetPublicationId);


        Converter<Publication, Publication> converter = new PublicationsConverter(publicationFavorRepository, followRepository, userId);
        publication = converter.convert(publication);

        return ResponseEntity.ok(resourceAssembler.toResource(publication));
    }


    /**
     * 转换器类
     */
    private static final class PublicationsConverter implements Converter<Publication, Publication> {

        private final Integer currentUserId;

        private final PublicationFavorRepository publicationFavorRepository;

        private final FollowRepository followRepository;

        private PublicationsConverter(PublicationFavorRepository publicationFavorRepository, FollowRepository followRepository, Integer currentUserId) {

            this.currentUserId = currentUserId;
            this.publicationFavorRepository = publicationFavorRepository;
            this.followRepository = followRepository;
        }

        @Override
        public Publication convert(Publication source) {
            source.setPublicationFavorsTotal(source.getPublicationFavors().size());
            source.setPublicationCommentsTotal(source.getPublicationComments().size());


            if (null != currentUserId) {
                //处理点赞状态
                PublicationFavor tempFavor = publicationFavorRepository.findByPublicationIdAndUserId(source.getId(), currentUserId);
                if (tempFavor != null) {
                    source.setFavor(tempFavor);
                }
                //设置关注状态
                Follow tempFollow = followRepository.findByuserIdAndFollowUserId(currentUserId, source.getUser().getId());
                if (tempFollow != null) {
                    source.setFollow(tempFollow);
                }
            }


            return source;
        }
    }

    /**
     * 后台 饭圈列表
     * 可根据用户名/手机号码/活动标签/内容关键字过滤,可根据创建时间/点赞数量/评论数量排序
     *
     * @param topic
     * @param pageable
     * @param resourceAssembler
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/search/all/admin")
    public ResponseEntity<PagedResources<PersistentEntityResource>> adminPublicationSearchByTopic(@RequestParam(name = "topic", required = false) String topic,
                                                                                                  @RequestParam(name = "nick_name", required = false, defaultValue = "") String nickName,
                                                                                                  @RequestParam(name = "phone", required = false, defaultValue = "") String phone,
                                                                                                  @RequestParam(name = "content", required = false, defaultValue = "") String content,
                                                                                                  @RequestParam(name = "time_sort", required = false) String time_sort,
                                                                                                  @RequestParam(name = "comment_sort", required = false) String comment_sort,
                                                                                                  @RequestParam(name = "favor_sort", required = false) String favor_sort,
                                                                                                  Pageable pageable,
                                                                                                  PersistentEntityResourceAssembler resourceAssembler) {
        Page<Publication> page = null;

        if (StringUtils.isNotBlank(topic)) {

            if ("ASC".equalsIgnoreCase(time_sort)) {
                //page =  publicationRepository.findByAllAdminASC(topic, nickName ,pageable);
                page = publicationRepository.findByAllAdminASC(topic, nickName, phone, content, pageable);
            }else {
                page = publicationRepository.findByAllAdmin(topic, nickName, phone, content, pageable);
            }


        }else {

            if ("ASC".equalsIgnoreCase(time_sort)) {
                page = publicationRepository.findNoTopicByAllAdminASC(nickName, phone, content, pageable);
            }else {
                page = publicationRepository.findNoTopicByAllAdmin(nickName, phone, content, pageable);
            }
        }

        //通过工具类转化器组装 评论数跟点赞数
        Page<Publication> publicationPage = page.map(new adminPublicationsConverter());


        /*根据评论数或者点赞数排序处理
           不可以直接对 getContent()拿出来的 list集合进行处理
           UnsupportedOperationException. 其实List结构按是否可修改也是可以在分为两个类型的
           这里拿到的是不可修改类型,所以只能clone一个同样的list集合
         */
        List<Publication> tempList = publicationPage.getContent();
        //复制一个list
        List<Publication> t = new ArrayList<Publication>(tempList);


        //评论总数排序
        if (comment_sort != null) {
            Collections.sort(t, new SortListUtils<Publication>("getPublicationCommentsTotal", SortListUtils.DESC));

            if ("ASC".equalsIgnoreCase(comment_sort)) {
                Collections.sort(t, new SortListUtils<Publication>("getPublicationCommentsTotal", SortListUtils.ASC));
            }
        }
        //点赞总数排序
        if (favor_sort != null) {
            Collections.sort(t, new SortListUtils<Publication>("getPublicationFavorsTotal", SortListUtils.DESC));


            if ("ASC".equalsIgnoreCase(favor_sort)) {
                Collections.sort(t, new SortListUtils<Publication>("getPublicationFavorsTotal", SortListUtils.ASC));
            }
        }

        Page<Publication> p = new PageImpl<Publication>(t, pageable, tempList.size());

        return ResponseEntity.ok(pagedResourcesAssembler.toResource(p, resourceAssembler));
    }

    /**
     * 后台转换器类
     */
    private static final class adminPublicationsConverter implements Converter<Publication, Publication> {


        @Override
        public Publication convert(Publication source) {
            source.setPublicationFavorsTotal(source.getPublicationFavors().size());
            source.setPublicationCommentsTotal(source.getPublicationComments().size());


            return source;
        }
    }


}


