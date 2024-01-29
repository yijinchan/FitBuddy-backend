package com.yijinchan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.model.domain.Team;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.domain.UserTeam;
import com.yijinchan.model.enums.TeamStatusEnum;
import com.yijinchan.model.request.TeamJoinRequest;
import com.yijinchan.model.request.TeamQueryRequest;
import com.yijinchan.model.request.TeamQuitRequest;
import com.yijinchan.model.request.TeamUpdateRequest;
import com.yijinchan.model.vo.TeamVO;
import com.yijinchan.model.vo.UserVO;
import com.yijinchan.service.TeamService;
import com.yijinchan.mapper.TeamMapper;
import com.yijinchan.service.UserService;
import com.yijinchan.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static com.yijinchan.constant.SystemConstants.PAGE_SIZE;

/**
 * @author jinchan
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-01-18 20:53:25
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {
    @Resource
    private UserTeamService userTeamService;
    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1.请求参数是否为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.判断用户是否登录
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (team.getExpireTime()!=null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(team.getExpireTime());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            team.setExpireTime(calendar.getTime());
        }else {
            team.setExpireTime(null);
        }
        final long userId = loginUser.getId();
        //3.校验信息
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建5只队伍");
        }
        //1.队伍人数 > 1 && <= 5
        int maxNun = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNun < 1 || maxNun > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数必须在1-5之间");
        }
        //2.队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题不能超过20个字符");
        }
        //3.队伍描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isBlank(description) || description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述不能超过512个字符");
        }
        //4.status 是否公开（int），不传默认为 0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if (statusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //5.如果队伍是加密状态，则需要校验密码，且密码 <= 32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(statusEnum)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //6.超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍过期时间不能小于当前时间");
        }
        //7.插入用户 => 队伍、用户关系表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        //8. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }
        //只有管理员和队伍创建者可以修改
        if (!userService.isAdmin(loginUser) && !oldTeam.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有权限修改该队伍");
        }
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        if (TeamStatusEnum.SECRET.equals(enumByValue) && StringUtils.isBlank(teamUpdateRequest.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密房间必须要设置密码");
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        return this.updateById(updateTeam);
    }

    /**
     * 根据条件查询团队列表
     *
     * @param teamQuery 团队查询条件
     * @param isAdmin   是否是管理员
     * @return 查询到的团队用户VO列表
     */
    @Override
    public Page<TeamVO> listTeams(long currentPage, TeamQueryRequest teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id); // 根据id查询
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList); // 根据id列表查询
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText)); // 根据搜索文本模糊匹配团队名或描述
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.eq("name", name); // 根据团队名查询
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.eq("description", description); // 根据团队描述查询
            }
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("max_num", maxNum); // 根据最大人数查询
            }
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("user_id", userId); // 根据用户id查询
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if (statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH); // 未授权异常
            }
            queryWrapper.eq("status", statusEnum.getValue()); // 根据状态查询
        }
        queryWrapper.and(qw -> qw.gt("expire_time", new Date()).or().isNull("expireTime")); // 根据过期时间和过期状态查询
        Page<Team> teamPage = this.page(new Page<>(currentPage, PAGE_SIZE),queryWrapper);
        if (CollectionUtils.isEmpty(teamPage.getRecords())) {
            return new Page<>();
        }
        Page<TeamVO> teamVOPage = new Page<>();
        BeanUtils.copyProperties(teamPage,teamVOPage,"records");
        List<Team> teamPageRecords = teamPage.getRecords();
        ArrayList<TeamVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamPageRecords) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue; // 团队用户id为空则跳过
            }
            User user = userService.getById(userId); // 根据用户id查询用户
            TeamVO teamUserVO = new TeamVO(); // 团队用户VO
            BeanUtils.copyProperties(team, teamUserVO); // 复制团队信息到团队用户VO
            if (user != null) {
                UserVO userVO = new UserVO(); // 用户VO
                BeanUtils.copyProperties(user, userVO); // 复制用户信息到用户VO
                teamUserVO.setCreateUser(userVO); // 设置创建用户
            }
            teamUserVOList.add(teamUserVO); // 添加团队用户VO到列表
        }
        teamVOPage.setRecords(teamUserVOList);
        return teamVOPage;
    }

    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if (expireTime != null && new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if (TeamStatusEnum.PRIVATE.equals(enumByValue)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "加密队伍不能加入");
        }
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(enumByValue)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
        }
        //不能加入超过5个队伍
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("user_id", userId);
        long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if (hasJoinNum > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能创建和加入超过5个队伍");
        }
        //不能重复加入队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        userTeamQueryWrapper.eq("user_id", userId);
        long hasJoinCount = userTeamService.count(userTeamQueryWrapper);
        if (hasJoinCount > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "已经加入该队伍");
        }
        //已加入队伍的人数不能超过最大人数
        long countTeamUserByTeamId = this.countTeamUserByTeamId(teamId);
        if (countTeamUserByTeamId >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数已满");
        }
        //修改队伍信息
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeamById(teamId);
        Long userId = loginUser.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(userTeam);
        long count = userTeamService.count(queryWrapper);
        if (count == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有加入该队伍");
        }
        long teamHasJoinNum = this.countTeamUserByTeamId(teamId);
        if (teamHasJoinNum == 1) {
            this.removeById(teamId);
        } else {
            //如果是队长，即创建队伍的人
            if (team.getUserId().equals(userId)) {
                //把队伍转让给最早加入的用户
                //1.查询所有加入该队伍的人
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("team_id", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //2.更新当前队伍的队长
                Team teamUpdate = new Team();
                teamUpdate.setId(teamId);
                teamUpdate.setUserId(nextTeamLeaderId);
                boolean updateById = this.updateById(teamUpdate);
                if(!updateById){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍队长失败");
                }

            }
        }
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser, boolean isAdmin) {
        // 校验队伍是否存在
        Team team = getTeamById(id);
        long teamId = team.getId();
        // 校验你是不是队伍的队长
        if (isAdmin){
            // 移除所有加入队伍的关联信息
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("team_id", teamId);
            boolean result = userTeamService.remove(userTeamQueryWrapper);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
            }
            this.removeById(teamId);
        }
        if (!team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }
    /**
     * 根据团队ID和用户ID获取团队信息
     *
     * @param teamId 团队ID
     * @param userId 用户ID
     * @return TeamVO 团队信息
     */
    @Override
    public TeamVO getTeam(Long teamId, Long userId) {
        // 根据团队ID获取团队对象
        Team team = this.getById(teamId);
        // 创建一个TeamVO对象
        TeamVO teamVO = new TeamVO();
        // 将团队对象的属性复制到TeamVO对象中
        BeanUtils.copyProperties(team, teamVO);

        // 创建一个LambdaQueryWrapper对象用于查询条件
        LambdaQueryWrapper<UserTeam> userTeamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 根据团队ID查询用户团队的数量
        long count = userTeamService.count(userTeamLambdaQueryWrapper.eq(UserTeam::getTeamId, teamId));
        // 将用户团队数量设置到TeamVO对象中的hasJoinNum属性中
        teamVO.setHasJoinNum(count);

        // 根据用户ID查询用户团队的数量
        long userJoin = userTeamService.count(userTeamLambdaQueryWrapper.eq(UserTeam::getUserId, userId));
        // 判断用户是否已加入团队，将结果设置到TeamVO对象中的hasJoin属性中
        teamVO.setHasJoin(userJoin > 0);

        // 根据团队ID查询用户信息
        User leader = userService.getById(team.getUserId());
        // 将leader的用户名设置到TeamVO对象中的leaderName属性中
        teamVO.setLeaderName(leader.getUsername());

        // 返回团队信息
        return teamVO;
    }

    @Override
    public Page<TeamVO> listMyJoin(long currentPage, TeamQueryRequest teamQuery) {
        List<Long> idList = teamQuery.getIdList();
        LambdaQueryWrapper<Team> teamLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teamLambdaQueryWrapper.in(Team::getId,idList);
        Page<Team> teamPage = this.page(new Page<>(currentPage, PAGE_SIZE), teamLambdaQueryWrapper);
        if (CollectionUtils.isEmpty(teamPage.getRecords())) {
            return new Page<>();
        }
        Page<TeamVO> teamVOPage = new Page<>();
        // 关联查询创建人的用户信息
        BeanUtils.copyProperties(teamPage, teamVOPage, "records");
        List<Team> teamPageRecords = teamPage.getRecords();
        ArrayList<TeamVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamPageRecords) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamVO teamUserVO = new TeamVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        teamVOPage.setRecords(teamUserVOList);
        return teamVOPage;
    }

    /**
     * 根据 id 获取队伍信息
     *
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return team;
    }

    /**
     * 获取某队伍当前人数
     *
     * @param teamId
     * @return
     */
    private long countTeamUserByTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}





