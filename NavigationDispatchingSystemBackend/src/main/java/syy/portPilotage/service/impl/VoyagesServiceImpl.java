package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import syy.portPilotage.entity.Voyages;
import syy.portPilotage.mapper.VoyagesMapper;
import syy.portPilotage.service.VoyagesService;
@Service
public class VoyagesServiceImpl extends ServiceImpl<VoyagesMapper, Voyages>
        implements VoyagesService {

}