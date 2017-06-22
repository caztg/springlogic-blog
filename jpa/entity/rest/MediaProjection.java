package cn.springlogic.blog.jpa.entity.rest;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fitcooker.app.serializer.AppDataPreFixSerializer;

/**
 * Created by admin on 2017/4/26.
 */
public interface MediaProjection {
    int getId();
    @JsonSerialize(using = AppDataPreFixSerializer.class)
    String getUri();
    int getRank();
}
