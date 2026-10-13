package syy.portPilotage.service.screen.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.dto.gantt.*;
import syy.portPilotage.dto.gantt.ConflictCheckRequestDTO;
import syy.portPilotage.dto.gantt.ResourceConflictDTO;
import syy.portPilotage.entity.*;
import syy.portPilotage.enums.PilotageStatusEnum;
import syy.portPilotage.enums.PlanStatusEnum;
import syy.portPilotage.service.*;
import syy.portPilotage.service.screen.GanttChartService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GanttChartServiceImpl implements GanttChartService {

    @Autowired
    private PilotagePlansService pilotagePlansService;
    
    @Autowired
    private PilotsService pilotsService;
    
    @Autowired
    private TugboatsService tugboatsService;
    
    @Autowired
    private PilotBoatsService pilotBoatsService;
    
    @Autowired
    private DriversService driversService;
    
    @Autowired
    private VehiclesService vehiclesService;
    
    @Autowired
    private PlanPilotAssignmentsService planPilotAssignmentsService;
    
    @Autowired
    private PlanTugboatAssignmentsService planTugboatAssignmentsService;
    
    @Autowired
    private PlanPilotBoatAssignmentsService planPilotBoatAssignmentsService;
    
    @Autowired
    private PlanDriverAssignmentsService planDriverAssignmentsService;
    
    @Autowired
    private PlanVehicleAssignmentsService planVehicleAssignmentsService;

    @Override
    public List<ResourceTimelineDTO> getResourceTimeline(Date startDate, Date endDate, String resourceType) {
        log.info("获取资源时间轴数据: startDate={}, endDate={}, resourceType={}", startDate, endDate, resourceType);
        
        List<ResourceTimelineDTO> result = new ArrayList<>();
        
        switch (resourceType != null ? resourceType.toUpperCase() : "ALL") {
            case "PILOT":
                result.addAll(getPilotTimeline(startDate, endDate));
                break;
            case "TUGBOAT":
                result.addAll(getTugboatTimeline(startDate, endDate));
                break;
            case "PILOT_BOAT":
                result.addAll(getPilotBoatTimeline(startDate, endDate));
                break;
            case "DRIVER":
                result.addAll(getDriverTimeline(startDate, endDate));
                break;
            case "VEHICLE":
                result.addAll(getVehicleTimeline(startDate, endDate));
                break;
            case "ALL":
            default:
                result.addAll(getPilotTimeline(startDate, endDate));
                result.addAll(getTugboatTimeline(startDate, endDate));
                result.addAll(getPilotBoatTimeline(startDate, endDate));
                result.addAll(getDriverTimeline(startDate, endDate));
                result.addAll(getVehicleTimeline(startDate, endDate));
                break;
        }
        
        return result;
    }

    @Override
    public GanttChartDataDTO getGanttData(Date startDate, Date endDate) {
        log.info("获取甘特图数据: startDate={}, endDate={}", startDate, endDate);
        
        GanttChartDataDTO ganttData = new GanttChartDataDTO();
        
        List<GanttResourceDTO> resources = getAllResources();
        ganttData.setResources(resources);
        
        List<GanttTaskDTO> tasks = getTasksForDateRange(startDate, endDate);
        ganttData.setTasks(tasks);
        
        GanttTimelineDTO timeline = new GanttTimelineDTO();
        timeline.setStartDate(startDate);
        timeline.setEndDate(endDate);
        timeline.setTimeUnit("HOUR");
        timeline.setTimeInterval(1);
        timeline.setShowWeekends(true);
        timeline.setShowHolidays(true);
        ganttData.setTimeline(timeline);
        
        return ganttData;
    }

    @Override
    public Boolean updatePlanTime(Long planId, Date newStartTime, Date newEndTime) {
        log.info("更新计划时间: planId={}, newStartTime={}, newEndTime={}", planId, newStartTime, newEndTime);
        
        try {
            PilotagePlans plan = pilotagePlansService.getById(planId);
            if (plan == null) {
                log.error("计划不存在: planId={}", planId);
                return false;
            }
            
            plan.setPlanStartTime(newStartTime);
            plan.setPlanEndTime(newEndTime);
            plan.setUpdatedAt(new Date());
            
            boolean success = pilotagePlansService.updateById(plan);
            
            if (success) {
                log.info("计划时间更新成功: planId={}", planId);
            } else {
                log.error("计划时间更新失败: planId={}", planId);
            }
            
            return success;
        } catch (Exception e) {
            log.error("更新计划时间时发生错误: planId={}", planId, e);
            return false;
        }
    }

    @Override
    public List<ResourceConflictDTO> checkConflicts(ConflictCheckRequestDTO request) {
        log.info("检查资源冲突: request={}", request);
        
        List<ResourceConflictDTO> conflicts = new ArrayList<>();
        
        if (request.getResources() == null || request.getResources().isEmpty()) {
            return conflicts;
        }
        
        for (ResourceCheckItemDTO resource : request.getResources()) {
            List<ResourceConflictDTO> resourceConflicts = checkResourceConflict(
                resource.getResourceId(), 
                resource.getResourceType(), 
                resource.getStartTime(), 
                resource.getEndTime(),
                request.getPlanId()
            );
            conflicts.addAll(resourceConflicts);
        }
        
        return conflicts;
    }

    @Override
    public Object getResourceOverview(Date startDate, Date endDate) {
        log.info("获取资源概览: startDate={}, endDate={}", startDate, endDate);
        
        Map<String, Object> overview = new HashMap<>();
        
        Map<String, Object> pilotStats = getResourceStats("PILOT", startDate, endDate);
        Map<String, Object> tugboatStats = getResourceStats("TUGBOAT", startDate, endDate);
        Map<String, Object> pilotBoatStats = getResourceStats("PILOT_BOAT", startDate, endDate);
        Map<String, Object> driverStats = getResourceStats("DRIVER", startDate, endDate);
        Map<String, Object> vehicleStats = getResourceStats("VEHICLE", startDate, endDate);
        
        overview.put("pilots", pilotStats);
        overview.put("tugboats", tugboatStats);
        overview.put("pilotBoats", pilotBoatStats);
        overview.put("drivers", driverStats);
        overview.put("vehicles", vehicleStats);
        overview.put("totalResources", 
            ((Long) pilotStats.get("total")) +
            ((Long) tugboatStats.get("total")) +
            ((Long) pilotBoatStats.get("total")) +
            ((Long) driverStats.get("total")) +
            ((Long) vehicleStats.get("total")));
        
        return overview;
    }
    
    private List<ResourceTimelineDTO> getPilotTimeline(Date startDate, Date endDate) {
        List<ResourceTimelineDTO> timelineList = new ArrayList<>();
        
        List<Pilots> pilots = pilotsService.list();
        for (Pilots pilot : pilots) {
            ResourceTimelineDTO timeline = new ResourceTimelineDTO();
            timeline.setResourceId(pilot.getId());
            timeline.setResourceName(pilot.getPilotName());
            timeline.setResourceType("PILOT");
            
            List<ResourceTaskDTO> tasks = getTasksForResource(pilot.getId(), "PILOT", startDate, endDate);
            timeline.setTasks(tasks);
            timeline.setStartDate(startDate);
            timeline.setEndDate(endDate);
            
            Double availability = calculateAvailability(tasks, startDate, endDate);
            timeline.setAvailabilityPercentage(availability);
            
            timelineList.add(timeline);
        }
        
        return timelineList;
    }
    
    private List<ResourceTimelineDTO> getTugboatTimeline(Date startDate, Date endDate) {
        List<ResourceTimelineDTO> timelineList = new ArrayList<>();
        
        List<Tugboats> tugboats = tugboatsService.list();
        for (Tugboats tugboat : tugboats) {
            ResourceTimelineDTO timeline = new ResourceTimelineDTO();
            timeline.setResourceId(tugboat.getId());
            timeline.setResourceName(tugboat.getTugboatName());
            timeline.setResourceType("TUGBOAT");
            
            List<ResourceTaskDTO> tasks = getTasksForResource(tugboat.getId(), "TUGBOAT", startDate, endDate);
            timeline.setTasks(tasks);
            timeline.setStartDate(startDate);
            timeline.setEndDate(endDate);
            
            Double availability = calculateAvailability(tasks, startDate, endDate);
            timeline.setAvailabilityPercentage(availability);
            
            timelineList.add(timeline);
        }
        
        return timelineList;
    }
    
    private List<ResourceTimelineDTO> getPilotBoatTimeline(Date startDate, Date endDate) {
        List<ResourceTimelineDTO> timelineList = new ArrayList<>();
        
        List<PilotBoats> pilotBoats = pilotBoatsService.list();
        for (PilotBoats pilotBoat : pilotBoats) {
            ResourceTimelineDTO timeline = new ResourceTimelineDTO();
            timeline.setResourceId(pilotBoat.getId());
            timeline.setResourceName(pilotBoat.getBoatName());
            timeline.setResourceType("PILOT_BOAT");
            
            List<ResourceTaskDTO> tasks = getTasksForResource(pilotBoat.getId(), "PILOT_BOAT", startDate, endDate);
            timeline.setTasks(tasks);
            timeline.setStartDate(startDate);
            timeline.setEndDate(endDate);
            
            Double availability = calculateAvailability(tasks, startDate, endDate);
            timeline.setAvailabilityPercentage(availability);
            
            timelineList.add(timeline);
        }
        
        return timelineList;
    }
    
    private List<ResourceTimelineDTO> getDriverTimeline(Date startDate, Date endDate) {
        List<ResourceTimelineDTO> timelineList = new ArrayList<>();
        
        List<Drivers> drivers = driversService.list();
        for (Drivers driver : drivers) {
            ResourceTimelineDTO timeline = new ResourceTimelineDTO();
            timeline.setResourceId(driver.getId());
            timeline.setResourceName(driver.getName());
            timeline.setResourceType("DRIVER");
            
            List<ResourceTaskDTO> tasks = getTasksForResource(driver.getId(), "DRIVER", startDate, endDate);
            timeline.setTasks(tasks);
            timeline.setStartDate(startDate);
            timeline.setEndDate(endDate);
            
            Double availability = calculateAvailability(tasks, startDate, endDate);
            timeline.setAvailabilityPercentage(availability);
            
            timelineList.add(timeline);
        }
        
        return timelineList;
    }
    
    private List<ResourceTimelineDTO> getVehicleTimeline(Date startDate, Date endDate) {
        List<ResourceTimelineDTO> timelineList = new ArrayList<>();
        
        List<Vehicles> vehicles = vehiclesService.list();
        for (Vehicles vehicle : vehicles) {
            ResourceTimelineDTO timeline = new ResourceTimelineDTO();
            timeline.setResourceId(vehicle.getId());
            timeline.setResourceName(vehicle.getVehicleModel());
            timeline.setResourceType("VEHICLE");
            
            List<ResourceTaskDTO> tasks = getTasksForResource(vehicle.getId(), "VEHICLE", startDate, endDate);
            timeline.setTasks(tasks);
            timeline.setStartDate(startDate);
            timeline.setEndDate(endDate);
            
            Double availability = calculateAvailability(tasks, startDate, endDate);
            timeline.setAvailabilityPercentage(availability);
            
            timelineList.add(timeline);
        }
        
        return timelineList;
    }
    
    private List<ResourceTaskDTO> getTasksForResource(Long resourceId, String resourceType, Date startDate, Date endDate) {
        List<ResourceTaskDTO> tasks = new ArrayList<>();
        
        List<PilotagePlans> plans = pilotagePlansService.list();
        for (PilotagePlans plan : plans) {
            // 检查计划时间是否在指定范围内
            if (isPlanInRange(plan, startDate, endDate)) {
                boolean isAssigned = isResourceAssignedToPlan(resourceId, resourceType, plan.getId());
                if (isAssigned) {
                    ResourceTaskDTO task = new ResourceTaskDTO();
                    task.setTaskId(plan.getId());
                    task.setTaskName("引航任务 - " + plan.getPlanCode());
                    task.setPlanId(plan.getId());
                    task.setStartTime(plan.getPlanStartTime());
                    task.setEndTime(plan.getPlanEndTime());
                    task.setStatus(plan.getPlanStatus() != null ? plan.getPlanStatus() : "UNKNOWN");
                    task.setDescription(plan.getNotes());
                    task.setTaskType("PILOTAGE");
                    tasks.add(task);
                }
            }
        }
        
        return tasks;
    }
    
    private boolean isResourceAssignedToPlan(Long resourceId, String resourceType, Long planId) {
        switch (resourceType.toUpperCase()) {
            case "PILOT":
                LambdaQueryWrapper<PlanPilotAssignments> pilotWrapper = new LambdaQueryWrapper<>();
                pilotWrapper.eq(PlanPilotAssignments::getPilotId, resourceId)
                        .eq(PlanPilotAssignments::getPlanId, planId);
                return planPilotAssignmentsService.count(pilotWrapper) > 0;
            case "TUGBOAT":
                LambdaQueryWrapper<PlanTugboatAssignments> tugboatWrapper = new LambdaQueryWrapper<>();
                tugboatWrapper.eq(PlanTugboatAssignments::getTugboatId, resourceId)
                        .eq(PlanTugboatAssignments::getPlanId, planId);
                return planTugboatAssignmentsService.count(tugboatWrapper) > 0;
            case "PILOT_BOAT":
                LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
                pilotBoatWrapper.eq(PlanPilotBoatAssignments::getPilotBoatId, resourceId)
                        .eq(PlanPilotBoatAssignments::getPlanId, planId);
                return planPilotBoatAssignmentsService.count(pilotBoatWrapper) > 0;
            case "DRIVER":
                LambdaQueryWrapper<PlanDriverAssignments> driverWrapper = new LambdaQueryWrapper<>();
                driverWrapper.eq(PlanDriverAssignments::getDriverId, resourceId)
                        .eq(PlanDriverAssignments::getPlanId, planId);
                return planDriverAssignmentsService.count(driverWrapper) > 0;
            case "VEHICLE":
                LambdaQueryWrapper<PlanVehicleAssignments> vehicleWrapper = new LambdaQueryWrapper<>();
                vehicleWrapper.eq(PlanVehicleAssignments::getVehicleId, resourceId)
                        .eq(PlanVehicleAssignments::getPlanId, planId);
                return planVehicleAssignmentsService.count(vehicleWrapper) > 0;
            default:
                return false;
        }
    }
    
    // 检查计划是否在指定时间范围内
    private boolean isPlanInRange(PilotagePlans plan, Date startDate, Date endDate) {
        if (plan.getPlanStartTime() == null || plan.getPlanEndTime() == null) {
            return false;
        }
        
        return !(plan.getPlanEndTime().before(startDate) || plan.getPlanStartTime().after(endDate));
    }
    
    private List<GanttResourceDTO> getAllResources() {
        List<GanttResourceDTO> resources = new ArrayList<>();
        
        List<Pilots> pilots = pilotsService.list();
        for (Pilots pilot : pilots) {
            GanttResourceDTO resource = new GanttResourceDTO();
            resource.setId(pilot.getId());
            resource.setName(pilot.getPilotName());
            resource.setType("PILOT");
            resource.setStatus(pilot.getHealthStatus());
            resource.setDescription(pilot.getOtherExperience());
            resource.setLevel(pilot.getPilotLevel());
            resources.add(resource);
        }
        
        List<Tugboats> tugboats = tugboatsService.list();
        for (Tugboats tugboat : tugboats) {
            GanttResourceDTO resource = new GanttResourceDTO();
            resource.setId(tugboat.getId());
            resource.setName(tugboat.getTugboatName());
            resource.setType("TUGBOAT");
            resource.setStatus(String.valueOf(tugboat.getStatus()));
            resource.setDescription("拖轮代码"+tugboat.getTugboatCode());
            resource.setLevel(String.valueOf(tugboat.getTugboatType()));
            resources.add(resource);
        }
        
        List<PilotBoats> pilotBoats = pilotBoatsService.list();
        for (PilotBoats pilotBoat : pilotBoats) {
            GanttResourceDTO resource = new GanttResourceDTO();
            resource.setId(pilotBoat.getId());
            resource.setName(pilotBoat.getBoatName());
            resource.setType("PILOT_BOAT");
            resource.setStatus(String.valueOf(pilotBoat.getCurrentStatus()));
            resource.setDescription(pilotBoat.getOperationRange());
            resource.setLevel(pilotBoat.getEngineModel());
            resources.add(resource);
        }
        
        List<Drivers> drivers = driversService.list();
        for (Drivers driver : drivers) {
            GanttResourceDTO resource = new GanttResourceDTO();
            resource.setId(driver.getId());
            resource.setName(driver.getName());
            resource.setType("DRIVER");
            resource.setStatus(String.valueOf(driver.getOnDutyStatus()));
            resource.setDescription(driver.getLicenseNumber());
            resource.setLevel(driver.getLevel());
            resources.add(resource);
        }
        
        List<Vehicles> vehicles = vehiclesService.list();
        for (Vehicles vehicle : vehicles) {
            GanttResourceDTO resource = new GanttResourceDTO();
            resource.setId(vehicle.getId());
            resource.setName(vehicle.getLicensePlate());
            resource.setType("VEHICLE");
            resource.setStatus(String.valueOf(vehicle.getVehicleStatus()));
            resource.setDescription(vehicle.getMainPerformance());
            resource.setLevel(vehicle.getVehicleModel());
            resources.add(resource);
        }
        
        return resources;
    }
    
    private List<GanttTaskDTO> getTasksForDateRange(Date startDate, Date endDate) {
        List<GanttTaskDTO> tasks = new ArrayList<>();
        
        List<PilotagePlans> plans = pilotagePlansService.list();
        for (PilotagePlans plan : plans) {
            if (isPlanInRange(plan, startDate, endDate)) {
                addTasksForPlan(plan, tasks);
            }
        }
        
        return tasks;
    }
    
    private void addTasksForPlan(PilotagePlans plan, List<GanttTaskDTO> tasks) {
        LambdaQueryWrapper<PlanPilotAssignments> pilotWrapper = new LambdaQueryWrapper<>();
        pilotWrapper.eq(PlanPilotAssignments::getPlanId, plan.getId());
        List<PlanPilotAssignments> pilotAssignments = planPilotAssignmentsService.list(pilotWrapper);
        for (PlanPilotAssignments assignment : pilotAssignments) {
            Pilots pilot = pilotsService.getById(assignment.getPilotId());
            if (pilot != null) {
                GanttTaskDTO task = createTaskFromPlan(plan, assignment.getPilotId(), "PILOT", pilot.getPilotName());
                tasks.add(task);
            }
        }
        
        LambdaQueryWrapper<PlanTugboatAssignments> tugboatWrapper = new LambdaQueryWrapper<>();
        tugboatWrapper.eq(PlanTugboatAssignments::getPlanId, plan.getId());
        List<PlanTugboatAssignments> tugboatAssignments = planTugboatAssignmentsService.list(tugboatWrapper);
        for (PlanTugboatAssignments assignment : tugboatAssignments) {
            Tugboats tugboat = tugboatsService.getById(assignment.getTugboatId());
            if (tugboat != null) {
                GanttTaskDTO task = createTaskFromPlan(plan, assignment.getTugboatId(), "TUGBOAT", tugboat.getTugboatName());
                tasks.add(task);
            }
        }
        
        LambdaQueryWrapper<PlanPilotBoatAssignments> pilotBoatWrapper = new LambdaQueryWrapper<>();
        pilotBoatWrapper.eq(PlanPilotBoatAssignments::getPlanId, plan.getId());
        List<PlanPilotBoatAssignments> pilotBoatAssignments = planPilotBoatAssignmentsService.list(pilotBoatWrapper);
        for (PlanPilotBoatAssignments assignment : pilotBoatAssignments) {
            PilotBoats pilotBoat = pilotBoatsService.getById(assignment.getPilotBoatId());
            if (pilotBoat != null) {
                GanttTaskDTO task = createTaskFromPlan(plan, assignment.getPilotBoatId(), "PILOT_BOAT", pilotBoat.getBoatName());
                tasks.add(task);
            }
        }
        
        LambdaQueryWrapper<PlanDriverAssignments> driverWrapper = new LambdaQueryWrapper<>();
        driverWrapper.eq(PlanDriverAssignments::getPlanId, plan.getId());
        List<PlanDriverAssignments> driverAssignments = planDriverAssignmentsService.list(driverWrapper);
        for (PlanDriverAssignments assignment : driverAssignments) {
            Drivers driver = driversService.getById(assignment.getDriverId());
            if (driver != null) {
                GanttTaskDTO task = createTaskFromPlan(plan, assignment.getDriverId(), "DRIVER", driver.getName());
                tasks.add(task);
            }
        }
        
        LambdaQueryWrapper<PlanVehicleAssignments> vehicleWrapper = new LambdaQueryWrapper<>();
        vehicleWrapper.eq(PlanVehicleAssignments::getPlanId, plan.getId());
        List<PlanVehicleAssignments> vehicleAssignments = planVehicleAssignmentsService.list(vehicleWrapper);
        for (PlanVehicleAssignments assignment : vehicleAssignments) {
            Vehicles vehicle = vehiclesService.getById(assignment.getVehicleId());
            if (vehicle != null) {
                GanttTaskDTO task = createTaskFromPlan(plan, assignment.getVehicleId(), "VEHICLE", vehicle.getLicensePlate());
                tasks.add(task);
            }
        }
    }
    
    private GanttTaskDTO createTaskFromPlan(PilotagePlans plan, Long resourceId, String resourceType, String resourceName) {
        GanttTaskDTO task = new GanttTaskDTO();
        task.setId(plan.getId());
        task.setName(resourceName + " - " + plan.getPlanCode());
        task.setStartDate(plan.getPlanStartTime());
        task.setEndDate(plan.getPlanEndTime());
        task.setResourceId(resourceId);
        task.setResourceType(resourceType);
        task.setTaskType("PILOTAGE");
        task.setPlanId(plan.getId());
        task.setDescription(plan.getNotes());
        task.setProgress(0);

        return task;
    }
    
    private Double calculateAvailability(List<ResourceTaskDTO> tasks, Date startDate, Date endDate) {
        if (tasks.isEmpty()) {
            return 100.0; // 如果没有任务，则100%可用
        }
        
        long totalTime = endDate.getTime() - startDate.getTime();
        long occupiedTime = 0;
        
        List<Date[]> occupiedPeriods = new ArrayList<>();
        for (ResourceTaskDTO task : tasks) {
            Date taskStart = task.getStartTime();
            Date taskEnd = task.getEndTime();
            
            // 确保任务时间在指定范围内
            if (taskStart.before(startDate)) taskStart = startDate;
            if (taskEnd.after(endDate)) taskEnd = endDate;
            
            if (!taskStart.after(taskEnd)) { // 确保开始时间不晚于结束时间
                occupiedPeriods.add(new Date[]{taskStart, taskEnd});
            }
        }
        
        occupiedPeriods.sort((a, b) -> a[0].compareTo(b[0]));
        List<Date[]> mergedPeriods = new ArrayList<>();
        
        for (Date[] period : occupiedPeriods) {
            if (mergedPeriods.isEmpty()) {
                mergedPeriods.add(period);
            } else {
                Date[] lastPeriod = mergedPeriods.get(mergedPeriods.size() - 1);
                if (period[0].before(lastPeriod[1])) { // 有重叠
                    if (period[1].after(lastPeriod[1])) {
                        lastPeriod[1] = period[1]; // 扩展结束时间
                    }
                } else {
                    mergedPeriods.add(period); // 无重叠，添加新区间
                }
            }
        }
        
        for (Date[] period : mergedPeriods) {
            occupiedTime += period[1].getTime() - period[0].getTime();
        }
        
        double occupancyRate = totalTime > 0 ? (double) occupiedTime / totalTime : 0;
        return (1 - occupancyRate) * 100; // 可用性 = 1 - 占用率
    }
    
    private List<ResourceConflictDTO> checkResourceConflict(Long resourceId, String resourceType, 
                                                          Date startTime, Date endTime, Long excludePlanId) {
        List<ResourceConflictDTO> conflicts = new ArrayList<>();
        
        List<PilotagePlans> conflictingPlans = getConflictingPlans(resourceId, resourceType, startTime, endTime, excludePlanId);
        
        for (PilotagePlans plan : conflictingPlans) {
            ResourceConflictDTO conflict = new ResourceConflictDTO();
            conflict.setResourceId(resourceId);
            conflict.setResourceType(resourceType);
            conflict.setStartTime(startTime);
            conflict.setEndTime(endTime);
            conflict.setConflictingTaskId(plan.getId());
            conflict.setConflictingTaskName(plan.getPlanCode());
            conflict.setDescription("资源时间冲突：该资源在指定时间段内已被分配给其他计划");
            conflict.setSeverity("HIGH");
            
            switch (resourceType.toUpperCase()) {
                case "PILOT":
                    Pilots pilot = pilotsService.getById(resourceId);
                    conflict.setResourceName(pilot != null ? pilot.getPilotName() : "未知引航员");
                    break;
                case "TUGBOAT":
                    Tugboats tugboat = tugboatsService.getById(resourceId);
                    conflict.setResourceName(tugboat != null ? tugboat.getTugboatName() : "未知拖轮");
                    break;
                case "PILOT_BOAT":
                    PilotBoats pilotBoat = pilotBoatsService.getById(resourceId);
                    conflict.setResourceName(pilotBoat != null ? pilotBoat.getBoatName() : "未知引航艇");
                    break;
                case "DRIVER":
                    Drivers driver = driversService.getById(resourceId);
                    conflict.setResourceName(driver != null ? driver.getName() : "未知驾驶员");
                    break;
                case "VEHICLE":
                    Vehicles vehicle = vehiclesService.getById(resourceId);
                    conflict.setResourceName(vehicle != null ? vehicle.getLicensePlate() : "未知车辆");
                    break;
                default:
                    conflict.setResourceName("未知资源");
            }
            
            conflicts.add(conflict);
        }
        
        return conflicts;
    }
    
    private List<PilotagePlans> getConflictingPlans(Long resourceId, String resourceType, 
                                                   Date startTime, Date endTime, Long excludePlanId) {
        List<PilotagePlans> conflictingPlans = new ArrayList<>();
        
        List<PilotagePlans> allPlans = pilotagePlansService.list();
        for (PilotagePlans plan : allPlans) {
            // 排除当前正在修改的计划
            if (excludePlanId != null && plan.getId().equals(excludePlanId)) {
                continue;
            }
            
            // 检查时间范围是否重叠
            if (isTimeRangeOverlapping(plan.getPlanStartTime(), plan.getPlanEndTime(), startTime, endTime)) {
                if (isResourceAssignedToPlan(resourceId, resourceType, plan.getId())) {
                    conflictingPlans.add(plan);
                }
            }
        }
        
        return conflictingPlans;
    }
    
    // 检查时间范围是否重叠
    private boolean isTimeRangeOverlapping(Date start1, Date end1, Date start2, Date end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        
        return !(end1.before(start2) || start1.after(end2));
    }
    
    private Map<String, Object> getResourceStats(String resourceType, Date startDate, Date endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        long total = 0;
        long available = 0;
        
        switch (resourceType.toUpperCase()) {
            case "PILOT":
                total = pilotsService.count();
                available = calculateAvailableResourceCount(resourceType, startDate, endDate);
                break;
            case "TUGBOAT":
                total = tugboatsService.count();
                available = calculateAvailableResourceCount(resourceType, startDate, endDate);
                break;
            case "PILOT_BOAT":
                total = pilotBoatsService.count();
                available = calculateAvailableResourceCount(resourceType, startDate, endDate);
                break;
            case "DRIVER":
                total = driversService.count();
                available = calculateAvailableResourceCount(resourceType, startDate, endDate);
                break;
            case "VEHICLE":
                total = vehiclesService.count();
                available = calculateAvailableResourceCount(resourceType, startDate, endDate);
                break;
        }
        
        stats.put("total", total);
        stats.put("available", available);
        stats.put("utilizationRate", total > 0 ? (double)(total - available) / total * 100 : 0.0);
        
        return stats;
    }
    
    private long calculateAvailableResourceCount(String resourceType, Date startDate, Date endDate) {
        // 这是一个简化实现，实际应用中可能需要更复杂的逻辑
        switch (resourceType.toUpperCase()) {
            case "PILOT":
                return pilotsService.list().stream()
                    .filter(p -> "ACTIVE".equals(p.getHealthStatus()))
                    .count();
            case "TUGBOAT":
                return tugboatsService.list().stream()
                    .filter(t -> "AVAILABLE".equals(t.getStatus()))
                    .count();
            case "PILOT_BOAT":
                return pilotBoatsService.list().stream()
                    .filter(p -> "AVAILABLE".equals(p.getCurrentStatus()))
                    .count();
            case "DRIVER":
                return driversService.list().stream()
                    .filter(d -> "ACTIVE".equals(d.getOnDutyStatus()))
                    .count();
            case "VEHICLE":
                return vehiclesService.list().stream()
                    .filter(v -> "AVAILABLE".equals(v.getVehicleStatus()))
                    .count();
            default:
                return 0;
        }
    }
}