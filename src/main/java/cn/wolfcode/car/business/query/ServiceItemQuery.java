package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Data;

@Data
public class ServiceItemQuery extends QueryObject {
    private String name;
    private Integer carPackage;
    private Integer serviceCatalog;
    private Integer auditStatus;
    private Integer saleStatus;
}
