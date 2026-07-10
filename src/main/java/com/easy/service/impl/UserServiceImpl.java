package com.easy.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.easy.enumeration.RoleEnum;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.constant.JwtClaimsConstant;
import com.easy.constant.MessageConstant;
import com.easy.exception.AccessDeniedException;
import com.easy.exception.BaseException;
import com.easy.mapper.UserMapper;
import com.easy.pojo.dto.*;
import com.easy.pojo.entity.Artist;
import com.easy.pojo.entity.User;
import com.easy.pojo.vo.PlaylistInfoVO;
import com.easy.pojo.vo.UserAdminVO;
import com.easy.pojo.vo.UserStatVO;
import com.easy.pojo.vo.UserVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.*;
import com.easy.utils.JwtUtil;
import com.easy.utils.ThreadLocalUtil;
import com.minio.MinioTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private final MinioTemplate minioTemplate;

    private final StringRedisTemplate stringRedisTemplate;

    private final EmailService emailService;

    private final StatisticService statService;

    private final PlaylistService playlistService;


    @Override
    public PageResult list(UserPageQueryDTO userPageQueryDTO) {
        Page<User> page = new Page<>(userPageQueryDTO.getPageNum(), userPageQueryDTO.getPageSize());
        Long userId =userPageQueryDTO.getUserId();
        String username = userPageQueryDTO.getUsername();
        String phone = userPageQueryDTO.getPhone();
        Integer userStatus = userPageQueryDTO.getUserStatus();

        Page<User> result = lambdaQuery().eq(userId != null, User::getUserId, userId)
                .like(StrUtil.isNotBlank(username), User::getUsername, username)
                .like(StrUtil.isNotBlank(phone), User::getPhone, phone)
                .eq(userStatus != null, User::getUserStatus, userStatus)
                .orderByDesc(User::getUserId)
                .page(page);
        return new PageResult(result.getTotal(), result.getRecords());


    }

    public Result addUser(UserDTO userDTO) {
        // 1.构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername())
                .or()
                .eq("phone", userDTO.getPhone())
                .or()
                .eq("email", userDTO.getEmail());
        // 2.查询用户信息
        List<User> userList = baseMapper.selectList(queryWrapper);
        // 3.判断用户信息是否已存在
        if (!userList.isEmpty()){
            for (User user : userList){
                if (user.getUsername().equals(userDTO.getUsername())){
                    return Result.error("用户名已存在");
                }
                if (user.getPhone().equals(userDTO.getPhone())){
                    return Result.error("手机号已存在");
                }
                if (user.getEmail().equals(userDTO.getEmail())){
                    return Result.error("邮箱已存在");
                }
            }
        }
        // 4.保存用户信息
        String passwordMd5 = DigestUtils.md5DigestAsHex("Hh123456".getBytes());
        User newUser = new User();
        BeanUtil.copyProperties(userDTO, newUser);
        newUser.setPassword(passwordMd5);
        newUser.setUserStatus(1);
        int result = baseMapper.insert(newUser);

        if (result == 0){
            return Result.error("用户添加失败");
        }
        // 5.返回结果
        return Result.success("已添加用户"+userDTO.getUsername());
    }


    public void updateStatus(Long userId, Integer userStatus) {
        User user = getById(userId);
        user.setUserStatus(userStatus);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    @CacheEvict(value = "userCache", key = "'userInfo:' + T(com.easy.utils.ThreadLocalUtil).getUserId()")
    public Result deleteUser(Long userId) {
        if(userId== null) return Result.error("用户ID不能为空");

        User user = baseMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");

        // 删除用户头像
        String avatar = user.getUserAvatar();
        if (StrUtil.isNotBlank(avatar)) {
            minioTemplate.deleteFile(avatar);
        }

        return Result.success(baseMapper.deleteById(userId) == 1 ? "已删除用户"+user.getUsername() : "用户删除失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public Result deleteUsers(List<Long> userIds) {
        // 批量查询用户
        List<User> userList = baseMapper.selectByIds(userIds);
        List<String> userNames = userList.stream().map(User::getUsername).toList();
        List<Long> userIdList = userList.stream().map(User::getUserId).toList();

        if(userList.isEmpty()){
            return Result.success("未找到相关数据");
        }

        // 删除用户头像
        for (User user : userList) {
            if (StrUtil.isNotBlank(user.getUserAvatar())) {
                minioTemplate.deleteFile(user.getUserAvatar());
            }
        }

        if (baseMapper.deleteByIds(userIdList) == 0) {
            return Result.error("用户批量删除失败");
        }


        return Result.success("已删除用户"+userNames);
    }

    @Override
    public String loginByPassword(UserPasswordLoginDTO userLoginDTO) {
        // 根据用户名查询用户信息
        User user = getOne(new QueryWrapper<User>().eq("username", userLoginDTO.getUsername()));
        if (user == null) {
            throw new AccessDeniedException("账号不存在");
        }
        if (user.getUserStatus() == 0){
            throw new AccessDeniedException("账号已被冻结");
        }

        Long userId = user.getUserId();
        // 4.1.生成Jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
        claims.put(JwtClaimsConstant.USER_ID, userId);
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        String token = JwtUtil.generateToken(claims);
        // 4.2.将token存入Redis
        stringRedisTemplate.opsForValue().set("login:"+RoleEnum.USER.getRole()+":" + userId, token, 3, TimeUnit.HOURS);

        // 5.返回结果
        return token;

    }

    @Override
    public String loginByEmail(UserEmailLoginDTO userLoginDTO) {

        String email = userLoginDTO.getEmail();

        User user = getOne(new QueryWrapper<User>().eq("email", email));

        if (user.getUserStatus() == 0){
            throw new AccessDeniedException("账号已被冻结");
        }

        String code=stringRedisTemplate.opsForValue().get("verificationCode:login:" + email);
        if(code==null){
            throw new AccessDeniedException("验证码已过期");
        }
        if(!code.equals(userLoginDTO.getVerificationCode())){
            throw new AccessDeniedException("验证码错误");
        }

        Long userId = user.getUserId();
        // 4.1.生成Jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
        claims.put(JwtClaimsConstant.USER_ID, userId);
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        String token = JwtUtil.generateToken(claims);
        // 4.2.将token存入Redis
        stringRedisTemplate.opsForValue().set("login:"+RoleEnum.USER.getRole()+":" + userId, token, 3, TimeUnit.HOURS);

        // 5.返回结果
        return token;
    }


    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO userRegisterDTO) {

        String email = userRegisterDTO.getEmail();

        // 1.校验验证码
        String code = stringRedisTemplate.opsForValue().get("verificationCode:register:" + email);

        if(code==null){
            throw new AccessDeniedException("验证码已过期");
        }
        if(!code.equals(userRegisterDTO.getVerificationCode())){
            throw new AccessDeniedException("验证码错误");
        }

        User user = getOne(new QueryWrapper<User>().eq("email", email));

        if (user != null){
            throw new AccessDeniedException("邮箱已存在");
        }

        user = new User();
        user.setEmail(email);
        user.setUsername(userRegisterDTO.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()));
        user.setUserStatus(1);
        user.setUserAvatar("users/avatar.webp");
        save(user);


    }

    @Override
    public void sendVerificationCode(String email,Integer operationType) {

        String verificationCode = emailService.sendVerificationCodeEmail(email);
        if (verificationCode == null) {
            throw new RuntimeException("发送验证码失败");
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

    }

    @Override
    public void logout() {
        Long userId = ThreadLocalUtil.getUserId();
        Boolean delete = stringRedisTemplate.delete("login:"+RoleEnum.USER.getRole()+":" + userId);
        if (!delete){
            throw new RuntimeException("用户未登录");
        }
    }

    @Override
    @Cacheable(cacheNames = "userCache",
            key = "'userInfo:' + (#userId != null ? #userId : T(com.easy.utils.ThreadLocalUtil).getUserId())")
    public UserVO userInfo(Long userId) {

        if (userId == null){
            userId = ThreadLocalUtil.getUserId();
        }

        User user = getById(userId);
        UserVO userVO = new UserVO();

        BeanUtils.copyProperties(user, userVO);
        if (userId == null){
            userVO.setEmail(null);
        }

        // 统计用户歌单创建数、歌曲收藏数、歌单创建数
        UserStatVO userStatVO = statService.getUserStat(userId);

        BeanUtils.copyProperties(userStatVO, userVO);

        return userVO;
    }

    @Override
    @CacheEvict(value = "userCache", key = "'userInfo:' + T(com.easy.utils.ThreadLocalUtil).getUserId()")
    public void updateUserInfo(UserDTO userDTO) {

        Long userId =ThreadLocalUtil.getUserId();

        userDTO.setUserId(userId);

        User user = getById(userId);

        String username = userDTO.getUsername();
        String phone = userDTO.getPhone();

        if (baseMapper.selectCount(new QueryWrapper<User>().eq("username",username))>0&&!user.getUsername().equals(username)){
            throw new BaseException("用户名已存在");
        }

        if (baseMapper.selectCount(new QueryWrapper<User>().eq("phone",phone))>0&&!user.getPhone().equals(phone)){
            throw new BaseException("手机号已存在");
        }

        BeanUtil.copyProperties(userDTO, user);

        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void updateUserPassword(UserResetPasswordDTO userResetPasswordDTO) {
        // Redis中查询验证码是否合法
        String code = stringRedisTemplate.opsForValue().get("verificationCode:updatePassword:" + userResetPasswordDTO.getEmail());


        if(code==null){
            throw new AccessDeniedException("验证码已过期");
        }
        if(!code.equals(userResetPasswordDTO.getVerificationCode())){
            throw new AccessDeniedException("验证码错误");
        }

        String email = userResetPasswordDTO.getEmail();
        User user = baseMapper.selectOne(new QueryWrapper<User>().eq("email", email));

        Long userId = user.getUserId();
        String newPassword = userResetPasswordDTO.getNewPassword();
        String repeatPassword = userResetPasswordDTO.getRepeatPassword();
        if (!newPassword.equals(repeatPassword)){
            throw new AccessDeniedException("两次密码不一致");
        }

        user.setPassword(DigestUtils.md5DigestAsHex(userResetPasswordDTO.getNewPassword().getBytes()));
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);

        // 删除Redis中的token
        stringRedisTemplate.delete("login:user:" + userId);
    }


    @Override
    public void updateUserEmail(UserEmailUpdateDTO updateDTO) {
        Long userId = ThreadLocalUtil.getUserId();

        String newEmail = updateDTO.getNewEmail();

        if (baseMapper.selectCount(new QueryWrapper<User>().eq("email",newEmail))>0){
            throw new BaseException("邮箱已存在");
        }

        // Redis中查询验证码是否合法
        String code = stringRedisTemplate.opsForValue().get("verificationCode:updateEmail:" + newEmail);

        if(code==null){
            throw new AccessDeniedException("验证码已过期");
        }
        if(!code.equals(updateDTO.getVerificationCode())){
            throw new AccessDeniedException("验证码错误");
        }

        User user = getById(userId);

        if (newEmail.equals(user.getEmail())){
            throw new AccessDeniedException("新邮箱不能与旧邮箱相同");
        }

        user.setEmail(newEmail);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);

    }


    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "userCache", key = "'userInfo:' + T(com.easy.utils.ThreadLocalUtil).getUserId()")
    public void updateAvatar(Long userId, MultipartFile avatar) {
        if (userId==null){
            userId = ThreadLocalUtil.getUserId();
        }
        String avatarUrl = minioTemplate.uploadFile(avatar, "users");

        User user = getById(userId);
        user.setUserAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }


    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "userCache", key = "'userInfo:' + T(com.easy.utils.ThreadLocalUtil).getUserId()")
    public void deleteAccount(String token) {
        Long userId = ThreadLocalUtil.getUserId();

        stringRedisTemplate.delete("login:user:" + userId);

        User user = getById(userId);

        // 删除头像
        minioTemplate.deleteFile(user.getUserAvatar());

        removeById(userId);

    }

    @Override
    public PageResult search(String username, PageQueryDTO pageQueryDTO) {
        Page<User> page = new Page<>(pageQueryDTO.getPageNum(), pageQueryDTO.getPageSize());
        Page<User> result = lambdaQuery().like(User::getUsername, username)
                .page(page);
        List<User> list = result.getRecords();
        List<UserVO> voList = new ArrayList<>();
        for (User user : list) {
            UserVO userVO = new UserVO();
            BeanUtil.copyProperties(user, userVO);
            voList.add(userVO);
        }
        return new PageResult(result.getTotal(), voList);
    }

}
