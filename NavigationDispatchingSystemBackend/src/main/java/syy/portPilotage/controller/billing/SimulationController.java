package syy.portPilotage.controller.billing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.dto.billing.PaymentCallbackRequest;
import syy.portPilotage.dto.billing.PaymentNoticeRequest;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/billing/simulation")
@Tag(name = "模拟外部系统", description = "模拟外部系统接口")
public class SimulationController {

    private static final ScheduledExecutorService PAYMENT_EXECUTOR = Executors.newScheduledThreadPool(2);
    private static final Random RANDOM = new Random();

    /**
     * 我方系统回调 baseUrl（可配置）。
     * - 单体默认跟随后端自身端口（server.port）
     * - 若模拟外部系统独立部署，需要配置为我方系统地址，例如 http://localhost:8099
     */
    @Value("${external.simulation.callback-base-url:}")
    private String callbackBaseUrl;

    @Value("${server.port:8099}")
    private int serverPort;

    //模拟移交交接单的时候提交到外部系统，外部系统返回成功，并提供相应需要的信息
    @PostMapping("/submitWriteOff")
    @Operation(summary = "模拟提交交接单到外部系统")
    public Result<Map<String, Object>> simulateExternalSystemSubmission(@RequestBody Map<String, Object> requestData) {
        log.info("模拟提交交接单到外部系统: requestData={}", requestData);
        
        List<Long> ids = (List<Long>) requestData.get("ids");
        Map<String, String> data = (Map<String, String>) requestData.get("data");
        
        String transferTarget = data.get("transferTarget");
        String transferType = data.get("transferType");
        
        log.info("接收到的移交信息: ids={}, transferTarget={}, transferType={}", ids, transferTarget, transferType);

        scheduleTransferCallback("writeoff", ids, transferTarget, transferType);
        String responseMessage = "External system accepted writeoff for ids: " + ids;
        
        Map<String, Object> responseData = Map.of(
            "message", responseMessage,
            "processedIds", ids,
            "transferTarget", transferTarget,
            "transferType", transferType,
            "timestamp", System.currentTimeMillis()
        );
        
        log.info("外部系统处理完成: response={}", responseData);
        return Result.success(responseData);
    }
    
    @PostMapping("/submitTaxRecords")
    @Operation(summary = "模拟提交税额记录到外部系统")
    public Result<Map<String, Object>> simulateTaxRecordsSubmission(@RequestBody Map<String, Object> requestData) {
        log.info("模拟提交税额记录到外部系统: requestData={}", requestData);
        
        List<Long> ids = (List<Long>) requestData.get("ids");
        Map<String, String> data = (Map<String, String>) requestData.get("data");
        
        String transferTarget = data.get("transferTarget");
        String transferType = data.get("transferType");
        
        log.info("接收到的税额记录信息: ids={}, transferTarget={}, transferType={}", ids, transferTarget, transferType);

        scheduleTransferCallback("tax", ids, transferTarget, transferType);
        String responseMessage = "External system accepted tax records for ids: " + ids;
        
        Map<String, Object> responseData = Map.of(
            "message", responseMessage,
            "processedIds", ids,
            "transferTarget", transferTarget,
            "transferType", transferType,
            "timestamp", System.currentTimeMillis()
        );
        
        log.info("外部系统处理完成: response={}", responseData);
        return Result.success(responseData);
    }

    private void scheduleTransferCallback(String kind, List<Long> ids, String transferTarget, String transferType) {
        if (ids == null || ids.isEmpty()) return;

        int delayMs = 800 + RANDOM.nextInt(4200); // 0.8s ~ 5s
        boolean ok = RANDOM.nextInt(100) >= 15;   // 85% 成功，15% 失败
        String refNo = (kind.equals("tax") ? "SIM-TAX-" : "SIM-WO-") + System.currentTimeMillis();
        String msg = ok ? "processed" : "external system failed (simulated)";

        PAYMENT_EXECUTOR.schedule(() -> doTransferCallback(kind, ids, transferTarget, transferType, ok, refNo, msg),
                delayMs, TimeUnit.MILLISECONDS);
    }

    private void doTransferCallback(String kind,
                                    List<Long> ids,
                                    String transferTarget,
                                    String transferType,
                                    boolean success,
                                    String externalRefNo,
                                    String message) {
        try {
            String baseUrl = (callbackBaseUrl == null || callbackBaseUrl.trim().isEmpty())
                    ? ("http://localhost:" + serverPort)
                    : callbackBaseUrl.trim();

            String callbackPath = kind.equals("tax")
                    ? "/api/billing/charges/transferCallback/taxRecords"
                    : "/api/billing/charges/transferCallback/writeoff";
            String callbackUrl = baseUrl + callbackPath;

            Map<String, Object> body = Map.of(
                    "ids", ids,
                    "success", success,
                    "transferTarget", transferTarget,
                    "transferType", transferType,
                    "externalRefNo", externalRefNo,
                    "message", message
            );

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Result> resp = restTemplate.postForEntity(callbackUrl, entity, Result.class);

            log.info("模拟外部系统移交回调完成: kind={}, ids={}, success={}, httpStatus={}, bodySuccess={}",
                    kind, ids, success, resp.getStatusCode(), resp.getBody() != null && resp.getBody().isSuccess());
        } catch (Exception e) {
            log.error("模拟外部系统移交回调失败: kind={}, ids={}", kind, ids, e);
        }
    }

        @PostMapping("/notifyPayment")
    @Operation(summary = "模拟外部系统接收付款提示并随机回调付款结果")
    public Result<Map<String, Object>> notifyPayment(@RequestBody PaymentNoticeRequest request) {
        log.info("模拟外部系统接收付款提示: request={}", request);

        List<Long> ids = request.getIds();
        if (ids == null || ids.isEmpty()) {
            return Result.success(Map.of("message", "no ids"));
        }

        for (Long voucherId : ids) {
            int delayMs = 500 + RANDOM.nextInt(9500); // 0.5s ~ 10s
            PAYMENT_EXECUTOR.schedule(() -> doCallbackPaid(voucherId), delayMs, TimeUnit.MILLISECONDS);
        }

        Map<String, Object> responseData = Map.of(
                "message", "payment notice accepted",
                "ids", ids,
                "willCallbackWithinSeconds", 10
        );
        return Result.success(responseData);
    }

    private void doCallbackPaid(Long voucherId) {
        try {
            PaymentCallbackRequest callback = new PaymentCallbackRequest();
            callback.setVoucherId(voucherId);
            callback.setPaidAt(new java.util.Date());
            callback.setExternalPaymentNo("SIM-" + voucherId + "-" + System.currentTimeMillis());

            String baseUrl = (callbackBaseUrl == null || callbackBaseUrl.trim().isEmpty())
                    ? ("http://localhost:" + serverPort)
                    : callbackBaseUrl.trim();
            String callbackUrl = baseUrl + "/api/billing/charges/paymentCallback";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PaymentCallbackRequest> entity = new HttpEntity<>(callback, headers);

            ResponseEntity<Result> resp = restTemplate.postForEntity(callbackUrl, entity, Result.class);
            log.info("模拟外部系统付款回调完成: voucherId={}, status={}, bodySuccess={}",
                    voucherId,
                    resp.getStatusCode(),
                    resp.getBody() != null && resp.getBody().isSuccess());
        } catch (Exception e) {
            log.error("模拟外部系统付款回调失败: voucherId={}", voucherId, e);
        }
    }
}