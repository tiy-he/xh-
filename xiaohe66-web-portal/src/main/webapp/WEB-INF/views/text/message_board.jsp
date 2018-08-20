<%--
  Created by IntelliJ IDEA.
  User: xh
  Date: 18-03-15 015
  Time: 10:02 上午
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="shiro" uri="http://shiro.apache.org/tags" %>
<link type="text/css" rel="stylesheet" href="/css/xh/xh-paging.css"/>
<link type="text/css" rel="stylesheet" href="/web/text/css/message_board.css"/>
<script type="text/javascript" src="/js/xh/xh-paging.js"></script>
<script type="text/javascript" src="/web/text/js/message_board.js"></script>
<input type="hidden" id="usrId" value="${usr.id}">
<input type="hidden" id="size" value="${pageInfo.pages}">
<shiro:guest>
<div class="msg border1">
    <p class="title">发表留言</p>
    <p class="hint"><a href="javascript:showLogin();">登录</a>后才能留言</p>
</div>
</shiro:guest>
<shiro:authenticated>
<div class="msg border1">
    <p class="title">发表留言</p>
    <div class="edit">
        <textarea placeholder="你在本站遇到的bug，或你想对站长说的话"></textarea>
    </div>
    <a class="btn">发表留言</a>
</div>
</shiro:authenticated>
<div class="content">
    <c:if test="${pageInfo.pages==0}">
        <div class="item">
            <p style="text-align: center">
                <img style="vertical-align: middle" src="/icon/grieved.png">
                <span style="vertical-align: middle;display: inline-block;margin-left:
                30px;font-size: 30px;line-height: 64px;">暂无留言</span>
            </p>
        </div>
    </c:if>
    <c:forEach items="${pageInfo.list}" var="item" varStatus="statu">
        <div class="item">
            <div>
                <div class="fl">
                    <img class="head_img" src="/org/usr/file/img/${item.imgFileId}" alt="头像">
                </div>
                <div class="u_r">
                    <div class="u_r_d">
                        <span>${item.usrName}</span>
                        <span>${item.createTime}</span>
                        <span>${item.id}楼</span>
                    </div>
                    <div class="desc">${item.msg}</div>
                </div>
            </div>
        </div>
    </c:forEach>
</div>
<div id="paging"></div>