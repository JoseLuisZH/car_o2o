package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.Leave;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.mapper.LeaveMapper;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.query.LeaveQuery;
import cn.wolfcode.car.business.service.ILeaveService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class LeaveServiceImpl implements ILeaveService {

    @Autowired
    private LeaveMapper leaveMapper;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;

    @Override
    public TablePageInfo<Leave> query(LeaveQuery qo) {
        String userName = ShiroUtils.getUser().getUserName();
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Leave>(leaveMapper.selectForList(qo));
    }


    @Override
    public Leave get(Long id) {
        return leaveMapper.selectById(id);
    }

    @Override
    public void insert(Leave leave) {
        leaveMapper.insert(leave);
    }

    @Override
    public void startApp(Leave leave,Long auditHrId) {
        BpmnInfo bpmnInfo = bpmnInfoMapper.queryByType("car_package").get(0);
        Map<String, Object> map = new HashMap<>();
        map.put("mgr",leave.getAuditId());
        map.put("hr",auditHrId);
        runtimeService.startProcessInstanceByKey(bpmnInfo.getActProcessKey(), leave.getId().toString(), map);
    }

}
