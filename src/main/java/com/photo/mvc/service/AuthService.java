package com.photo.mvc.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.photo.common.exception.BizException;
import com.photo.mvc.entity.model.SysUserPO;
import com.photo.mvc.entity.vo.LoginVO;
import com.photo.mvc.entity.vo.RefreshTokenVO;
import com.photo.mvc.mapper.SysUserMapper;
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

    private final SysUserMapper sysUserMapper;

    @Value("${wx.appid:}")
    private String appid;

    @Value("${wx.secret:}")
    private String secret;

    public LoginVO wxLogin(String code) {
        String openid = getOpenidFromWx(code);

        SysUserPO user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUserPO>().eq(SysUserPO::getOpenid, openid));

        if (user == null) {
            user = new SysUserPO();
            user.setOpenid(openid);
            user.setNickname("用户" + openid.substring(0, 6));
            user.setRole("buyer");
            user.setPoints(0);
            user.setIsBanned(0);
            sysUserMapper.insert(user);
        }

        if (user.getIsBanned() == 1) {
            throw new BizException(403, "账号已被封禁");
        }

        StpUtil.login(user.getId());
        String accessToken = StpUtil.getTokenValue();

        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        StpUtil.getSession().set("refreshToken", refreshToken);

        LoginVO resp = new LoginVO();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken);

        LoginVO.LoginUserInfo userInfo = new LoginVO.LoginUserInfo();
        userInfo.setId(String.valueOf(user.getId()));
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        userInfo.setPoints(user.getPoints());
        resp.setUser(userInfo);

        return resp;
    }

    public RefreshTokenVO refreshToken(String refreshToken) {
        if (!StpUtil.isLogin()) {
            throw new BizException(401, "refreshToken已失效，请重新登录");
        }

        String storedToken = (String) StpUtil.getSession().get("refreshToken");
        if (!refreshToken.equals(storedToken)) {
            throw new BizException(401, "refreshToken无效");
        }

        StpUtil.renewTimeout(7200);
        String newRefreshToken = UUID.randomUUID().toString().replace("-", "");
        StpUtil.getSession().set("refreshToken", newRefreshToken);

        RefreshTokenVO resp = new RefreshTokenVO();
        resp.setAccessToken(StpUtil.getTokenValue());
        resp.setRefreshToken(newRefreshToken);
        return resp;
    }

    private String getOpenidFromWx(String code) {
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
