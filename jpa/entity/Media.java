package cn.springlogic.blog.jpa.entity;

import cn.springlogic.oss.jpa.entity.File;
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
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    /**
     * 1图片
       2音频
       3视频
       4文件
     */
    private int type;

    private String title;

    private String uri;

    /**
     * 用于排序
     */
    private int rank;

    @Column(name = "create_time")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @ManyToOne(fetch=FetchType.LAZY,  // 指定user属性的抓取策略 FetchType.LAZY:延迟加载   FetchType.EAGER:立即加载
            targetEntity=User.class)// 指定关联的持久化类
    /** 生成关联的外键列 */
    @JoinColumn(name="user_id", // 外键列的列名
            referencedColumnName="id") // 指定引用user表的主键列
    private User user;

    /**该File类为 oss包下的File类*/
    @ManyToOne(fetch=FetchType.LAZY,  // 指定属性的抓取策略 FetchType.LAZY:延迟加载   FetchType.EAGER:立即加载
            targetEntity=File.class)// 指定关联的持久化类
    /** 生成关联的外键列 */
    @JoinColumn(name="oss_file_id", // 外键列的列名
            referencedColumnName="id") // 指定引用表的主键列
    private File ossFile;

}
