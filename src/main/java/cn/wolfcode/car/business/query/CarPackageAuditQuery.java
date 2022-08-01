package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Data;

@Data
public class CarPackageAuditQuery extends QueryObject {
    private String username;
    private Long auditorId;
    private Integer status;
    private String info;
}
