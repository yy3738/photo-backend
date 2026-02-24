package com.photo.module.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.photo.common.exception.BizException;
import com.photo.module.auth.dto.LoginRespDTO;
import com.photo.module.auth.dto.RefreshTokenRespDTO;
import com.photo.module.user.entity.User;
import com.photo.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;

    @Value("${wx.appid:}")
    private String appid;

    @Value("${wx.secret:}")
    private String secret;

    /**
     * 微信登录
     */
    public LoginRespDTO wxLogin(String code) {
        // 调用微信 code2Session 接口
        String openid = getOpenidFromWx(code);

        // 查找或创建用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));

        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("用户" + openid.substring(0, 6));
            user.setRole("buyer");
            user.setPoints(0);
            user.setIsBanned(0);
            userMapper.insert(user);
        }

        if (user.getIsBanned() == 1) {
            throw new BizException(403, "账号已被封禁");
        }

        // Sa-Token 登录
        StpUtil.login(user.getId());
        String accessToken = StpUtil.getTokenValue();

        // 生成 refreshToken 存入 session
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        StpUtil.getSession().set("refreshToken", refreshToken);

        // 构建响应
        LoginRespDTO resp = new LoginRespDTO();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken);

        LoginRespDTO.LoginUserInfo userInfo = new LoginRespDTO.LoginUserInfo();
        userInfo.setId(String.valueOf(user.getId()));
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        userInfo.setPoints(user.getPoints());
        resp.setUser(userInfo);

        return resp;
    }

    /**
     * 刷新 Token
     */
    public RefreshTokenRespDTO refreshToken(String refreshToken) {
        // 遍历查找持有该 refreshToken 的用户 session
        // 简化实现：前端传 refreshToken，后端校验当前登录态
        // 实际生产中应使用独立的 refreshToken 存储
        if (!StpUtil.isLogin()) {
            throw new BizException(401, "refreshToken已失效，请重新登录");
        }

        String storedToken = (String) StpUtil.getSession().get("refreshToken");
        if (!refreshToken.equals(storedToken)) {
            throw new BizException(401, "refreshToken无效");
        }

        // 续期并生成新 refreshToken
        StpUtil.renewTimeout(7200); // accessToken 续期 2 小时
        String newRefreshToken = UUID.randomUUID().toString().replace("-", "");
        StpUtil.getSession().set("refreshToken", newRefreshToken);

        RefreshTokenRespDTO resp = new RefreshTokenRespDTO();
        resp.setAccessToken(StpUtil.getTokenValue());
        resp.setRefreshToken(newRefreshToken);
        return resp;
    }

    /**
     * 调用微信 code2Session
     */
    private String getOpenidFromWx(String code) {
        // 开发环境模拟：如果没有配置 appid，直接用 code 当 openid
        if (appid == null || appid.isEmpty()) {
            log.warn("未配置微信 appid，开发模式：code 直接作为 openid");
            return "dev_" + code;
        }

        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, Object> params = new HashMap<>();
        params.put("appid", appid);
        params.put("secret", secret);
        params.put("js_code", code);
        params.put("grant_type", "authorization_code");

        String result = HttpUtil.get(url, params);
        JSONObject json = JSONUtil.parseObj(result);

        if (json.containsKey("errcode") && json.getInt("errcode") != 0) {
            log.error("微信登录失败: {}", result);
            throw new BizException(400, "微信登录失败: " + json.getStr("errmsg"));
        }

        return json.getStr("openid");
    }
}
