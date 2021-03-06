package com.xiaohe66.web.code.love.mapper;

import com.xiaohe66.web.base.base.IBaseMapper;
import com.xiaohe66.web.code.love.po.PhotoDesc;

import java.util.List;

/**
 * @author xiaohe
 * @time 2019.10.11 17:49
 */
public interface PhotoDescMapper extends IBaseMapper<PhotoDesc> {

    List<PhotoDesc> listByPhotoId(Integer photoId);

}
