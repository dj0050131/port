package syy.portPilotage.controller.base;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import syy.portPilotage.common.Result;
import syy.portPilotage.entity.ChargeCategories;
import syy.portPilotage.service.ChargeCategoriesService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/base/chargeCategories")
@RequiredArgsConstructor
@Tag(name = "收费项目分类管理", description = "收费项目分类相关接口")
public class ChargeCategoriesController {

    @Autowired
    private ChargeCategoriesService chargeCategoriesService;

    @GetMapping("/list")
    @Operation(summary = "获取收费项目分类列表")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','AGENT','FINANCE')")
    public Result<List<ChargeCategories>> list() {
        return Result.success(chargeCategoriesService.list());
    }
}
