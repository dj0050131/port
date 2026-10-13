package syy.portPilotage.service;

import syy.portPilotage.entity.Vessels;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface VesselsService extends IService<Vessels> {
    /**
     * 根据IMO编号查询船舶
     * @param imoNumber IMO编号
     * @return 船舶信息
     */
    Vessels getByImoNumber(String imoNumber);

    /**
     * 根据船舶名称查询船舶
     * @param vesselName 船舶名称
     * @return 船舶列表
     */
    List<Vessels> getByVesselName(String vesselName);

    /**
     * 根据船舶类型查询船舶
     * @param vesselType 船舶类型
     * @return 船舶列表
     */
    List<Vessels> getByVesselType(String vesselType);

    /**
     * 根据国籍查询船舶
     * @param nationality 国籍
     * @return 船舶列表
     */
    List<Vessels> getByNationality(String nationality);

    /**
     * 创建船舶信息
     * @param vessel 船舶信息
     * @return 是否创建成功
     */
    boolean createVessel(Vessels vessel);

    /**
     * 更新船舶信息
     * @param vessel 船舶信息
     * @return 是否更新成功
     */
    boolean updateVessel(Vessels vessel);

    /**
     * 删除船舶信息
     * @param id 船舶ID
     * @return 是否删除成功
     */
    boolean deleteVessel(Long id);

    /**
     * 检查船舶是否在黑名单中
     * @param vesselId 船舶ID
     * @return 是否在黑名单中
     */
    boolean isBlacklisted(Long vesselId);
}
