package cn.springlogic.blog.jpa.entity;

import cn.springlogic.user.jpa.entity.User;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by admin on 2017/4/18.
 */
@Data
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    @Column(name = "create_time")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    /** 分类是多的一端 (多个分类对应一个用户)*/
    @ManyToOne(fetch=FetchType.LAZY,  // 指定属性的抓取策略 FetchType.LAZY:延迟加载   FetchType.EAGER:立即加载
            targetEntity=User.class)// 指定关联的持久化类
    /** 生成关联的外键列 */
    @JoinColumn(name="user_id", // 外键列的列名
            referencedColumnName="id") // 指定引用表的主键列
    private User user;

    @ManyToOne(fetch=FetchType.LAZY,  // 指定属性的抓取策略 FetchType.LAZY:延迟加载   FetchType.EAGER:立即加载
            targetEntity=Category.class)// 指定关联的持久化类
    /** 生成关联的外键列 */
    @JoinColumn(name="categoryId_id", // 外键列的列名
            referencedColumnName="id") // 指定引用表的主键列
    private Category categoryId;

}
