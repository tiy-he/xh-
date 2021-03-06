package com.xiaohe66.web.code.text.controller;

import com.xiaohe66.web.base.annotation.Del;
import com.xiaohe66.web.base.annotation.Get;
import com.xiaohe66.web.base.annotation.Paging;
import com.xiaohe66.web.base.annotation.Post;
import com.xiaohe66.web.base.annotation.Put;
import com.xiaohe66.web.base.annotation.XhController;
import com.xiaohe66.web.base.data.Final;
import com.xiaohe66.web.base.util.ClassUtils;
import com.xiaohe66.web.code.org.helper.UserHelper;
import com.xiaohe66.web.code.text.dto.ArticleDto;
import com.xiaohe66.web.code.text.po.Article;
import com.xiaohe66.web.code.text.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author xiaohe
 * @time 17-11-08 008
 */
//@XhController("/text/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Post
    public Integer add(Article article,@RequestParam(value = "perCategoryIds[]",required=false) Integer[] perCategoryIds){
        articleService.add(article,perCategoryIds);
        return article.getId();
    }

    @Get("/{id}")
    public ArticleDto findById(@PathVariable("id") Integer id){
        Article article = articleService.getById(id);
        return ClassUtils.convert(ArticleDto.class,article);
    }

    @Paging
    @Get("/all/{onlyWebmaster}")
    public List<ArticleDto> all(@PathVariable("onlyWebmaster") boolean onlyWebmaster){
        return articleService.findDtoAll(null,onlyWebmaster);
    }

    @Paging
    @Get("/all/{onlyWebmaster}/{search}")
    public List<ArticleDto> all2(@PathVariable("onlyWebmaster") boolean onlyWebmaster,@PathVariable("search") String search){
        return articleService.findDtoAll(search,onlyWebmaster);
    }

    @Paging
    @Get("/usr")
    public List<ArticleDto> list(){
        return list2(null);
    }

    @Paging
    @Get("/usr/{lookUsrId}")
    public List<ArticleDto> list2(@PathVariable("lookUsrId") Integer lookUsrId){
        return articleService.findDtoByUsrId(lookUsrId,Final.Article.SECRET_LEVEL_PUBLIC);
    }

    @Paging
    @Get("/admin")
    public List<ArticleDto> admin(){
        return admin2(null);
    }

    @Paging
    @Get("/admin/{secretLevel}")
    public List<ArticleDto> admin2(@PathVariable("secretLevel") Integer secretLevel){
        return articleService.findDtoByUsrId(UserHelper.getCurrentUsrId(),secretLevel);
    }

    @Put
    public Integer update(Article article,@RequestParam(value = "perCategoryIds[]",required=false) Integer[] perCategoryIds){
        articleService.updateById(article,perCategoryIds);
        return article.getId();
    }

    @Del("/{id}")
    public void delete(@PathVariable("id") Integer id){
        articleService.removeById(id);
    }
}
