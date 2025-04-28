package com.lloop.authcheckdemo.utils;

import com.google.gson.Gson;
import com.lloop.authcheckdemo.model.dto.UserToken;
import com.lloop.authcheckdemo.model.dto.UserTokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * JWT工具类，用于处理token的生成、验证和管理
 * @Author lloop
 * @Create 2024/12/26 15:01
 */
@Slf4j
@Data
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.expire.accessToken}")
    private Integer accessTokenExpire;

    @Value("${jwt.expire.refreshToken}")
    private Integer refreshTokenExpire;

    @Resource
    private RedisUtils redisUtils;

    private static final Gson gson = new Gson();
    private static final String TOKEN_TYPE = "JWT";
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String REFRESH_TOKEN_PREFIX = "token:refresh:";

    /**
     * 创建 刷新令牌 与 访问令牌 关联关系
     *
     * @param userToken 用户令牌信息
     * @param refreshTokenExpireDate 刷新令牌过期时间
     * @throws IllegalArgumentException 如果参数无效
     */
    public void tokenAssociation(UserToken userToken, Date refreshTokenExpireDate) {
        validateTokenAssociationParams(userToken, refreshTokenExpireDate);
        // 计算过期时间
        long expireSeconds = calculateExpireSeconds(refreshTokenExpireDate);
        String refreshTokenKey = getRefreshTokenKey(userToken.getRefreshToken());
        // 设置过期时间
        redisUtils.set(refreshTokenKey, userToken.getAccessToken(), expireSeconds);
        log.debug("Created token association for refresh token: {}", userToken.getRefreshToken());
    }

    /**
     * 根据 刷新令牌 获取 访问令牌
     *
     * @param refreshToken 刷新令牌
     * @return 访问令牌，如果不存在则返回null
     */
    public String getAccessTokenByRefresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            log.warn("Attempted to get access token with empty refresh token");
            return null;
        }

        String refreshTokenKey = getRefreshTokenKey(refreshToken);
        Object value = redisUtils.get(refreshTokenKey);
        
        if (value == null) {
            log.debug("No access token found for refresh token: {}", refreshToken);
            return null;
        }
        
        return String.valueOf(value);
    }

    /**
     * 将令牌添加到黑名单
     *
     * @param token 需要加入黑名单的令牌
     * @param expireTime 过期时间
     * @throws IllegalArgumentException 如果参数无效
     */
    public void addBlacklist(String token, Date expireTime) {
        validateBlacklistParams(token, expireTime);
        
        long expireSeconds = calculateExpireSeconds(expireTime);
        String key = getBlacklistKey(token);
        
        redisUtils.set(key, "1", expireSeconds);
        log.debug("Added token to blacklist: {}", token);
    }

    /**
     * 检查令牌是否在黑名单中
     *
     * @param token 需要检查的令牌
     * @return true 如果在黑名单中，false 如果不在
     */
    public Boolean checkBlacklist(String token) {
        if (!StringUtils.hasText(token)) {
            log.warn("Attempted to check blacklist with empty token");
            return false;
        }
        
        return redisUtils.hasKey(getBlacklistKey(token));
    }

    /**
     * 创建访问令牌和刷新令牌
     *
     * @param userTokenInfo 用户令牌信息
     * @return 包含访问令牌和刷新令牌的UserToken对象
     * @throws IllegalArgumentException 如果参数无效
     */
    public UserToken createTokens(UserTokenInfo userTokenInfo) {
        validateUserTokenInfo(userTokenInfo);
        
        Date nowDate = new Date();
        Date accessTokenExpireDate = new Date(nowDate.getTime() + accessTokenExpire * 1000L);
        Date refreshTokenExpireDate = new Date(nowDate.getTime() + refreshTokenExpire * 1000L);

        UserToken userToken = new UserToken();
        BeanUtils.copyProperties(userTokenInfo, userToken);
        userToken.setAccessToken(createToken(userTokenInfo, nowDate, accessTokenExpireDate));
        userToken.setRefreshToken(createToken(userTokenInfo, nowDate, refreshTokenExpireDate));

        // 创建 刷新令牌 与 访问令牌 关联关系
        tokenAssociation(userToken, refreshTokenExpireDate);
        log.debug("Created new tokens for user: {}", userTokenInfo.getUsername());
        
        return userToken;
    }

    /**
     * 生成JWT令牌
     *
     * @param userTokenInfo 用户令牌信息
     * @param nowDate 当前时间
     * @param expireDate 过期时间
     * @return 生成的JWT令牌
     */
    private String createToken(UserTokenInfo userTokenInfo, Date nowDate, Date expireDate) {
        return Jwts.builder()
                .setHeaderParam("typ", TOKEN_TYPE)
                .setSubject(gson.toJson(userTokenInfo))
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                // .signWith(SignatureAlgorithm.HS512, secret)  // 暂时不用这个,不然 token 太捏麻长了
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 获取 token 中注册信息
     *
     * @param token JWT令牌
     * @return 令牌中的声明信息
     * @throws IllegalArgumentException 如果令牌无效
     */
    public Claims getTokenClaim(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token cannot be empty");
        }

        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to parse token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token");
        }
    }

    /**
     * 验证令牌是否过期
     *
     * @param token JWT令牌
     * @return true 如果过期，false 如果未过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            return getExpirationDate(token).before(new Date());
        } catch (Exception e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 获取令牌过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date getExpirationDate(String token) {
        return getTokenClaim(token).getExpiration();
    }

    /**
     * 获取令牌发布时间
     *
     * @param token JWT令牌
     * @return 发布时间
     */
    public Date getIssuedAtDate(String token) {
        return getTokenClaim(token).getIssuedAt();
    }

    /**
     * 获取用户令牌信息
     *
     * @param token JWT令牌
     * @return 用户令牌信息
     */
    public UserTokenInfo getUserTokenInfo(String token) {
        String subject = getTokenClaim(token).getSubject();
        return gson.fromJson(subject, UserTokenInfo.class);
    }

    /**
     * 获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsername(String token) {
        return getUserTokenInfo(token).getUsername();
    }

    /**
     * 获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserId(String token) {
        return getUserTokenInfo(token).getId();
    }

    // 私有辅助方法
    private void validateTokenAssociationParams(UserToken userToken, Date refreshTokenExpireDate) {
        if (userToken == null || !StringUtils.hasText(userToken.getRefreshToken()) || 
            !StringUtils.hasText(userToken.getAccessToken()) || refreshTokenExpireDate == null) {
            throw new IllegalArgumentException("Invalid token association parameters");
        }
    }

    private void validateBlacklistParams(String token, Date expireTime) {
        if (!StringUtils.hasText(token) || expireTime == null) {
            throw new IllegalArgumentException("Invalid blacklist parameters");
        }
    }

    private void validateUserTokenInfo(UserTokenInfo userTokenInfo) {
        if (userTokenInfo == null || userTokenInfo.getId() == null || 
            !StringUtils.hasText(userTokenInfo.getAccount())) {
            throw new IllegalArgumentException("Invalid user token info");
        }
    }

    private long calculateExpireSeconds(Date expireDate) {
        return (expireDate.getTime() - System.currentTimeMillis()) / 1000 + 100;
    }

    private String getRefreshTokenKey(String refreshToken) {
        return REFRESH_TOKEN_PREFIX + refreshToken;
    }

    private String getBlacklistKey(String token) {
        return TOKEN_BLACKLIST_PREFIX + token;
    }
}
