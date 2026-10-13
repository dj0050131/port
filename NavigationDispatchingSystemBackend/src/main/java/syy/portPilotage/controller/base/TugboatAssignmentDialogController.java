package syy.portPilotage.controller.base;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.base.TugboatDTO;
import syy.portPilotage.service.TugboatsService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/base/planTugboatAssignments")
@RequiredArgsConstructor
@Tag(name = "引航计划拖轮分配表", description = "引航计划拖轮分配表相关接口")
public class TugboatAssignmentDialogController {

    private final TugboatsService tugboatsService;

    @GetMapping("/availableTugs")
    @Operation(summary = "获取可用拖轮列表")
    public Result<List<TugboatDTO>> getAvailableTugboats() {
        log.info("获取可用拖轮列表");
        List<TugboatDTO> tugboats = tugboatsService.getAvailableTugboats();

        return Result.success(tugboats);
    }

    @GetMapping("/tugs")
    @Operation(summary = "获取所有拖轮列表")
    public Result<List<TugboatDTO>> getAllTugboats() {
        log.info("获取所有拖轮列表");
        List<TugboatDTO> tugboats = tugboatsService.getAllTugboats();
        return Result.success(tugboats);
    }

    @GetMapping("/tugs/{id}")
    @Operation(summary = "根据ID获取拖轮详情")
    public Result<TugboatDTO> getTugboatById(@PathVariable Long id) {
        log.info("根据ID获取拖轮详情: id={}", id);
        TugboatDTO tugboat = tugboatsService.getTugboatById(id);
        return Result.success(tugboat);
    }
}