package syy.portPilotage.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import syy.portPilotage.dto.certificate.CertificateDetailDTO;
import syy.portPilotage.entity.PilotageCertificates;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Map;

@Mapper
public interface PilotageCertificatesMapper extends BaseMapper<PilotageCertificates> {

        IPage<CertificateDetailDTO> selectCertificatePage(Page<CertificateDetailDTO> page,
                                                      @Param("params") Map<String, Object> params);
}
