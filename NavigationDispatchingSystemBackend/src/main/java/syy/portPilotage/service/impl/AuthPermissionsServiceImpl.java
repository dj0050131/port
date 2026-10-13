package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.AuthPermissions;
import syy.portPilotage.mapper.AuthPermissionsMapper;
import syy.portPilotage.service.AuthPermissionsService;

@Service
public class AuthPermissionsServiceImpl extends ServiceImpl<AuthPermissionsMapper, AuthPermissions>
        implements AuthPermissionsService {
}
