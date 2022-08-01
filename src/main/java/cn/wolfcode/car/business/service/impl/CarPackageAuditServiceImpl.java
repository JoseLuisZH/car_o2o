package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.mapper.CarPackageAuditMapper;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class CarPackageAuditServiceImpl implements ICarPackageAuditService {

    @Autowired
    private CarPackageAuditMapper carPackageAuditMapper;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private TaskService taskService;

    @Override
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo) {
        String userName = ShiroUtils.getUser().getUserName();
        qo.setUsername(userName);
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<CarPackageAudit>(carPackageAuditMapper.selectForList(qo));
    }

    @Override
    public TablePageInfo<CarPackageAudit> todoQuery(CarPackageAuditQuery qo) {
        qo.setAuditorId(ShiroUtils.getUser().getId());
        qo.setStatus(CarPackageAudit.STATUS_IN_ROGRESS);
        return new TablePageInfo<CarPackageAudit>(carPackageAuditMapper.selectForList(qo));
    }

    @Override
    public InputStream processImg(Long id) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        List<String> activitiHightList;
        // 如果在审核中调用工作流api获取高亮集合
        if (CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())) {
            activitiHightList = runtimeService.getActiveActivityIds(carPackageAudit.getInstanceId());
        } else {
            activitiHightList = Collections.emptyList();
        }

        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(carPackageAudit.getBpmnInfoId());

        DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
        InputStream inputStream = generator.generateDiagram(repositoryService.getBpmnModel(bpmnInfo.getActProcessId())
                , activitiHightList
                , Collections.emptyList(),
                "宋体", "宋体", "宋体");
        return inputStream;
    }

    @Override
    public void cancelApply(Long id) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        // 如果不是在审核中则抛出异常
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())) {
            throw new BusinessException("非法操作");
        }
        // 把套餐审核状态改成审核取消
        carPackageAuditMapper.changStatus(id, CarPackageAudit.STATUS_CANCEL);
        // 服务单项状态改成重新调整
        serviceItemMapper.changeAuditStatus(carPackageAudit.getServiceItemId(), ServiceItem.AUDITSTATUS_REPLY);
        // 删除流程定义
        runtimeService.deleteProcessInstance(carPackageAudit.getInstanceId(), "删除");
    }


    @Override
    public void audit(Long id, Integer auditStatus, String info) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())) {
            throw new BusinessException("非法操作");
        }
        Task task = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();
        HashMap<String, Object> map = new HashMap<>();
        map.put("auditStatus", auditStatus);
        taskService.complete(task.getId(), map);

        if (CarPackageAudit.STATUS_PASS.equals(auditStatus)) {
            Task nextTask = taskService.createTaskQuery().processInstanceId(carPackageAudit.getInstanceId()).singleResult();
            if (nextTask != null) {
                carPackageAudit.setAuditorId(Long.parseLong(nextTask.getAssignee()));
            } else {
                serviceItemMapper.changeAuditStatus(carPackageAudit.getServiceItemId(), ServiceItem.AUDITSTATUS_APPROVED);
                carPackageAudit.setStatus(CarPackageAudit.STATUS_PASS);
            }
            carPackageAudit.setInfo(carPackageAudit.getInfo() + "---> 审核人" + ShiroUtils.getUser().getUserName() + "同意备注-->" + info);

        } else if (CarPackageAudit.STATUS_REJECT.equals(auditStatus)) {
            serviceItemMapper.changeAuditStatus(carPackageAudit.getServiceItemId(), ServiceItem.AUDITSTATUS_REPLY);
            carPackageAudit.setStatus(CarPackageAudit.STATUS_REJECT);
            carPackageAudit.setInfo(carPackageAudit.getInfo() + "---> 审核人" + ShiroUtils.getUser().getUserName() + "拒绝备注-->" + info);
        }
        carPackageAudit.setAuditTime(new Date());
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
    }
}
