package com.libraryinformation.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.libraryinformation.dao.UserDao;
import com.libraryinformation.domain.RegisterRequest;
import com.libraryinformation.domain.Result;
import com.libraryinformation.domain.User;
import com.libraryinformation.stateCode.Code;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 用户控制器
 *
 * @author ZhouTX
 */
@RestController
@Transactional
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserDao userDao;

    /**
     * 查询所有用户
     *
     * @return
     */
    @GetMapping
    public Result selectAll() {
        System.out.println("查询所有用户");
        List<User> users = userDao.getAll();
        return new Result(Code.GET_OK, users);
    }

    /**
     * 保存用户，非注册使用
     *
     * @param user
     * @return
     */
    @PostMapping
    @PreAuthorize("hasAuthority('admin')")
    public Result save(@RequestBody User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        int flag = userDao.insert(user);
        return new Result(flag == 1 ? Code.SAVE_OK : Code.SAVE_ERR, flag);
    }

    /**
     * 更新用户
     *
     * @param user
     * @return
     */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('admin','user')")
    public Result update(@RequestBody User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        int flag = userDao.updateById(user);
        return new Result(flag == 1 ? Code.UPDATE_OK : Code.UPDATE_ERR, flag);
    }

    /**
     * 删除用户
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public Result delete(@PathVariable String id) {
        int flag = userDao.deleteById(id);
        return new Result(flag == 1 ? Code.DELETE_OK : Code.DELETE_ERR, flag);
    }

    /**
     * 根据id查询用户
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('admin','user')")
    public Result getById(@PathVariable String id) {
        User user = userDao.selectById(id);
        Integer code = user != null ? Code.GET_OK : Code.GET_ERR;
        String msg = user != null ? "" : "数据查询失败，请重试！";
        return new Result(code, user, msg);
    }

    /**
     * 根据uid查询用户
     * 用于注册时验证uid是否已存在
     *
     * @param uid
     * @return
     */
    @GetMapping("/id/{uid}")
    public Result checkUid(@PathVariable String uid) {
        User user = userDao.getById(uid);
        Integer code = user != null ? Code.GET_OK : Code.GET_ERR;
        String msg = user != null ? "用户名已被注册啦，换一个吧" : "用户名还未被注册，赶快注册吧！";
        if (user != null) {
            return new Result(code, user.getUid(), msg);
        } else {
            return new Result(code, null, msg);
        }
    }

    /**
     * 根据uid精准查询与uname模糊查询用户，并分页
     *
     * @param page
     * @param size
     * @param uid
     * @param uname
     * @return
     */
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('admin')")
    public IPage<User> getByPage(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 @RequestParam(required = false) Integer uid,
                                 @RequestParam(required = false) String uname
    ) {
        IPage<User> userPage = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (uid != null && !"".equals(String.valueOf(uid))) {
            wrapper.like("uid", uid);
        }
        if (uname != null && !"".equals(uname)) {
            wrapper.like("uname", uname);
        }
        userDao.selectPage(userPage, wrapper);
        return userPage;
    }

    /**
     * 验证用户登录
     *
     * @param user
     * @return
     */
//    @PostMapping("/login")
    /*public Result login(@RequestBody User user) {
        User _user = userDao.selectById(user.getUid());
        if (_user == null) return new Result(Code.LOGIN_ERR, null, "用户名不存在，请先注册！");
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        Integer code = user.getPassword().equals(_user.getPassword()) ? Code.LOGIN_OK : Code.LOGIN_ERR;
        String msg = user.getPassword().equals(_user.getPassword()) ? "登录成功" : "用户名或密码错误，请重试！";
        if (code.equals(Code.LOGIN_OK)) {
            String token = tokenUtil.createToken(_user);
            _user.setPassword(null);
            _user.setUphone(null);
            return new Result(code, new LoginResponse(token,_user), msg);
        } else return new Result(code, null, msg);
    }*/

    /**
     * 批量删除用户
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/ids")
    @PreAuthorize("hasAuthority('admin')")
    public Result deleteByIds(@RequestBody List<String> ids) {
        int flag = userDao.deleteBatchIds(ids);
        return new Result(flag == ids.size() ? Code.DELETE_OK : Code.DELETE_ERR, flag);
    }

    /**
     * 用户注册
     *
     * @param registerRequest
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        // 验证码校验
        HttpSession session = request.getSession();
        String _code = (String) session.getAttribute("verifyCode");
        if (!_code.equals(registerRequest.getCheckCode())) {
            return new Result(Code.VERIFY_CODE_ERR, null, "验证码错误，请重试！");
        }
        // 用户注册
        User rUser = registerRequest.getUser();
        User user = User.builder()
                .uid(rUser.getUid())
                .password(new BCryptPasswordEncoder().encode(rUser.getPassword()))
                .uname(rUser.getUname())
                .uphone(rUser.getUphone())
                .uidentity("user")
                .deleted(0)
                .build();
        int flag = userDao.insert(user);
        return new Result(flag == 1 ? Code.REGISTER_OK : Code.REGISTER_ERR, null, flag == 1 ? "注册成功" : "注册失败，请重试！");
    }

}