package syy.portPilotage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import syy.portPilotage.entity.TideInfo;
import syy.portPilotage.service.TideInfoService;
import syy.portPilotage.mapper.TideInfoMapper;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class TideInfoServiceImpl extends ServiceImpl<TideInfoMapper, TideInfo>
    implements TideInfoService{

    @Autowired
    private TideInfoMapper tideInfoMapper;
    public TideInfo getTodayTideInfo(Date date, String region) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date dayStart = cal.getTime();

        Calendar nextCal = Calendar.getInstance();
        nextCal.setTime(dayStart);
        nextCal.add(Calendar.DAY_OF_MONTH, 1);
        Date dayEnd = nextCal.getTime();

        return this.getOne(
                new LambdaQueryWrapper<TideInfo>()
                        .eq(TideInfo::getRegion, region)
                        .ge(TideInfo::getTideDate, dayStart)  // 大于等于当天开始时间
                        .lt(TideInfo::getTideDate, dayEnd)    // 小于第二天开始时间
        );
    }

    @Override
    public TideInfo getHourlyTideInfo(Date date, int hour, String region) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date hourStart = cal.getTime();

        Calendar nextCal = Calendar.getInstance();
        nextCal.setTime(hourStart);
        nextCal.add(Calendar.HOUR_OF_DAY, 1);
        Date hourEnd = nextCal.getTime();

        return this.getOne(
                new LambdaQueryWrapper<TideInfo>()
                        .eq(TideInfo::getRegion, region)
                        .ge(TideInfo::getTideDate, hourStart)  // 大于等于小时开始时间
                        .lt(TideInfo::getTideDate, hourEnd)    // 小于下个小时开始时间
        );
    }

}
