package com.lloop.authcheckdemo.utils;

import com.google.gson.Gson;
import com.lloop.authcheckdemo.constant.UserConstant;
import com.lloop.authcheckdemo.model.dto.UserToken;
import com.lloop.authcheckdemo.model.dto.UserTokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author lloop
 * @Create 2024/12/26 15:01
 */
@Data
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    public String secret;

    @Value("${jwt.header}")
    public String header;

    @Value("${jwt.expire.accessToken}")
    public Integer accessTokenExpire;

    @Value("${jwt.expire.refreshToken}")
    public Integer refreshTokenExpire;

    @Resource
    RedisUtils redisUtils;

    private static final Gson gson = new Gson();


    /**
     * 创建 刷新令牌 与 访问令牌 关联关系
     *
     * @param userToken
     * @param refreshTokenExpireDate
     */
    public void tokenAssociation(UserToken userToken, Date refreshTokenExpireDate) {
        long time = (refreshTokenExpireDate.getTime() - System.currentTimeMillis()) / 1000 + 100;
        redisUtils.set(getRefreshTokenPrefix(userToken.getRefreshToken()), userToken.getAccessToken(), time);
    }

    /**
     * 根据 刷新令牌 获取 访问令牌
     *
     * @param refreshToken
     */
    public String getAccessTokenByRefresh(String refreshToken) {
        Object value = redisUtils.get(getRefreshTokenPrefix(refreshToken));
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 获取 刷新令牌前缀
     * @param refreshToken
     * @return
     */
    private String getRefreshTokenPrefix(String refreshToken) {
        return UserConstant.USER_REFRESH_TOKEN_PREFIX + refreshToken;
    }

    /**
     * 添加至黑名单
     *
     * @param token
     * @param expireTime
     */
    public void addBlacklist(String token, Date expireTime) {
        long expireTimeLong = (expireTime.getTime() - System.currentTimeMillis()) / 1000 + 100;
        redisUtils.set(getBlacklistPrefix(token), "1", expireTimeLong);
    }

    /**
     * 校验是否存在黑名单
     *
     * @param token
     * @return true 存在 false不存在
     */
    public Boolean checkBlacklist(String token) {
        return redisUtils.hasKey(getBlacklistPrefix(token));
    }

    /**
     * 获取 黑名单前缀
     *
     * @param token
     * @return
     */
    public String getBlacklistPrefix(String token) {
        return UserConstant.TOKEN_BLACKLIST_PREFIX + token;
    }


    /**
     * 获取 token 信息
     *
     * @param userTokenInfo
     * @return
     */
    public UserToken createTokens(UserTokenInfo userTokenInfo) {
        Date nowDate = new Date();
        Date accessTokenExpireDate = new Date(nowDate.getTime() + accessTokenExpire * 1000);
        Date refreshTokenExpireDate = new Date(nowDate.getTime() + refreshTokenExpire * 1000);

        UserToken userToken = new UserToken();
        BeanUtils.copyProperties(userTokenInfo, userToken);
        userToken.setAccessToken(createToken(userTokenInfo, nowDate, accessTokenExpireDate));
        userToken.setRefreshToken(createToken(userTokenInfo, nowDate, refreshTokenExpireDate));

        // 创建 刷新令牌 与 访问令牌 关联关系
        tokenAssociation(userToken, refreshTokenExpireDate);
        return userToken;
    }

    /**
     * 生成token
     *
     * @param userTokenInfo
     * @return
     */
    public String createToken(UserTokenInfo userTokenInfo, Date nowDate, Date expireDate) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
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
     * @param token
     * @return
     */
    public Claims getTokenClaim(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证 token 是否过期失效
     *
     * @param token
     * @return true 过期 false 未过期
     */
    public Boolean isTokenExpired(String token) {
        return getExpirationDate(token).before(new Date());
    }

    /**
     * 获取 token 失效时间
     *
     * @param token
     * @return
     */
    public Date getExpirationDate(String token) {
        return getTokenClaim(token).getExpiration();
    }


    /**
     * 获取 token 发布时间
     *
     * @param token
     * @return
     */
    public Date getIssuedAtDate(String token) {
        return getTokenClaim(token).getIssuedAt();
    }


    /**
     * 获取用户信息
     *
     * @param token 包含了用户身份信息的JWT字符串
     * @return
     */
    public UserTokenInfo getUserTokenInfo(String token) {
        String subject = getTokenClaim(token).getSubject();
        return gson.fromJson(subject, UserTokenInfo.class);
    }

    /**
     * 获取用户名
     *
     * @param token
     * @return
     */
    public String getUsername(String token) {
        UserTokenInfo userInfoToken = getUserTokenInfo(token);
        return userInfoToken.getUsername();
    }

    /**
     * 获取用户Id
     *
     * @param token
     * @return
     */
    public Long getUserId(String token) {

        UserTokenInfo userInfoToken = getUserTokenInfo(token);
        return userInfoToken.getId();
    }

}
