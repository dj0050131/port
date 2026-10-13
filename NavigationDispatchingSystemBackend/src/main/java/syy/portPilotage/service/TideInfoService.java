package syy.portPilotage.service;

import syy.portPilotage.entity.TideInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;

public interface TideInfoService extends IService<TideInfo> {

    TideInfo getHourlyTideInfo(Date date, int hour, String region);

    TideInfo getTodayTideInfo(Date date, String region);
}
