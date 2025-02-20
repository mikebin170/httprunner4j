package io.lematech.httprunner4j.controller;

import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Maps;
import io.lematech.httprunner4j.core.annotation.ValidateRequest;
import io.lematech.httprunner4j.core.entity.User;
import io.lematech.httprunner4j.core.enums.CommonBusinessCode;
import io.lematech.httprunner4j.service.TokenService;
import io.lematech.httprunner4j.service.UserService;
import io.lematech.httprunner4j.vo.TokenVO;
import io.lematech.httprunner4j.vo.UserVO;
import io.lematech.httprunner4j.vo.base.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

/**
 * @author lematech@foxmail.com
 * @version 1.0.0
 * @className HttpRunner4jController
 * @description Httprunner4j 使用入门案例
 * @created 2021/3/2 1:57 下午
 * @publicWechat lematech
 */
@RestController
@RequestMapping("/api")
public class HttpRunner4jController {
    @Autowired
    private TokenService tokenServiceImpl;

    @Autowired
    private UserService userServiceImpl;

    @GetMapping(value = "/")
    public R index() {
        return R.ok("Hello,Lematech~!");
    }

    @PostMapping(value = "/get-token")
    public R getToken(@RequestHeader(value = "device_sn") String deviceSN,
                      @RequestHeader(value = "os_platform", required = false) String osPlatform,
                      @RequestHeader(value = "app_version", required = false) String appVersion,
                      @RequestBody TokenVO tokenVO) {
        String expectSign = tokenServiceImpl.generateToken(deviceSN, osPlatform, appVersion);
        boolean validateResult = tokenServiceImpl.validateToken(tokenVO.getSign(), expectSign);
        if (!validateResult) {
            return R.fail(CommonBusinessCode.Authorization_FAILED_EXCEPTION);
        }
        String token = RandomUtil.randomString(16);
        tokenServiceImpl.storyToken(deviceSN, token);
        Map resultData = Maps.newHashMap();
        resultData.put("token", token);
        return R.ok(resultData);
    }

    @PostMapping(value = "/user/{uid}")
    @ValidateRequest(headerNames = {"device_sn", "token"})
    public R createUser(@PathVariable int uid, @RequestBody UserVO userVO) {
        User addUser = new User();
        addUser.setUid(uid);
        addUser.setName(userVO.getName());
        addUser.setPassword(userVO.getPassword());
        userServiceImpl.addUser(addUser);
        return R.ok("用户创建成功！");
    }

    @GetMapping(value = "/user/{uid}")
    @ValidateRequest(headerNames = {"device_sn", "token"})
    public R queryUser(@PathVariable int uid) {
        User user = userServiceImpl.findByUid(uid);
        if (Objects.isNull(user)) {
            return R.fail(CommonBusinessCode.USER_IS_NOT_EXISTS_EXCEPTION);
        }
        return R.ok(user);
    }

    @PutMapping(value = "/user/{uid}")
    @ValidateRequest(headerNames = {"device_sn", "token"})
    public R updateUser(@PathVariable int uid, @RequestBody UserVO userVO) {
        User addUser = new User();
        addUser.setUid(uid);
        addUser.setName(userVO.getName());
        addUser.setPassword(userVO.getPassword());
        userServiceImpl.updateUser(addUser);
        return R.ok("用户更新成功！");
    }

    @DeleteMapping(value = "/user/{uid}")
    @ValidateRequest(headerNames = {"device_sn", "token"})
    public R deleteUser(@PathVariable int uid) {
        boolean delFlag = userServiceImpl.deleteUser(uid);
        if (!delFlag) {
            R.fail("删除用户失败！");
        }
        return R.ok("用户删除成功！");
    }

    @GetMapping(value = "/users/list")
    @ValidateRequest(headerNames = {"device_sn", "token"})
    public R list() {
        return R.ok(userServiceImpl.lists());
    }

    @GetMapping(value = "/users/reset-all")
    @ValidateRequest(headerNames = {"device_sn", "token"})
    public R resetAll() {
        userServiceImpl.deleteUsers();
        return R.ok();
    }
}
