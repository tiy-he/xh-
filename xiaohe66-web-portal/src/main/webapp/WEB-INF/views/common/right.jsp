<%--
  Created by IntelliJ IDEA.
  User: xh
  Date: 18-04-02 002
  Time: 23:11 下午
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <jsp:include page="/WEB-INF/views/common/init.jsp"></jsp:include>
    <link type="text/css" rel="stylesheet" href="/web/common/css/right.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/top.jsp"></jsp:include>
<div class="c">
    <div class="l">
        <jsp:include page="/WEB-INF/views/${page}.jsp"></jsp:include>
    </div>
    <div class="r">
        <div class="module1">
            <div class="title">
                <c:if test="${empty usrDivTitle}">作者</c:if>
                <c:if test="${not empty usrDivTitle}">${usrDivTitle}</c:if>
            </div>
            <div class="body master">
                <img src="/org/usr/file/img/${usrDto.imgFileId}" class="head_img" alt="${usrDto.usrName}"
                     onclick="location.href = '/text/article/list/${usrDto.id}'">
                <p>${usrDto.usrName}</p>
                <p>${usrDto.signature}</p>
            </div>
        </div>
        <div class="module1">
            <div class="title">友情链接</div>
            <div class="body link">
                <a href="http://www.github.com/tiy-he" target="_blank">小何的GitHub</a>
                <a href="https://blog.csdn.net/xiaohe73" target="_blank">小何的Csdn</a>
            </div>
        </div>
        <div class="module1">
            <div class="title">
                <c:if test="${empty usrDivTitle}">他的</c:if>
                热门资源(近30天)
                <c:if test="${not empty usrDivTitle}">
                    <a href="/org/usr/file/all">more>></a>
                </c:if>
            </div>
            <div class="body source">
                <c:forEach items="${fileList}" var="item">
                    <a href="/org/usr/file/download/${item.id}">${item.fileName}${item.extension}<span>${item.downloadCount}</span></a>
                </c:forEach>
            </div>
        </div>
        <div class="module1">
            <div class="title">
                <c:if test="${empty usrDivTitle}">他的</c:if>
                热门文章(近30天)
                <c:if test="${not empty usrDivTitle}">
                    <a href="/text/article/all">more>></a>
                </c:if>
            </div>
            <div class="body article">
                <c:forEach items="${hotArticle}" var="item">
                    <a href="/text/article/detail/${item.id}">${item.title}<span>${item.count}</span></a>
                </c:forEach>
            </div>
        </div>
    </div>
</div>
<jsp:include page="/WEB-INF/views/common/foot.jsp"></jsp:include>
</body>
</html>
