package com.xiaohe66.web.code.love.mapper;

import com.xiaohe66.web.base.base.CreateTableMapper;
import com.xiaohe66.web.base.base.IBaseMapper;
import com.xiaohe66.web.code.love.po.Lover;

/**
 * @author xiaohe
 * @time 2020.01.06 16:42
 */
public interface LoverMapper extends IBaseMapper<Lover>, CreateTableMapper {

    Lover selectByLoverUserId(Integer userId);

    Integer selectLoverIdByUserId(Integer userId);
}
