package com.clarimire.service.impl;

import com.clarimire.entity.*;
import com.clarimire.mapper.*;
import com.clarimire.service.PatrolService;
import com.clarimire.util.GeoUtil;
import com.clarimire.util.ReservoirNameUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PatrolServiceImpl implements PatrolService {

    @Autowired
    private PatrolTaskMapper patrolTaskMapper;

    @Autowired
    private PatrolRecordMapper patrolRecordMapper;

    @Autowired
    private IssueReportMapper issueReportMapper;

    @Autowired
    private CheckinPolicyMapper checkinPolicyMapper;

    @Autowired
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<PatrolTask> getTasks(String status, Integer assigneeId, String reservoirName, String taskType) {
        return patrolTaskMapper.findList(status, assigneeId, reservoirName, taskType);
    }

    @Override
    public PatrolTask getTaskById(Integer id) {
        return patrolTaskMapper.findById(id);
    }

    @Override
    public boolean createTask(PatrolTask task) {
        if (task.getStatus() == null) {
            task.setStatus("pending");
        }
        if (task.getReservoirName() != null) {
            task.setReservoirName(ReservoirNameUtil.normalize(task.getReservoirName()));
        }
        return patrolTaskMapper.insert(task) > 0;
    }

    @Override
    public boolean updateTask(PatrolTask task) {
        if (task.getReservoirName() != null) {
            task.setReservoirName(ReservoirNameUtil.normalize(task.getReservoirName()));
        }
        // 完成状态请走 completeTask，避免绕过打卡校验
        if ("completed".equals(task.getStatus()) && task.getId() != null) {
            PatrolTask existing = patrolTaskMapper.findById(task.getId());
            if (existing != null && !"completed".equals(existing.getStatus())) {
                int checkins = patrolRecordMapper.countByTaskId(task.getId());
                if (checkins <= 0) {
                    return false;
                }
            }
        }
        return patrolTaskMapper.update(task) > 0;
    }

    @Override
    public boolean deleteTask(Integer id) {
        return patrolTaskMapper.deleteById(id) > 0;
    }

    @Override
    public List<PatrolRecord> getRecords(Integer userId, String reservoirName, String locationZone,
                                         String startDate, String endDate) {
        return patrolRecordMapper.findList(userId, reservoirName, locationZone, startDate, endDate);
    }

    @Override
    public Map<String, Object> checkin(PatrolRecord record, String username) {
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.findByUsername(username);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        if (!"inspector".equals(user.getRole()) && !"admin".equals(user.getRole())) {
            result.put("success", false);
            result.put("message", "仅巡查员可打卡");
            return result;
        }

        record.setUserId(user.getId());
        record.setUsername(user.getUsername());
        record.setRealName(user.getRealName());
        if (record.getReservoirName() != null) {
            record.setReservoirName(ReservoirNameUtil.normalize(record.getReservoirName()));
        }

        CheckinPolicy policy = checkinPolicyMapper.getPolicy();
        int coreRadius = policy != null ? policy.getCoreRadiusM() : 200;
        int bufferRadius = policy != null ? policy.getBufferRadiusM() : 500;
        boolean demoMode = policy != null && policy.getDemoMode() != null && policy.getDemoMode() == 1;

        String zone = "remote";
        int distanceM = 0;
        if (record.getLat() != null && record.getLng() != null && record.getReservoirName() != null) {
            ReservoirLocation loc = checkinPolicyMapper.findByReservoirName(record.getReservoirName());
            if (loc != null) {
                double dist = GeoUtil.distanceMeters(
                        record.getLat().doubleValue(), record.getLng().doubleValue(),
                        loc.getLat().doubleValue(), loc.getLng().doubleValue());
                distanceM = (int) Math.round(dist);
                zone = GeoUtil.resolveZone(dist, coreRadius, bufferRadius);
            }
        } else if ("manual".equals(record.getCheckinMode())) {
            zone = "buffer";
        }

        if (demoMode) {
            zone = "core";
        }

        record.setLocationZone(zone);
        record.setDistanceM(distanceM);

        if ("remote".equals(zone)) {
            boolean allowed = user.getAllowRemoteCheckin() != null && user.getAllowRemoteCheckin() == 1;
            if (!allowed && (policy == null || policy.getRemoteEnabled() == null || policy.getRemoteEnabled() != 1)) {
                result.put("success", false);
                result.put("message", "异地打卡未授权，请联系管理员");
                result.put("locationZone", zone);
                result.put("distanceM", distanceM);
                return result;
            }
            if (!allowed) {
                result.put("success", false);
                result.put("message", "您暂无异地打卡权限");
                result.put("locationZone", zone);
                result.put("distanceM", distanceM);
                return result;
            }
            record.setCheckinMode("remote");
        }

        if (record.getPhotos() != null && !(record.getPhotos().startsWith("["))) {
            try {
                record.setPhotos(objectMapper.writeValueAsString(new String[]{record.getPhotos()}));
            } catch (JsonProcessingException ignored) {
            }
        }

        if (patrolRecordMapper.insert(record) <= 0) {
            result.put("success", false);
            result.put("message", "打卡失败");
            return result;
        }

        // 打卡只证明到达，不自动完成任务
        if (record.getTaskId() != null) {
            PatrolTask task = patrolTaskMapper.findById(record.getTaskId());
            if (task != null && !"completed".equals(task.getStatus()) && !"cancelled".equals(task.getStatus())) {
                if (!"in_progress".equals(task.getStatus())) {
                    task.setStatus("in_progress");
                    patrolTaskMapper.update(task);
                }
            }
        }

        result.put("success", true);
        result.put("message", "打卡成功");
        result.put("record", record);
        result.put("locationZone", zone);
        result.put("distanceM", distanceM);
        return result;
    }

    @Override
    public Map<String, Object> completeTask(Integer taskId, String username) {
        Map<String, Object> result = new HashMap<>();
        PatrolTask task = patrolTaskMapper.findById(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }
        if ("completed".equals(task.getStatus())) {
            result.put("success", true);
            result.put("message", "任务已完成");
            result.put("data", task);
            return result;
        }
        int checkins = patrolRecordMapper.countByTaskId(taskId);
        if (checkins <= 0) {
            result.put("success", false);
            result.put("message", "请先打卡后再完成任务");
            return result;
        }
        task.setStatus("completed");
        if (patrolTaskMapper.update(task) <= 0) {
            result.put("success", false);
            result.put("message", "完成失败");
            return result;
        }
        result.put("success", true);
        result.put("message", "任务已完成");
        result.put("data", task);
        return result;
    }

    @Override
    public Map<String, Object> ensureDailyTasks(Integer assigneeId) {
        Map<String, Object> result = new HashMap<>();
        if (assigneeId == null) {
            result.put("success", false);
            result.put("message", "缺少巡查员");
            return result;
        }
        User user = userMapper.findById(assigneeId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        List<PatrolTask> dailies = patrolTaskMapper.findList(null, assigneeId, null, "daily");
        boolean hasOpen = false;
        Date now = new Date();
        for (PatrolTask t : dailies) {
            if (!"completed".equals(t.getStatus()) && !"cancelled".equals(t.getStatus())) {
                hasOpen = true;
                break;
            }
        }

        // 周期 A：无进行中日常时，若近 7 天内无已完成日常，则生成一条
        if (!hasOpen) {
            boolean recentCompleted = false;
            long sevenDaysMs = TimeUnit.DAYS.toMillis(7);
            for (PatrolTask t : dailies) {
                if ("completed".equals(t.getStatus()) && t.getUpdateTime() != null) {
                    if (now.getTime() - t.getUpdateTime().getTime() < sevenDaysMs) {
                        recentCompleted = true;
                        break;
                    }
                } else if ("completed".equals(t.getStatus()) && t.getCreateTime() != null) {
                    if (now.getTime() - t.getCreateTime().getTime() < sevenDaysMs) {
                        recentCompleted = true;
                        break;
                    }
                }
            }
            if (!recentCompleted) {
                String reservoir = "密云水库";
                List<ReservoirLocation> locs = checkinPolicyMapper.findAllLocations();
                if (locs != null && !locs.isEmpty() && locs.get(0).getReservoirName() != null) {
                    reservoir = ReservoirNameUtil.normalize(locs.get(0).getReservoirName());
                }
                PatrolTask task = new PatrolTask();
                task.setTitle(reservoir + "日常巡查");
                task.setTaskType("daily");
                task.setStatus("pending");
                task.setReservoirName(reservoir);
                task.setAssigneeId(assigneeId);
                task.setAssigneeName(user.getRealName());
                task.setCreatedBy("system");
                task.setDescription("每 7 天周期日常巡查，须打卡后收尾");
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.DAY_OF_MONTH, 7);
                task.setDueTime(cal.getTime());
                patrolTaskMapper.insert(task);
            }
        }

        List<PatrolTask> list = patrolTaskMapper.findList(null, assigneeId, null, "daily");
        result.put("success", true);
        result.put("data", list);
        return result;
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingTasks", patrolTaskMapper.countByStatus("pending")
                + patrolTaskMapper.countByStatus("assigned"));
        stats.put("pendingIssues", issueReportMapper.countByStatus("pending")
                + issueReportMapper.countByStatus("reviewing"));
        stats.put("inProgressTasks", patrolTaskMapper.countByStatus("in_progress"));
        return stats;
    }
}
