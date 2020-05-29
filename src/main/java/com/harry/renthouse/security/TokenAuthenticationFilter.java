package com.harry.renthouse.security;

import com.harry.renthouse.base.ApiResponseEnum;
import com.harry.renthouse.exception.BusinessException;
import com.harry.renthouse.util.TokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.util.Map;

/**
 * token认证过滤器
 * @author Harry Xu
 * @date 2020/5/11 9:52
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final String  TOKEN_HEADER = "Authorization";

    private final String  TOKEN_PREFIX = "Bearer ";

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc)
            throws ServletException, IOException {

        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
            // do nothing
        } else {
            String token = req.getHeader(TOKEN_HEADER);
            if(StringUtils.isNotEmpty(token)){
                //解析Token时将“Bearer ”前缀去掉
                token = StringUtils.trim(token).replace(TOKEN_PREFIX, "");
            }
            if(StringUtils.isBlank(token)){
                token = req.getParameter("token");
            }
            // 如果请求头中有token,则生成Authentication凭证
            if (StringUtils.isNotBlank(token)) {
                if (!tokenUtil.hasToken(token)){
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    throw new BusinessException(ApiResponseEnum.UNAUTHORIZED);
                }
                tokenUtil.refresh(token);
                Authentication auth = new TokenAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        fc.doFilter(req, res);
    }
}
