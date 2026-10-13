package syy.portPilotage.service.screen.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.TideInfo;
import syy.portPilotage.service.TideInfoService;
import syy.portPilotage.service.screen.TideService;

@Service
public class TideServiceImpl implements TideService {

    @Autowired
    private TideInfoService tideInfoService;

    @Override
    public Result<TideInfo> getCurrent() {
        LambdaQueryWrapper<TideInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TideInfo::getCreatedAt);
        wrapper.last("LIMIT 1");
        
        TideInfo tide = tideInfoService.getOne(wrapper);
        
        return Result.success(tide);
    }

    @Override
    public Result<PageResult<TideInfo>> history(PageQueryDTO query) {
        Page<TideInfo> page = new Page<>(query.getCurrent(), query.getSize());
        
        LambdaQueryWrapper<TideInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TideInfo::getCreatedAt);
        
        IPage<TideInfo> result = tideInfoService.page(page, wrapper);
        
        PageResult<TideInfo> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
        
        return Result.success(pageResult);
    }
}