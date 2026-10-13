package syy.portPilotage.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdGenerator {
    
    private static Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);// 使用雪花算法生成ID
    private static BusinessCodeGenerator businessCodeGenerator;
    
    @Autowired
    public void setBusinessCodeGenerator(BusinessCodeGenerator generator) {
        IdGenerator.businessCodeGenerator = generator;
    }
    
    public static Long nextId() {
        return SNOWFLAKE.nextId();
    }
    
    public static String nextIdStr() {
        return SNOWFLAKE.nextIdStr();
    }
    
    public static String uuid() {
        return IdUtil.simpleUUID();
    }
    
    public static String generateRequestCode() {
        // 更符合真实业务：按日递增序号，且由数据库保证唯一
        return businessCodeGenerator.nextCode("REQUEST", "REQ", 4);
    }
    
    public static String generatePlanCode() {
        return businessCodeGenerator.nextCode("PLAN", "PLAN", 4);
    }
    
    public static String generateCertificateCode() {
        return businessCodeGenerator.nextCode("CERTIFICATE", "PC", 4);
    }
    
    public static String generateVoucherCode() {
        // 与计费凭证实际展示前缀统一：BV
        return businessCodeGenerator.nextCode("VOUCHER", "BV", 4);
    }
    
    public static String generateInvoiceCode() {
        return businessCodeGenerator.nextCode("INVOICE", "INV", 4);
    }
}