package syy.portPilotage.service.screen.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.BillingVouchers;
import syy.portPilotage.entity.PilotagePlans;
import syy.portPilotage.entity.SystemUpdateHistory;
import syy.portPilotage.entity.Users;
import syy.portPilotage.mapper.SystemUpdateHistoryMapper;
import syy.portPilotage.service.BillingVouchersService;
import syy.portPilotage.service.PilotagePlansService;
import syy.portPilotage.service.UsersService;
import syy.portPilotage.service.screen.ConsoleService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ConsoleServiceImpl implements ConsoleService {

    @Autowired
    private BillingVouchersService billingVouchersService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private PilotagePlansService pilotagePlansService;
    @Autowired
    private SystemUpdateHistoryMapper systemUpdateHistoryMapper;

    @Override
    public Result<List<Map<String, Object>>> getSystemLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        List<Map<String, Object>> events = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        List<SystemUpdateHistory> updates = systemUpdateHistoryMapper.selectPage(0, 5, null, 1);
        for (SystemUpdateHistory update : updates) {
            Date time = update.getPublishedAt() != null ? update.getPublishedAt() : update.getCreatedAt();
            String title = update.getTitle() != null ? update.getTitle() : "系统更新";
            String version = update.getVersion() != null ? update.getVersion() : "";
            addLogEvent(events, time, "System", "INFO", "发布更新 " + version + " " + title);
        }
        
        List<BillingVouchers> vouchers = billingVouchersService.list(
            new LambdaQueryWrapper<BillingVouchers>()
                .orderByDesc(BillingVouchers::getCreatedAt)
                .last("LIMIT 5")
        );
        for (BillingVouchers voucher : vouchers) {
            Date time = voucher.getCreatedAt();
            String code = voucher.getVoucherCode() != null ? voucher.getVoucherCode() : "V" + voucher.getId();
            addLogEvent(events, time, "Billing", "INFO", "生成计费凭证 " + code);
        }
        
        List<PilotagePlans> plans = pilotagePlansService.list(
            new LambdaQueryWrapper<PilotagePlans>()
                .orderByDesc(PilotagePlans::getCreatedAt)
                .last("LIMIT 5")
        );
        for (PilotagePlans plan : plans) {
            Date time = plan.getUpdatedAt() != null ? plan.getUpdatedAt() : plan.getCreatedAt();
            String code = plan.getPlanCode() != null ? plan.getPlanCode() : "P" + plan.getId();
            String level = "DELAYED".equals(plan.getPlanStatus()) ? "WARNING" : "INFO";
            String content = "引航计划 " + code + " 状态: " + (plan.getPlanStatus() != null ? plan.getPlanStatus() : "UNKNOWN");
            addLogEvent(events, time, "Pilotage", level, content);
        }
        
        List<Users> recentLogins = usersService.list(
            new LambdaQueryWrapper<Users>()
                .isNotNull(Users::getLastLoginAt)
                .orderByDesc(Users::getLastLoginAt)
                .last("LIMIT 5")
        );
        for (Users user : recentLogins) {
            Date time = user.getLastLoginAt();
            String username = user.getUsername() != null ? user.getUsername() : user.getAccount();
            addLogEvent(events, time, "Auth", "INFO", "用户 " + (username != null ? username : "未知") + " 登录");
        }
        
        events.sort((a, b) -> {
            Date timeA = (Date) a.get("timestamp");
            Date timeB = (Date) b.get("timestamp");
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });
        
        int id = 1;
        for (Map<String, Object> event : events) {
            if (logs.size() >= 10) {
                break;
            }
            Map<String, Object> log = new HashMap<>();
            Date time = (Date) event.get("timestamp");
            log.put("id", id++);
            log.put("module", event.get("module"));
            log.put("content", event.get("content"));
            log.put("level", event.get("level"));
            log.put("time", time != null ? formatter.format(time) : "");
            logs.add(log);
        }
        
        return Result.success(logs);
    }

    @Override
    public Result<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalUsers = usersService.count();
        stats.put("totalUsers", totalUsers);
        
        Date now = new Date();
        Date onlineThreshold = new Date(now.getTime() - 30L * 60L * 1000L);
        long onlineUsers = usersService.count(
            new LambdaQueryWrapper<Users>()
                .isNotNull(Users::getLastLoginAt)
                .ge(Users::getLastLoginAt, onlineThreshold)
        );
        stats.put("onlineUsers", onlineUsers);
        
        List<Users> usersList = usersService.list();
        Map<String, Long> deptCounts = new LinkedHashMap<>();
        for (Users user : usersList) {
            String department = user.getDepartment();
            String key = (department == null || department.trim().isEmpty()) ? "未分配" : department.trim();
            deptCounts.put(key, deptCounts.getOrDefault(key, 0L) + 1L);
        }
        
        List<Map<String, Object>> departments = new ArrayList<>();
        for (Map.Entry<String, Long> entry : deptCounts.entrySet()) {
            Map<String, Object> dept = new HashMap<>();
            dept.put("name", entry.getKey());
            dept.put("value", entry.getValue());
            departments.add(dept);
        }
        stats.put("departments", departments);
        return Result.success(stats);
    }
    
    private void addLogEvent(List<Map<String, Object>> events, Date time, String module, String level, String content) {
        Map<String, Object> event = new HashMap<>();
        event.put("timestamp", time);
        event.put("module", module);
        event.put("level", level);
        event.put("content", content);
        events.add(event);
    }
}