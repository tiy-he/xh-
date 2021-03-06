package com.xiaohe66.web.code.common.controller;

import com.xiaohe66.web.base.annotation.Del;
import com.xiaohe66.web.base.annotation.Get;
import com.xiaohe66.web.base.annotation.Post;
import com.xiaohe66.web.base.annotation.Put;
import com.xiaohe66.web.base.annotation.XhController;
import com.xiaohe66.web.base.util.ClassUtils;
import com.xiaohe66.web.code.common.dto.CategoryDto;
import com.xiaohe66.web.code.common.po.Category;
import com.xiaohe66.web.code.common.service.CategoryService;
import com.xiaohe66.web.code.org.helper.UserHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author xh
 * @date 18-03-07 007
 */
//@XhController("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Get("/{id}")
    public CategoryDto get(@PathVariable("id") Integer id){
        Category category = categoryService.getById(id);
        return ClassUtils.convert(CategoryDto.class,category);
    }

    @Post
    public Integer add(Category category){
        category.setCreateId(UserHelper.getCurrentUsrId());
        categoryService.save(category);
        return category.getId();
    }

    @Put
    public void update(Category category){
        category.setUpdateId(UserHelper.getCurrentUsrId());
        categoryService.updateById(category);
    }

    @Del("/{id}")
    public void del(@PathVariable("id")Integer id){
        categoryService.removeById(id);
    }

    @Get("/p/{pid}")
    public List<CategoryDto> getByPid(@PathVariable("pid")Integer pid){
        List<Category> categoryList = categoryService.findByPid(pid);
        return ClassUtils.convert(CategoryDto.class,categoryList);
    }
}
