package syy.portPilotage.util;

import syy.portPilotage.vo.screen.GeoCoordinateVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeoCoordinateUtil {

    /**
     * 生成温州沿海周边的随机经纬度列表（GCJ-02坐标系）
     * @param count 生成的坐标数量
     * @return 包含指定数量随机坐标的List
     */
    public static List<GeoCoordinateVO> generateWenzhouCoastRandomCoordinates(int count) {
        List<GeoCoordinateVO> coordinateList = new ArrayList<>();
        Random random = new Random();

        // 温州沿海核心经纬度范围（确保随机坐标在合理区域内）
        // 经度范围：120.5 ~ 121.3（温州沿海经度区间）
        double minLng = 120.5;
        double maxLng = 121.3;
        // 纬度范围：27.2 ~ 27.9（温州沿海纬度区间）
        double minLat = 27.2;
        double maxLat = 27.9;

        for (int i = 0; i < count; i++) {
            double randomLng = minLng + (maxLng - minLng) * random.nextDouble();
            double randomLat = minLat + (maxLat - minLat) * random.nextDouble();

            BigDecimal lng = new BigDecimal(String.format("%.6f", randomLng));
            BigDecimal lat = new BigDecimal(String.format("%.6f", randomLat));

            GeoCoordinateVO coordinate = new GeoCoordinateVO();
            coordinate.setLng(lng);
            coordinate.setLat(lat);

            coordinateList.add(coordinate);
        }

        return coordinateList;
    }
}