package com.xiaohe66.web.org.service;

import com.github.pagehelper.PageHelper;
import com.xiaohe66.web.comm.po.CommonFile;
import com.xiaohe66.web.comm.service.CommonFileService;
import com.xiaohe66.web.common.base.impl.AbstractService;
import com.xiaohe66.web.common.data.CodeEnum;
import com.xiaohe66.web.common.data.StrEnum;
import com.xiaohe66.web.common.exception.XhException;
import com.xiaohe66.web.common.util.Check;
import com.xiaohe66.web.common.util.ClassUtils;
import com.xiaohe66.web.common.util.EncoderUtils;
import com.xiaohe66.web.common.util.StrUtils;
import com.xiaohe66.web.org.dao.UsrFileDao;
import com.xiaohe66.web.org.dto.UsrFileDto;
import com.xiaohe66.web.org.param.UsrFileParam;
import com.xiaohe66.web.org.po.UsrFile;
import com.xiaohe66.web.org.po.UsrFileLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author xh
 * @date 18-03-12 012
 */
@Service
public class UsrFileService extends AbstractService<UsrFile>{

    private static final Logger LOG = LoggerFactory.getLogger(UsrFileService.class);

    private static final Set<String> IMG_TYPE_SET = new HashSet<>(Arrays.asList(".png",".jpg",".jpeg",".bmp"));

    private static final char[] FILE_ILLEGAL_CHARS = new char[]{'?','\\','/','*','\"',':','<','>','|'};

    private static final int DEFAULT_FILE_TYPE = 0;

    private static final int USR_HEAD_IMG_FILE_TYPE = 1;

    private UsrFileDao usrFileDao;

    @Autowired
    private CommonFileService commonFileService;

    @Autowired
    private UsrFileLogService usrFileLogService;

    public UsrFileService() {}

    @Autowired
    public UsrFileService(UsrFileDao usrFileDao) {
        super(usrFileDao);
        this.usrFileDao = usrFileDao;
    }

    public UsrFileDto uploadUsrFile(MultipartFile file,String md5,Long currentUsrId){
        return uploadFile(file,md5,currentUsrId,DEFAULT_FILE_TYPE);
    }
    public UsrFileDto uploadHeadImgFile(MultipartFile file,String md5,Long currentUsrId){
        return uploadFile(file,md5,currentUsrId,USR_HEAD_IMG_FILE_TYPE);
    }

    /**
     * 上传用户文件，返回commonFile的id
     * @param file  文件
     * @param md5   文件的md5摘要
     * @param currentUsrId  当前操作用户
     * @return 返回commonFile的id
     */
    public UsrFileDto uploadFile(MultipartFile file,String md5,Long currentUsrId,Integer fileType){
        if(Check.isNull(file)){
            throw new XhException(CodeEnum.NULL_EXCEPTION,"file is null");
        }
        if(Check.isNull(currentUsrId)){
            throw new XhException(CodeEnum.NOT_LOGGED_IN,"not login");
        }
        if(StrUtils.isEmpty(md5)){
            throw new XhException(CodeEnum.NULL_EXCEPTION,"md5 is empty");
        }
        if(Check.isNull(fileType)){
            throw new XhException(CodeEnum.NULL_EXCEPTION,"fileType is null");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new XhException(CodeEnum.IO_EXCEPTION);
        }
        //上传文件，如果已存在相同的，则返回存在的id，否则上传后再返回id
        CommonFile commonFile = commonFileService.uploadFile(bytes,md5);

        String name = file.getOriginalFilename();
        int dotIndex = name.lastIndexOf(".");
        String fileName,extension;
        if(dotIndex != -1){
            fileName = name.substring(0,dotIndex);
            extension = name.substring(dotIndex);
        }else{
            fileName = name;
            extension = "";
        }
        //文件名不可以超过20字符
        fileName = fileNameFormat(fileName);

        UsrFile usrFile = new UsrFile();
        usrFile.setFileId(commonFile.getId());
        usrFile.setFileName(fileName);
        usrFile.setExtension(extension);
        usrFile.setFileType(fileType);
        this.add(usrFile,currentUsrId);
        UsrFileDto usrFileDto = ClassUtils.convert(UsrFileDto.class,usrFile);
        usrFileDto.setFileSize(commonFile.getFileByte()+"字节");

        UsrFileLog usrFileLog = new UsrFileLog(usrFile.getId());
        usrFileLogService.add(usrFileLog,currentUsrId);
        return usrFileDto;
    }

    public UsrFile findByCommonFileId(Long commonFileId){
        if(Check.isOneNull(commonFileId)){
            throw new XhException(CodeEnum.NULL_EXCEPTION,"commonFileId is null");
        }
        return usrFileDao.findByCommonFileId(commonFileId);
    }

