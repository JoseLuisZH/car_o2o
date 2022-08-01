package cn.wolfcode.car.business.domain;

import lombok.Data;

@Data
public class CarPackageAuditVO {
    private Long id;
    private Long bpmnInfoId;
    private Long director;
    private Long finance;
    private String info;
}
