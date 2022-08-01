package cn.wolfcode.car.business.mapper;

import cn.wolfcode.car.business.domain.Leave;
import cn.wolfcode.car.business.domain.Leave;
import cn.wolfcode.car.business.query.LeaveQuery;

import java.util.List;

public interface LeaveMapper {
    int insert(Leave record);

    List<Leave> selectAll();

    List<Leave> selectForList(LeaveQuery qo);

    Leave selectById(Long id);
}