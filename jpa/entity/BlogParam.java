package cn.springlogic.blog.jpa.entity;

import cn.springlogic.social.jpa.entity.Tag;
import lombok.Data;

import java.util.List;

/**
 *
 * Created by admin on 2017/4/24.
 */
@Data
public class BlogParam {

    private Article article;

    //private List<Topic> topics;
    private List<Tag> tags;

}
