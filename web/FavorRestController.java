package cn.springlogic.blog.web;

import cn.springlogic.collection.jpa.repository.FavorRepository;
import cn.springlogic.social.jpa.entity.Follow;
import cn.springlogic.social.jpa.entity.Publication;
import cn.springlogic.social.jpa.entity.PublicationFavor;
import cn.springlogic.social.jpa.repository.FollowRepository;
import cn.springlogic.social.jpa.repository.PublicationFavorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
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

import java.util.List;

/**
 * Created by admin on 2017/5/2.
 */
@RepositoryRestController
@RequestMapping(value = "social:publicationfavor")
public class FavorRestController {

    @Autowired
    private PublicationFavorRepository publicationFavorRepository;

    @Autowired
    private PagedResourcesAssembler pagedResourcesAssembler;

    @Autowired
    private FollowRepository followRepository;

    /**
     * 一条饭圈下的点赞记录
     *
     * @param userId
     * @param pageable
     * @param resourceAssembler
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/search/favors")
    public ResponseEntity<PagedResources<PersistentEntityResource>> customSearchBySubject4(
            @RequestParam(name = "user_id",required = false) Integer userId,
            @RequestParam(name = "target_pid") Integer targetPublicationId,
            Pageable pageable,
            PersistentEntityResourceAssembler resourceAssembler) {

        Page<PublicationFavor> page = publicationFavorRepository.findByPublicationId(targetPublicationId, pageable);

        Page<PublicationFavor> customDishesPage = page.map(new FavorRestController.CustomDishesConverter3(followRepository, userId));

        return ResponseEntity.ok(pagedResourcesAssembler.toResource(customDishesPage, resourceAssembler));
    }


    /**
     * 转换器类
     */
    private static final class CustomDishesConverter3 implements Converter<PublicationFavor, PublicationFavor> {

        private final Integer currentUserId;

        private final FollowRepository followRepository;

        private CustomDishesConverter3(FollowRepository followRepository, Integer currentUserId) {

            this.currentUserId = currentUserId;

            this.followRepository=followRepository;
        }

        @Override
        public PublicationFavor convert(PublicationFavor source) {

            // 设置当前点赞状态
            if (null != currentUserId) {
                //设置关注状态
                Follow tempFollow = followRepository.findByuserIdAndFollowUserId(currentUserId, source.getUser().getId());
                if(tempFollow!=null) {
                    source.setPublication(null);
                    source.setFollow(tempFollow);
                }
            }


            return source;
        }
    }
}