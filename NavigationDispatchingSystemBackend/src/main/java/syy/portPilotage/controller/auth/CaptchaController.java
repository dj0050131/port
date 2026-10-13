package syy.portPilotage.controller.auth;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import syy.portPilotage.common.Result;
import syy.portPilotage.util.IdGenerator;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/captcha")
@RequiredArgsConstructor
@Tag(name = "验证码管理", description = "验证码相关接口")
public class CaptchaController {
    
    @GetMapping("/generate")
    @Operation(summary = "生成验证码")
    public Result<Map<String, String>> generate() {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 50);
        String code = captcha.getCode();
        String captchaKey = IdGenerator.uuid();
        
        Map<String, String> result = new HashMap<>();
        result.put("captchaKey", captchaKey);
        result.put("captchaImage", captcha.getImageBase64());
        
        log.info("生成验证码: key={}, code={}", captchaKey, code);
        
        return Result.success(result);
    }
    
    @GetMapping("/image")
    @Operation(summary = "获取验证码图片")
    public void image(HttpServletResponse response) throws IOException {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 50);
        
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache");
        
        captcha.write(response.getOutputStream());
    }
}
