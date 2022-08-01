package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarPackageAuditMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CarPackageAudit record);

    CarPackageAudit selectByPrimaryKey(Long id);

    List<CarPackageAudit> selectAll();

    int updateByPrimaryKey(CarPackageAudit record);

    List<CarPackageAudit> selectForList(CarPackageAuditQuery qo);

    void changStatus(@Param("id") Long id, @Param("statusCancel") Integer statusCancel);
}