package com.xiaohe66.web.text.service;

import com.github.pagehelper.PageHelper;
import com.xiaohe66.web.base.base.impl.AbstractService;
import com.xiaohe66.web.base.data.CodeEnum;
import com.xiaohe66.web.base.data.Final;
import com.xiaohe66.web.base.exception.MsgException;
import com.xiaohe66.web.base.exception.XhException;
import com.xiaohe66.web.base.util.Check;
import com.xiaohe66.web.base.util.ClassUtils;
import com.xiaohe66.web.base.util.HtmlUtils;
import com.xiaohe66.web.base.util.StrUtils;
import com.xiaohe66.web.common.service.CategoryService;
import com.xiaohe66.web.org.helper.UsrHelper;
import com.xiaohe66.web.org.po.Usr;
import com.xiaohe66.web.org.service.UsrService;
import com.xiaohe66.web.sys.helper.SysCfgHelper;
import com.xiaohe66.web.text.dao.ArticleDao;
import com.xiaohe66.web.text.dto.ArticleDto;
import com.xiaohe66.web.text.param.ArticleParam;
import com.xiaohe66.web.text.po.Article;
import com.xiaohe66.web.text.po.ArticleCategoryLink;
import com.xiaohe66.web.text.po.TextCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xiaohe
 * @time 17-11-08 008
 */
@Service
public class ArticleService extends AbstractService<Article>{

    private static final Logger LOG = LoggerFactory.getLogger(ArticleService.class);

    private ArticleDao articleDao;

    @Autowired
    private ArticleCategoryLinkService articleCategoryLinkService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TextCategoryService textCategoryService;

    @Autowired
    private UsrService usrService;

    @Autowired
    private ArticleLogService articleLogService;

    public ArticleService(){}

    @Autowired
    public ArticleService(ArticleDao articleDao){
        super(articleDao);
        this.articleDao = articleDao;
    }

    /**
     * 查询列表
     * @param article 条件
     * @return 匹配的结果
     */
    public List<Article> list(Article article){
        return this.findByParam(article);
    }

    /**
     * 首页显示数据
     * @return
     */
    public List<ArticleDto> indexArticle(){
        PageHelper.startPage(1,5);
        ArticleParam articleParam = new ArticleParam();
        articleParam.setSecretLevel(Final.Article.SECRET_LEVEL_PUBLIC);
        return installDto(findByParam(articleParam));
    }

    /**
     * 禁用该方法，调用该方法会抛出异常，请调用add(p1,p2,p3)
     * @param article 禁用
     * @param currentUsrId 当前操作者id
     */
    @Deprecated
    @Override
    public void updateById(Article article, Long currentUsrId) {
        throw new XhException(CodeEnum.DISABLE_FUNCTION,"pls invoke updateById(p1,p2,p3)");
    }

    /**
     * 更新文章
     * @param article
     * @param perCategoryIds
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateById(Article article,Long[] perCategoryIds) {
        if(Check.isNull(article) || Check.isNull(article.getId())){
            throw new XhException(CodeEnum.NULL_EXCEPTION,"article or id is null");
        }
        Article dbArticle = findById(article.getId());
        if(Check.isNull(dbArticle)){
            throw new XhException(CodeEnum.RESOURCE_NOT_FOUND,"object not found");
        }
        Long currentUsrId = UsrHelper.getCurrentUsrId();
        if(!currentUsrId.equals(dbArticle.getCreateId())){
            throw new XhException(CodeEnum.NOT_PERMISSION,"this article not is current usr article");
        }
        super.updateById(article, currentUsrId);

        //删除个人分类关联
        articleCategoryLinkService.delByArticleId(dbArticle.getId());
        if(Check.isNotEmpty(perCategoryIds)){
            List<ArticleCategoryLink> linkList = new ArrayList<>(perCategoryIds.length);
            for (Long perCategoryId : perCategoryIds) {
                linkList.add(new ArticleCategoryLink(article.getId(),perCategoryId));
            }
            //增加新的个人分类关联
            articleCategoryLinkService.addAll(linkList,currentUsrId);
        }
    }

    /**
     * 禁用该方法，调用该方法会抛出异常，请调用add(p1,p2,p3)
     * @param article 禁用
     * @param currentUsrId 当前操作者id
     */
    @Deprecated
    @Override
    public void add(Article article, Long currentUsrId) {
        throw new XhException(CodeEnum.DISABLE_FUNCTION,"pls invoke add(p1,p2,p3)");
    }

    /**
     * 创建新文章
     * @param article   文章
     * @param perCategoryIds    文章个人分类
     */
    @Transactional(rollbackFor = Exception.class)
    public void add(Article article,Long[] perCategoryIds){

        Check.notEmptyCheck(article.getText(),article.getSysCategoryId());

        Long currentUsrId = UsrHelper.getCurrentUsrId();

        if (article.getSecretLevel() == null) {
            article.setSecretLevel(Final.Article.SECRET_LEVEL_PUBLIC);
        }

        LOG.info("创建一篇文章：title="+article.getTitle()+",usrId="+currentUsrId);
        super.add(article, currentUsrId);

        if(Check.isNotEmpty(perCategoryIds)){
            List<ArticleCategoryLink> linkList = new ArrayList<>();
            for (Long perCategoryId : perCategoryIds) {
                ArticleCategoryLink link = new ArticleCategoryLink(article.getId(),perCategoryId);
                linkList.add(link);
            }
            articleCategoryLinkService.addAll(linkList,currentUsrId);
        }
    }

