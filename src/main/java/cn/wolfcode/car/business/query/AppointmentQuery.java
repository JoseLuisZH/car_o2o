package cn.wolfcode.car.business.query;

import cn.wolfcode.car.common.base.query.QueryObject;
import lombok.Data;

@Data
public class AppointmentQuery extends QueryObject {
    private String customerName;
    private String customerPhone;
    private Integer status;
}
