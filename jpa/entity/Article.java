package cn.springlogic.blog.jpa.entity;

import cn.springlogic.collection.jpa.entity.Favor;
import cn.springlogic.user.jpa.entity.User;
import lombok.Data;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import java.util.*;

/**
 * Created by admin on 2017/4/18.
 */
@Data
@Entity
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String title;

    private String summary;

    private String content;

    @Column(name = "create_time")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @ManyToOne(fetch=FetchType.EAGER,  // 指定user属性的抓取策略 FetchType.LAZY:延迟加载   FetchType.EAGER:立即加载
            targetEntity=User.class)// 指定关联的持久化类
    /** 生成关联的外键列 */
    @JoinColumn(name="user_id", // 外键列的列名
            referencedColumnName="id") // 指定引用user表的主键列
    private User user;


    @ManyToMany
    @JoinTable(name = "article_category",
            joinColumns = {@JoinColumn(name = "article_id",referencedColumnName = "id")},//JoinColumns定义本方在中间表的主键映射
            inverseJoinColumns = {@JoinColumn(name = "category_id",referencedColumnName = "id")})//inverseJoinColumns定义另一在中间表的主键映射
    private Set<Category> categories=new HashSet<>();

    @ManyToMany//(cascade = {CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REMOVE, CascadeType.REFRESH})
    @Cascade(org.hibernate.annotations.CascadeType.PERSIST)
    @JoinTable(name = "article_media",
            joinColumns = {@JoinColumn(name = "article_id",referencedColumnName = "id")},//JoinColumns定义本方在中间表的主键映射
            inverseJoinColumns = {@JoinColumn(name = "media_id",referencedColumnName = "id")})//inverseJoinColumns定义另一在中间表的主键映射
    private List<Media> medias=new ArrayList<>();



}
