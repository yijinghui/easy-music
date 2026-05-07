package com.easy.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.constant.JwtClaimsConstant;
import com.easy.constant.MessageConstant;
import com.easy.enumeration.RoleEnum;
import com.easy.mapper.UserMapper;
import com.easy.pojo.dto.*;
import com.easy.pojo.entity.User;
import com.easy.pojo.vo.UserAdminVO;
import com.easy.pojo.vo.UserVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.EmailService;
import com.easy.service.MinIOService;
import com.easy.service.UserService;
import com.easy.utils.JwtUtil;
import com.easy.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    private final MinIOService minIOService;

    private final StringRedisTemplate stringRedisTemplate;

    private final EmailService emailService;


    @Override
    public Result<PageResult<UserAdminVO>> page(UserPageQueryDTO userPageQueryDTO) {
        Page<User> page = new Page<>(userPageQueryDTO.getPageNum(), userPageQueryDTO.getPageSize());

        String username = userPageQueryDTO.getUsername();
        String phone = userPageQueryDTO.getPhone();
        Integer userStatus = userPageQueryDTO.getUserStatus();

        // 1.使用LambdaQueryWrapper构建分页查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .like(StrUtil.isNotBlank(username), User::getUsername, username)
                .like(StrUtil.isNotBlank(phone), User::getPhone, phone)
                .eq(userStatus != null, User::getUserStatus, userStatus)
                .orderByDesc(User::getCreateTime);
        // 2.分页查询
        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);

        // 3.判断查询结果是否为空2
        if (userPage.getTotal() == 0){
            return Result.success("未找到相关数据", new PageResult<>(0L, null));
        }

        // 4.将查询结果转换为VO
        List<UserAdminVO> userAdminVOList = userPage.getRecords().stream()
                .map(user -> {
                    UserAdminVO userAdminVO = new UserAdminVO();
                    BeanUtil.copyProperties(user, userAdminVO);
                    return userAdminVO;
                }).toList();
        // 5.返回结果
        return Result.success(new PageResult<>(userPage.getTotal(), userAdminVOList));


    }

    public Result addUser(UserAddDTO userAddDTO) {
        // 1.构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userAddDTO.getUsername())
                .or()
                .eq("phone", userAddDTO.getPhone())
                .or()
                .eq("email", userAddDTO.getEmail());
        // 2.查询用户信息
        List<User> userList = userMapper.selectList(queryWrapper);
        // 3.判断用户信息是否已存在
        if (!userList.isEmpty()){
            for (User user : userList){
                if (user.getUsername().equals(userAddDTO.getUsername())){
                    return Result.error("用户名已存在");
                }
                if (user.getPhone().equals(userAddDTO.getPhone())){
                    return Result.error("手机号已存在");
                }
                if (user.getEmail().equals(userAddDTO.getEmail())){
                    return Result.error("邮箱已存在");
                }
            }
        }
        // 4.保存用户信息
        String passwordMd5 = DigestUtils.md5DigestAsHex(userAddDTO.getPassword().getBytes());
        User newUser = new User();
        BeanUtil.copyProperties(userAddDTO, newUser);
        newUser.setPassword(passwordMd5);
        newUser.setCreateTime(LocalDateTime.now());
        newUser.setUpdateTime(LocalDateTime.now());
        int result = userMapper.insert(newUser);

        if (result == 0){
            return Result.error("用户添加失败");
        }
        // 5.返回结果
        return Result.success("用户添加成功");
    }

    public Result updateUser(UserDTO userDTO) {
        Long id = userDTO.getUserId();
        if (id == null){
            return Result.error("用户ID不能为空");
        }

        User user = userMapper.selectById(id);
        if (user == null){
            return Result.error("用户不存在");
        }
        User newUser = new User();
        BeanUtils.copyProperties(userDTO, newUser);
        newUser.setUserId(id);
        newUser.setUpdateTime(LocalDateTime.now());
        int result = userMapper.updateById(newUser);
        if (result == 0){
            return Result.error("用户更新失败");
        }

        return Result.success("用户更新成功");
    }

    public Result updateUserStatus(Long userId, Integer userStatus) {
        User user = userMapper.selectById(userId);
        if (user == null){
            return Result.error("用户不存在");
        }
        user.setUserStatus(userStatus);
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.updateById(user);
        if (result == 0){
            return Result.error("用户状态更新失败");
        }
        return Result.success("用户状态更新成功");
    }

    public Result deleteUser(Long userId) {
        if(userId== null) return Result.error("用户ID不能为空");

        User user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");

        // 删除用户头像
        String avatar = user.getUserAvatar();
        if (StrUtil.isNotBlank(avatar)) {
            minIOService.deleteFile(avatar);
        }

        return Result.success(userMapper.deleteById(userId) == 1 ? "用户删除成功" : "用户删除失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result deleteUsers(List<Long> userIds) {
        // 批量查询用户
        List<User> userList = userMapper.selectByIds(userIds);

        List<Long> userIdList = userList.stream().map(User::getUserId).toList();

        if(userList.isEmpty()){
            return Result.success("未找到相关数据");
        }

        // 删除用户头像
        for (User user : userList) {
            if (StrUtil.isNotBlank(user.getUserAvatar())) {
                minIOService.deleteFile(user.getUserAvatar());
            }
        }

        if (userMapper.deleteByIds(userIdList) == 0) {
            return Result.error("用户批量删除失败");
        }


        return Result.success("删除成功，已删除用户"+userIdList);
    }

    @Override
    public Result loginByPassword(UserPasswordLoginDTO userLoginDTO) {
        // 根据用户名查询用户信息
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", userLoginDTO.getUsername()));

        if (user == null){
            return Result.error("用户不存在");
        }

        if (user.getUserStatus() == 0){
            return Result.error("用户已被禁用");
        }

        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes()))){
            return Result.error("密码错误");
        }

        Long userId = user.getUserId();
        // 4.1.生成Jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
        claims.put(JwtClaimsConstant.USER_ID, userId);
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        String token = JwtUtil.generateToken(claims);
        // 4.2.将token存入Redis
        stringRedisTemplate.opsForValue().set("login:user:" + userId, token, 10, TimeUnit.HOURS);

        // 5.返回结果
        return Result.success("登录成功", token);

    }

    @Override
    public Result loginByEmail(UserEmailLoginDTO userLoginDTO) {

        String email = userLoginDTO.getEmail();

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));

        if (user == null){
            return Result.error("用户不存在");
        }

        if (user.getUserStatus() == 0){
            return Result.error("用户已被禁用");
        }

        String code=stringRedisTemplate.opsForValue().get("verificationCode:login:" + email);
        if(code==null){
            return Result.error("验证码已过期");
        }
        if(!code.equals(userLoginDTO.getVerificationCode())){
            return Result.error("验证码错误");
        }

        Long userId = user.getUserId();
        // 4.1.生成Jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
        claims.put(JwtClaimsConstant.USER_ID,userId);
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        String token = JwtUtil.generateToken(claims);
        // 4.2.将token存入Redis
        stringRedisTemplate.opsForValue().set("login:user:" + userId, token, 10, TimeUnit.HOURS);

        // 5.返回结果
        return Result.success("登录成功", token);
    }


    @Transactional(rollbackFor = Exception.class)
    public Result register(UserRegisterDTO userRegisterDTO) {

        String email = userRegisterDTO.getEmail();

        // 1.校验验证码
        String code = stringRedisTemplate.opsForValue().get("verificationCode:register:" + email);

        if (code == null){
            return Result.error("验证码已过期");
        }

        if (!code.equals(userRegisterDTO.getVerificationCode())){
            return Result.error("验证码错误");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));

        if (user != null){
            return Result.error("邮箱已存在");
        }

        user = new User();
        user.setEmail(email);
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()));
        user.setUserStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.insert(user);
        if (result == 0){
            return Result.error("用户注册失败");
        }
        return Result.success("用户注册成功");


    }

    @Override
    public Result sendVerificationCode(String email,Integer operationType) {

        String verificationCode = emailService.sendVerificationCodeEmail(email);
        if (verificationCode == null) {
            return Result.error(MessageConstant.EMAIL_SEND_FAILED);
        }
        if (operationType == 1){
            stringRedisTemplate.opsForValue().set("verificationCode:register:" + email, verificationCode, 5, TimeUnit.MINUTES);
        }
        if (operationType == 2){
            stringRedisTemplate.opsForValue().set("verificationCode:login:" + email, verificationCode, 5, TimeUnit.MINUTES);
        }
        if (operationType == 3){
            stringRedisTemplate.opsForValue().set("verificationCode:updateEmail:" + email, verificationCode, 5, TimeUnit.MINUTES);
        }
        if (operationType == 4){
            stringRedisTemplate.opsForValue().set("verificationCode:updatePassword:" + email, verificationCode, 5, TimeUnit.MINUTES);
        }

        return Result.success(MessageConstant.EMAIL_SEND_SUCCESS);
    }

    @Override
    public Result logout() {


        Long userId = ThreadLocalUtil.getUserId();

        Boolean delete = stringRedisTemplate.delete("login:user:" + userId);

        if (!delete){
            return Result.error("用户未登录");
        }

        return Result.success("退出登录成功");

    }

    @Override
    public Result<UserVO> userInfo() {

        Long userId = ThreadLocalUtil.getUserId();
        User user = userMapper.selectById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);


        return Result.success(userVO);
    }

    @Override
    public Result updateUserInfo(UserUpdateDTO updateDTO) {

        Long userId =ThreadLocalUtil.getUserId();

        updateDTO.setUserId(userId);

        // 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null){
            return Result.error("用户不存在");
        }

        String username = updateDTO.getUsername();
        String phone = updateDTO.getPhone();


        if (userMapper.selectCount(new QueryWrapper<User>().eq("username",username))>0){
            return Result.error("用户名已存在");
        }

        if (userMapper.selectCount(new QueryWrapper<User>().eq("phone",phone))>0){
            return Result.error("手机号已存在");
        }

        BeanUtil.copyProperties(updateDTO, user);

        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.updateById(user);
        if (result == 0){
            return Result.error("邮箱修改失败");
        }
        return Result.success("邮箱修改成功");

    }

    @Override
    public Result updateUserPassword(UserResetPasswordDTO userResetPasswordDTO) {


        // Redis中查询验证码是否合法
        String code = stringRedisTemplate.opsForValue().get("verificationCode:updatePassword:" + userResetPasswordDTO.getEmail());


        if (code == null){
            return Result.error("验证码已过期");
        }

        if (!code.equals(userResetPasswordDTO.getVerificationCode())){
            return Result.error("验证码错误");
        }

        String email = userResetPasswordDTO.getEmail();
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
        if (user == null){
            return Result.error("用户不存在");
        }

        Long userId = user.getUserId();
        String newPassword = userResetPasswordDTO.getNewPassword();
        String repeatPassword = userResetPasswordDTO.getRepeatPassword();
        if (!newPassword.equals(repeatPassword)){
            return Result.error("两次密码不一致");
        }


        user.setPassword(DigestUtils.md5DigestAsHex(userResetPasswordDTO.getNewPassword().getBytes()));
        user.setUpdateTime(LocalDateTime.now());
        int result = userMapper.updateById(user);
        if (result == 0){
            return Result.error("密码重置失败");
        }
        // 删除Redis中的token
        stringRedisTemplate.delete("login:user:" + userId);
        return Result.success("密码重置成功");
    }


    @Override
    public Result updateUserEmail(UserEmailUpdateDTO updateDTO) {
        Long userId = ThreadLocalUtil.getUserId();

        String newEmail = updateDTO.getNewEmail();

        if (userMapper.selectCount(new QueryWrapper<User>().eq("email",newEmail))>0){
            return Result.error("邮箱已存在");
        }

        // Redis中查询验证码是否合法
        String code = stringRedisTemplate.opsForValue().get("verificationCode:updateEmail:" + newEmail);

        if (code == null){
            return Result.error("验证码已过期");
        }

        if (!code.equals(updateDTO.getVerificationCode())){
            return Result.error("验证码错误");
        }

        User user = userMapper.selectById(userId);
        if (user == null){return Result.error("用户不存在");}

        if (newEmail.equals(user.getEmail())){return Result.error("新邮箱不能与旧邮箱相同");}

        user.setEmail(newEmail);
        user.setUpdateTime(LocalDateTime.now());
        if (userMapper.updateById(user) == 0){
            return Result.error("用户更新失败");
        }
        return Result.success("用户更新成功");
    }


    @Transactional(rollbackFor = Exception.class)
    public Result updateUserAvatar(MultipartFile avatar) {
        String avatarUrl = minIOService.uploadFile(avatar, "users");

        Long userId = ThreadLocalUtil.getUserId();

        User user = userMapper.selectById(userId);
        if (user == null){
            return Result.error("用户不存在");
        }
        user.setUserAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        if (userMapper.updateById(user) == 0){
            return Result.error("头像更新失败");
        }
        return Result.success("头像更新成功");
    }


    @Transactional(rollbackFor = Exception.class)
    public Result deleteAccount(String token) {
        Long userId = ThreadLocalUtil.getUserId();

        stringRedisTemplate.delete("login:user:" + userId);

        User user = userMapper.selectById(userId);

        // 删除头像
        minIOService.deleteFile(user.getUserAvatar());

        return userMapper.deleteById(userId) == 0 ? Result.error("注销失败") : Result.success("注销成功");
    }

}
