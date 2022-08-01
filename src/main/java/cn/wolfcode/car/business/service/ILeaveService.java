package cn.wolfcode.car.business.service;

import cn.wolfcode.car.business.domain.Leave;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.query.LeaveQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.io.InputStream;

/**
 * 客户服务接口
 */
public interface ILeaveService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<Leave> query(LeaveQuery qo);

    Leave get(Long id);

    void insert(Leave leave);

    void startApp(Leave leave, Long auditHrId);
}
