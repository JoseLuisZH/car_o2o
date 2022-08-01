package cn.wolfcode.car.business.service;

import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.query.AppointmentQuery;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.io.InputStream;
import java.util.List;

/**
 * 客户服务接口
 */
public interface ICarPackageAuditService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo);

    InputStream processImg(Long id);

    void cancelApply(Long id);

    TablePageInfo<CarPackageAudit> todoQuery(CarPackageAuditQuery qo);

    void audit(Long id, Integer auditStatus, String info);
}
