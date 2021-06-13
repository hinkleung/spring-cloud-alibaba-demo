package com.itmuch.usercenter.controller.user;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.itmuch.usercenter.auth.CheckLogin;
import com.itmuch.usercenter.domain.entity.user.*;
import com.itmuch.usercenter.service.user.UserService;
import com.itmuch.usercenter.util.JwtOperator;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private WxMaService wxMaService;

    @Resource
    private JwtOperator jwtOperator;

    @GetMapping("/{id}")
    @CheckLogin
    public User findById(@PathVariable Integer id) {
        return userService.findById(id);
    }

    /**
     * 模拟生成token（模拟登录）
     *
     * @param
     * @return
     * @throws WxErrorException
     */
    @GetMapping("/gen-token")
    public String genToken() {
        // 颁发
        //颁发token
        Map<String, Object> userInfo = new HashMap<>(3);
        userInfo.put("id", 1);
        userInfo.put("wxNickName", "hinkleung");
        userInfo.put("role", "user");
        String token = jwtOperator.generateToken(userInfo);
        return token;
    }


    @PostMapping("/login")
    public LoginRespDTO login(@RequestBody UserLoginDTO loginDTO) throws WxErrorException {
        // 微信小程序服务端校验是否已经登录的结果
        WxMaJscode2SessionResult result = wxMaService.getUserService()
                .getSessionInfo(loginDTO.getCode());

        // 微信的openId，用户在微信这边的唯一标识
        String openid = result.getOpenid();

        //看用户是否注册，如果没有注册就插入
        //如果已经注册，就直接颁发token
        User user = userService.login(loginDTO, openid);

        //颁发token
        Map<String, Object> userInfo = new HashMap<>(3);
        userInfo.put("id", user.getId());
        userInfo.put("wxNickName", user.getWxNickname());
        userInfo.put("role", user.getRoles());
        String token = jwtOperator.generateToken(userInfo);

        log.info("用户{}登录成功，生成的token={}，有效期到{}",
                user.getWxNickname(), token, jwtOperator.getExpirationDateFromToken(token));

        return LoginRespDTO.builder()
                .user(UserRespDTO.builder()
                        .id(user.getId())
                        .avatarUrl(user.getAvatarUrl())
                        .bonus(user.getBonus())
                        .wxNickname(user.getWxNickname())
                        .build())
                .token(JwtTokenRespDTO.builder()
                        .expirationTime(jwtOperator.getExpirationDateFromToken(token).getTime())
                        .token(token)
                        .build())
                .build();
    }

}
