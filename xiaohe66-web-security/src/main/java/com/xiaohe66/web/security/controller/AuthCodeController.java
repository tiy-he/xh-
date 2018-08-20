package com.xiaohe66.web.security.controller;

import com.xiaohe66.web.base.annotation.Get;
import com.xiaohe66.web.base.annotation.Page;
import com.xiaohe66.web.base.annotation.XhController;
import com.xiaohe66.web.base.data.CodeEnum;
import com.xiaohe66.web.base.data.ParamFinal;
import com.xiaohe66.web.base.exception.XhException;
import com.xiaohe66.web.base.util.StrUtils;
import com.xiaohe66.web.security.auth.AuthCode;
import com.xiaohe66.web.security.auth.AuthCodeHelper;
import org.springframework.web.bind.annotation.PathVariable;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author xh
 * @date 18-03-11 011
 */
@XhController("/authCode")
public class AuthCodeController {

    @Page("/img")
    public void code(HttpServletResponse response) throws IOException {

        AuthCode authCode = AuthCodeHelper.createAuthCode();

        response.setContentType(ParamFinal.CONTENT_TYPE_IMAGE_PNG);
        OutputStream os = response.getOutputStream();

        ImageIO.write(authCode.getImg(), ParamFinal.FILE_TYPE_PNG, os);
    }

    @Get("/{code}")
    public Boolean authCode(@PathVariable("code") String code){
        if(StrUtils.isEmpty(code)){
            throw new XhException(CodeEnum.NULL_EXCEPTION,"code is null");
        }

        return AuthCodeHelper.verifyNotClearSession(code);
    }

}