package com.xiaohe66.web.base.base;

import com.xiaohe66.web.base.annotation.Del;
import com.xiaohe66.web.base.annotation.Get;
import com.xiaohe66.web.base.annotation.Post;
import com.xiaohe66.web.base.annotation.Put;
import com.xiaohe66.web.base.base.impl.AbstractService;
import com.xiaohe66.web.base.data.Result;
import com.xiaohe66.web.base.exception.NotPermittedException;
import com.xiaohe66.web.base.util.ClassUtils;
import com.xiaohe66.web.code.org.helper.UsrHelper;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author xiaohe
 * @time 2019.10.12 11:34
 */
public abstract class BaseController<S extends AbstractService<? extends IBaseMapper, T>, T extends BasePo, D extends BaseDto> {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    protected S baseService;

    protected final String moduleName;

    protected final Class<D> dtoClass;

    @SuppressWarnings("unchecked")
    public BaseController() {
        Type[] type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();

        Class<T> poClass = ((Class<T>) type[1]);
        String poClassName = poClass.getSimpleName();
        char[] chars = poClassName.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        moduleName = new String(chars);

        logger.info("moduleName : {}", moduleName);

        dtoClass = ((Class<D>) type[2]);
        logger.info("dtoClass : {}", dtoClass.getName());
    }

    @Post
    public Result post(T po) {
        checkSavePermitted();
        if (po instanceof BasePoDetailed) {
            Integer currentUsrId = UsrHelper.getCurrentUsrId();
            ((BasePoDetailed) po).setCreateId(currentUsrId);
        }
        return Result.ok(baseService.save(po));
    }

    @Del("/{id}")
    public Result del(@PathVariable("id") Integer id) {
        checkDeletePermitted();
        return Result.ok(baseService.removeById(id));
    }

    @Put
    public Result put(T po) {
        checkUpdatePermitted();
        if (po instanceof BasePoDetailed) {
            Integer currentUsrId = UsrHelper.getCurrentUsrId();
            ((BasePoDetailed) po).setUpdateId(currentUsrId);
        }
        return Result.ok(baseService.updateById(po));
    }

    @Get("/{id}")
    public Result get(@PathVariable("id") Integer id) {
        checkSelectPermitted();
        T po = baseService.getById(id);
        D dto = ClassUtils.convert(dtoClass, po);
        convertTask(dto, po);
        return Result.ok(dto);
    }

    // todo : 改成分页查询
    @Get
    public Result list() {
        checkSelectPermitted();
        checkPermitted(moduleName + ":select");
        List<T> poList = baseService.list();
        List<D> dtoList = ClassUtils.convertList(dtoClass, poList, this::convertTask);
        return Result.ok(dtoList);
    }

    protected void checkSavePermitted(){
        checkPermitted(moduleName+":insert");
    }

    protected void checkDeletePermitted(){
        checkPermitted(moduleName+":delete");
    }

    protected void checkUpdatePermitted(){
        checkPermitted(moduleName+":update");
    }

    protected void checkSelectPermitted(){
        checkPermitted(moduleName+":select");
    }

    protected final void checkPermitted(String permittedName) {
        if (!SecurityUtils.getSubject().isPermittedAll(permittedName)) {
            throw new NotPermittedException(permittedName);
        }
    }

    protected void convertTask(D dto, T po) {

    }
}