    public List<UsrFileDto> findDtoByUsrId(Long usrId){
        if(Check.isOneNull(usrId)){
            throw new XhException(CodeEnum.NULL_EXCEPTION);
        }
        UsrFileParam param = new UsrFileParam();
        param.setCreateId(usrId);

        List<UsrFile> usrFileList = this.findByParam(param);

        return ClassUtils.convertList(UsrFileDto.class,usrFileList,(usrFileDto,usrFile)->{
            Integer size = commonFileService.findById(usrFile.getFileId()).getFileByte();

            //todo:需转成可视化单位
            usrFileDto.setFileSize(size+"字节");
        });
    }

    public List<UsrFileDto> findDtoHotTop5(Long usrId){
        List<Map<String,Long>> mapList = usrFileLogService.countDownloadOfMonth(usrId);
        int i = 0;
        final int maxSize = 5;
        List<UsrFileDto> usrFileDtoList = new ArrayList<>(maxSize);
        for (Map<String, Long> map : mapList) {
            UsrFile usrFile = this.findById(map.get("id"));
            if(usrFile == null){
                continue;
            }
            UsrFileDto usrFileDto = ClassUtils.convert(UsrFileDto.class,usrFile);
            usrFileDtoList.add(usrFileDto);
            usrFileDto.setDownloadCount(map.get("count"));
            if(++i >= maxSize){
                break;
            }
        }
        return usrFileDtoList;
    }

    public void showImg(HttpServletResponse response,Long usrFileId){
        if(usrFileId == null){
            throw new XhException(CodeEnum.NULL_EXCEPTION,"usrFileId is null");
        }

        UsrFile usrFile = findById(usrFileId);
        if(usrFile == null){
            throw new XhException(CodeEnum.RESOURCE_NOT_FOUND);
        }
        String extension = usrFile.getExtension();

        //不是图片类型，不返回
        if(!IMG_TYPE_SET.contains(extension)){
            throw new XhException(CodeEnum.IMAGE_FORMAT_EXCEPTION);
        }

        response.setContentType(StrEnum.CONTENT_TYPE_IMAGE_PNG.data());
        try {
            commonFileService.outputFile(usrFile.getFileId(),response.getOutputStream());
        } catch (IOException e) {
            throw new XhException(CodeEnum.IO_EXCEPTION);
        }
    }

    /**
     * 下载文件
     * todo:需要控制不公开文件的下载权限
     *
     * @param response HttpServletResponse
     * @param usrFileId 用户文件id
     */
    public void downloadFile(HttpServletResponse response,Long usrFileId,Long currentUsrId){
        if(usrFileId == null){
            throw new XhException(CodeEnum.NULL_EXCEPTION,"usrFileId is null");
        }
        if(currentUsrId == null){
            throw new XhException(CodeEnum.NULL_EXCEPTION,"currentUsrId is null");
        }

        UsrFile usrFile = findById(usrFileId);
        if(usrFile == null){
            throw new XhException(CodeEnum.RESOURCE_NOT_FOUND);
        }

        //记录下载日志
        usrFileLogService.add(new UsrFileLog(usrFileId),currentUsrId);

        String name = EncoderUtils.urlEncoder(usrFile.getFileName())+usrFile.getExtension();

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition","attachment; filename="+name);
        try {
            commonFileService.outputFile(usrFile.getFileId(),response.getOutputStream());
        } catch (IOException e) {
            throw new XhException(CodeEnum.IO_EXCEPTION);
        }
    }

    public void updateNameById(Long fileId,String fileName,Long currentUsrId){
        fileName = fileNameFormat(fileName);
        Check.notEmptyCheck(fileId,fileName,currentUsrId);
        updateById(new UsrFile(fileId,fileName),currentUsrId);
    }

    /**
     * 文件名格式化
     * 超过20个字符时，截取前20个字符。
     * 此外，如果出现非法字符，则会抛出异常
     * @param fileName 文件名
     * @return  格式化后的文件名
     */
    protected  String fileNameFormat(String fileName){
        fileName = StrUtils.trim(fileName);
        Check.notEmptyCheck(fileName);
        for (char fileIllegalChar : FILE_ILLEGAL_CHARS) {
            if (fileName.contains(String.valueOf(fileIllegalChar))) {
                throw new XhException(CodeEnum.ILLEGAL_CHAR_EXCEPTION);
            }
        }
        //文件名字符长度不能超过20
        return fileName.length() > 20 ? fileName.substring(0,20):fileName;
    }

}
