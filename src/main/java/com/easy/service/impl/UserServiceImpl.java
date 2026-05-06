package com.easy.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.mapper.UserMapper;
import com.easy.pojo.dto.UserAddDTO;
import com.easy.pojo.dto.UserDTO;
import com.easy.pojo.dto.UserPageQueryDTO;
import com.easy.pojo.entity.User;
import com.easy.pojo.vo.UserAdminVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.MinIOService;
import com.easy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;

    private final MinIOService minIOService;


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

    @Transactional
    public Result deleteUsers(List<Long> userIds) {
        // 批量查询用户
        List<User> userList = userMapper.selectByIds(userIds);
        // 使用Set获取查询结果的id集合
        Set<Long> userIdSet = userList.stream().map(User::getUserId).collect(Collectors.toSet());

        List<Long> notExistUserIds = new ArrayList<>();

        for (Long userId : userIds) {
            if (!userIdSet.contains(userId)) {
                notExistUserIds.add(userId);
            }
        }

        if (userMapper.deleteByIds(userIdSet) == 0) {
            return Result.error("用户批量删除失败");
        }

        // 删除用户头像
        for (User user : userList) {
            try {
                if (StrUtil.isNotBlank(user.getUserAvatar())) {
                    minIOService.deleteFile(user.getUserAvatar());
                }
            } catch (Exception e) {
                log.error("头像删除失败，路径: {}，用户ID: {}，需手动清理",
                        user.getUserAvatar(), user.getUserId(), e);
            }
        }


        if (!notExistUserIds.isEmpty()){
            return Result.success("用户批量删除成功，以下用户不存在：" + notExistUserIds);
        }

        return Result.success("用户批量删除成功");
    }


}
