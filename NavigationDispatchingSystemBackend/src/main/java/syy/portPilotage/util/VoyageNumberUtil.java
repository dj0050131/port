package syy.portPilotage.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import syy.portPilotage.entity.Vessels;
import syy.portPilotage.entity.Voyages;
import syy.portPilotage.mapper.VoyagesMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class VoyageNumberUtil {
    
    private static VoyagesMapper voyagesMapper;
    
    @Autowired
    public void setVoyagesMapper(VoyagesMapper voyagesMapper) {
        VoyageNumberUtil.voyagesMapper = voyagesMapper;
    }

    
    public static String generateVoyageNumber(Vessels vessel) {
        String baseNumber;
        if (vessel != null && vessel.getImoNumber() != null) {
            LocalDateTime now = LocalDateTime.now();
            String datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
            String timePart = String.valueOf(System.currentTimeMillis()).substring(7);
            baseNumber = vessel.getImoNumber() + datePart + timePart;
        } else if (vessel != null && vessel.getNameEn() != null) {
            // 使用船名首字母 + 年月 + 序号
            String shipCode = getShipInitials(vessel.getNameEn());
            LocalDateTime now = LocalDateTime.now();
            String datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
            String timePart = String.valueOf(System.currentTimeMillis()).substring(7);
            baseNumber = shipCode + datePart + timePart;
        } else {
            LocalDateTime now = LocalDateTime.now();
            String datePart = now.format(DateTimeFormatter.ofPattern("yyMMdd"));
            String timePart = String.valueOf(System.currentTimeMillis()).substring(7);
            baseNumber = datePart + timePart;
        }
        
        // 检查数据库中是否已存在该编号，如果存在则添加后缀
        return generateUniqueVoyageNumber(baseNumber);
    }
    
    /**
     * 生成唯一的航次编号
     * @param baseNumber 基础编号
     * @return 唯一的航次编号
     */
    private static String generateUniqueVoyageNumber(String baseNumber) {
        String voyageNumber = baseNumber + "001";
        int suffix = 1;
        
        // 如果没有注入mapper，则直接返回基础编号（防止循环依赖）
        if (voyagesMapper == null) {
            return voyageNumber;
        }
        
        while (isVoyageNumberExists(voyageNumber)) {
            suffix++;
            voyageNumber = baseNumber + String.format("%03d", suffix);
            
            if (suffix > 999) {
                throw new RuntimeException("无法生成唯一的航次编号");
            }
        }
        
        return voyageNumber;
    }
    
    /**
     * 检查航次编号是否已存在
     * @param voyageNumber 航次编号
     * @return 是否存在
     */
    private static boolean isVoyageNumberExists(String voyageNumber) {
        // 如果没有注入mapper，则认为不存在（防止循环依赖）
        if (voyagesMapper == null) {
            return false;
        }
        
        LambdaQueryWrapper<Voyages> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Voyages::getVoyageNumber, voyageNumber);
        List<Voyages> voyagesList = voyagesMapper.selectList(queryWrapper);
        
        return !voyagesList.isEmpty();
    }

        public static String getShipInitials(String shipName) {
        StringBuilder initials = new StringBuilder();
        String[] words = shipName.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                initials.append(word.charAt(0));
            }
        }
        return initials.toString().toUpperCase();
    }

}