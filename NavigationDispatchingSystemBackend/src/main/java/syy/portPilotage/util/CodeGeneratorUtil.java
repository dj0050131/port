package syy.portPilotage.util;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CodeGeneratorUtil {

    private final BusinessCodeGenerator businessCodeGenerator;

    /**
     * 生成签证单编号
     * 格式: PC + yyyyMMdd + 4位序号
     *
     * @return 签证单编号
     */
    public String generateCertificateCode() {
        return businessCodeGenerator.nextCode("CERTIFICATE", "PC", 4);
    }

    /**
     * 生成计费凭证编号
     * 格式: BV + yyyyMMdd + 4位序号
     *
     * @return 计费凭证编号
     */
    public String generateVoucherCode() {
        return businessCodeGenerator.nextCode("VOUCHER", "BV", 4);
    }

    /**
     * 生成作业编号
     * 格式: OP + yyyyMMdd + 4位序号
     *
     * @return 作业编号
     */
    public String generateOperationId() {
        return businessCodeGenerator.nextCode("OPERATION", "OP", 4);
    }

    /**
     * 生成收费明细编号
     * 格式: CD + yyyyMMdd + 4位序号
     *
     * @return 收费明细编号
     */
    public String generateChargeDetailCode() {
        return businessCodeGenerator.nextCode("CHARGE_DETAIL", "CD", 4);
    }

    
    public String generateWorksheetCode() {
        return businessCodeGenerator.nextCode("WORKSHEET", "WS", 4);
    }
}