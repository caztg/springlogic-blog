package cn.springlogic.blog.jpa.entity.rest;

import cn.springlogic.user.jpa.entity.User;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fitcooker.app.serializer.AppDataPreFixSerializer;
import org.springframework.data.rest.core.config.Projection;

/**
 * Created by admin on 2017/4/26.
 */
@Projection(name = "userinfo",types = {User.class})
public interface UserProjection {
    int getId();
    String getNickName();
    @JsonSerialize(using = AppDataPreFixSerializer.class)
    String getAvatar();
    String getEmail();
    String getStatus();
    String getPhone();
    String getUsername();
}