    /**
     * 查询一个用户的文章
     * @param usrId     待查询的用户id，传入null时默认为站长的id
     * @return      对应用户的文章列表
     */
    public List<Article> findByUsrId(Long usrId,Integer secretLevel){
        if(Check.isOneNull(usrId)){
            //默认显示站长的列表
            String usrIdStr = SysCfgHelper.getString(Final.Str.CFG_KEY_XIAO_HE_USR_ID);
            usrId = StrUtils.toLong(usrIdStr);
        }
        ArticleParam param = new ArticleParam();
        param.setCreateId(usrId);
        param.setSecretLevel(secretLevel);

        return articleDao.findByParam(param);
    }

    public List<ArticleDto> findDtoByUsrId(Long usrId,Integer secretLevel){
        return installDto(findByUsrId(usrId,secretLevel));
    }

    public ArticleDto findDtoById(Long id){
        Check.notEmptyCheck(id);
        Article article = this.findById(id);

        //如果该文章是简历文章，则任何人都可以查看
        if(Final.Article.SECRET_LEVEL_RESUME_ARTICLE != article.getSecretLevel()){

            //只有文章作者是自己 或者 文章的私密等级是公开时，才能查看
            if(Final.Article.SECRET_LEVEL_PUBLIC != article.getSecretLevel()){
                Long currentUsrId = UsrHelper.getCurrentUsrId();
                if(!currentUsrId.equals(article.getCreateId())){
                    throw new MsgException(CodeEnum.NOT_PERMISSION);
                }
            }
        }

        ArticleDto articleDto = ClassUtils.convert(ArticleDto.class,article);

        //保存日志准备
        articleLogService.addPrepare(id);

        installDto(articleDto,article);
        return articleDto;
    }

    public List<ArticleDto> findDtoAll(String search,boolean onlyWebmaster){
        ArticleParam param = new ArticleParam();
        if(onlyWebmaster){
            String usrIdStr = SysCfgHelper.getString(Final.Str.CFG_KEY_XIAO_HE_USR_ID);
            param.setCreateId(StrUtils.toLong(usrIdStr));
        }
        if(StrUtils.isNotEmpty(search)){
            param.setTitle("%"+search+"%");
        }
        param.setSecretLevel(Final.Article.SECRET_LEVEL_PUBLIC);
        return installDto(findByParam(param));
    }

    public void installDto(ArticleDto articleDto,Article article){

        articleDto.setSysCategoryName(categoryService.findById(article.getSysCategoryId()).getCategoryName());
        articleDto.setCount(articleLogService.countByArticleId(article.getId()));

        List<TextCategory> textCategoryList = textCategoryService.findByArticleId(article.getId());

        final int textCategoryListSize = textCategoryList.size();
        if(textCategoryListSize > 0){
            TextCategory textCategory = textCategoryList.get(0);
            StringBuilder perCategoryIds = new StringBuilder(String.valueOf(textCategory.getId()));
            StringBuilder perCategoryNames = new StringBuilder(String.valueOf(textCategory.getCategoryName()));
            for (int i=1;i<textCategoryListSize;i++) {
                textCategory = textCategoryList.get(i);
                perCategoryIds.append(",").append(textCategory.getId());
                perCategoryNames.append("、").append(textCategory.getCategoryName());
            }
            articleDto.setPerCategoryIds(perCategoryIds.toString());
            articleDto.setPerCategoryNames(perCategoryNames.toString());
        }else{
            articleDto.setPerCategoryIds("");
            articleDto.setPerCategoryNames("");
        }
    }

    /**
     * 返回文章列表，文章的内容作摘要处理
     * @param articleList
     * @return
     */
    public List<ArticleDto> installDto(List<Article> articleList){
        return ClassUtils.convertList(ArticleDto.class, articleList, (articleDto, article) -> {
            installDto(articleDto,article);

            articleDto.setText(HtmlUtils.digest(articleDto.getText(),110));

            Usr usr = usrService.findById(article.getCreateId());
            articleDto.setUsrName(usr.getUsrName());
            articleDto.setImgFileId(usr.getImgFileId());
            articleDto.setCount(articleLogService.countByArticleId(article.getId()));
        });
    }

    /**
     * 热门文章top5
     * @param usrId 传入用户id，则查该用户的，否则查全部用户的
     * @return  热门文章top5
     */
    public List<ArticleDto> findDtoHotTop5(Long usrId){
        final int hotSize = 5;
        int i = 0;
        List<ArticleDto> articleDtoList = new ArrayList<>(hotSize);
        List<Map<String,Long>> mapList = articleLogService.countDownloadOfMonth(usrId);
        for (Map<String, Long> map : mapList) {

            ArticleParam param = new ArticleParam();
            param.setSecretLevel(Final.Article.PUBLISH_STATE_NOT_PUBLISH);
            param.setId(map.get("id"));

            List<Article> articleList = findByParam(param);
            if(articleList == null || articleList.size() == 0){
                continue;
            }

            ArticleDto articleDto = ClassUtils.convert(ArticleDto.class,articleList.get(0));
            articleDtoList.add(articleDto);

            articleDto.setCount(map.get("count"));

            if(++i >= hotSize){
                break;
            }
        }
        return articleDtoList;
    }
}