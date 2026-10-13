package syy.portPilotage.controller.system;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import syy.portPilotage.common.Result;
import syy.portPilotage.exception.BusinessException;
import syy.portPilotage.exception.ErrorCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "文件管理", description = "文件上传接口")
public class FileController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads/}")
    private String urlPrefix;

    @PostMapping("/avatar")
    @Operation(summary = "上传头像")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf('.'));
        }

        String filename = "avatar-" + UUID.randomUUID() + ext;
        Path targetDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SERVER_ERROR, "头像上传失败");
        }

        return Result.success(urlPrefix + filename);
    }

    /**
     * 读取 uploads 目录下的原始文件（用于 <img> 直接访问，浏览器不会携带 token，所以必须放行）
     * 例：/api/files/raw/avatar-xxx.png
     */
    @GetMapping("/raw/{filename:.+}")
    @Operation(summary = "读取上传文件原图(无需鉴权)")
    public ResponseEntity<Resource> raw(@PathVariable("filename") String filename) {
        if (filename == null || filename.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件名不能为空");
        }
        // 防止路径穿越
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\") ) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "非法文件名");
        }

        Path baseDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path target = baseDir.resolve(filename).normalize();
        if (!target.startsWith(baseDir)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "非法文件路径");
        }
        if (!Files.exists(target) || !Files.isRegularFile(target)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "文件不存在");
        }

        try {
            Resource resource = new UrlResource(target.toUri());
            String contentType = Files.probeContentType(target);
            MediaType mt = (contentType != null && !contentType.isBlank())
                    ? MediaType.parseMediaType(contentType)
                    : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(mt)
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                    .body(resource);
        } catch (Exception e) {
            log.error("读取文件失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取文件失败");
        }
    }
}
