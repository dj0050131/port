package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import syy.portPilotage.common.PageResult;
import syy.portPilotage.dto.common.PageQueryDTO;
import syy.portPilotage.entity.Agents;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;
import syy.portPilotage.service.AgentsService;
import syy.portPilotage.mapper.AgentsMapper;
import syy.portPilotage.redis.service.CacheService;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgentsServiceImpl extends ServiceImpl<AgentsMapper, Agents>
        implements AgentsService {

    private final CacheService cacheService;

    public Agents getById(Long id) {
        if (id == null) {
            return null;
        }
        Agents cached = cacheService.getAgent(id, Agents.class);
        if (cached != null) {
            log.debug("从缓存获取代理信息: id={}", id);
            return cached;
        }
        Agents agent = super.getById(id);
        if (agent != null) {
            cacheService.setAgent(id, agent);
        }
        return agent;
    }

    @Override
    public PageResult<Agents> pageAgent(PageQueryDTO query) {

        Page<Agents> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<Agents> wrapper = new LambdaQueryWrapper<>();
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            String keyword = query.getKeyword().trim();
            wrapper.like(Agents::getCompanyName, keyword)
                    .or()
                    .like(Agents::getAgentCode, keyword)
                    .or()
                    .like(Agents::getShortName, keyword)
                    .or()
                    .like(Agents::getContactPerson, keyword);
        }
        wrapper.orderByDesc(Agents::getCreatedAt);
        IPage<Agents> result = this.page(page, wrapper);
        PageResult<Agents> pageResult = PageResult.of(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                result.getRecords()
        );
        return pageResult;
    }
}
