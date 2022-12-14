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
        // ?????????????????????????????????api??????????????????
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
                "??????", "??????", "??????");
        return inputStream;
    }

    @Override
    public void cancelApply(Long id) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        // ???????????????????????????????????????
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())) {
            throw new BusinessException("????????????");
        }
        // ???????????????????????????????????????
        carPackageAuditMapper.changStatus(id, CarPackageAudit.STATUS_CANCEL);
        // ????????????????????????????????????
        serviceItemMapper.changeAuditStatus(carPackageAudit.getServiceItemId(), ServiceItem.AUDITSTATUS_REPLY);
        // ??????????????????
        runtimeService.deleteProcessInstance(carPackageAudit.getInstanceId(), "??????");
    }


    @Override
    public void audit(Long id, Integer auditStatus, String info) {
        CarPackageAudit carPackageAudit = carPackageAuditMapper.selectByPrimaryKey(id);
        if (!CarPackageAudit.STATUS_IN_ROGRESS.equals(carPackageAudit.getStatus())) {
            throw new BusinessException("????????????");
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
            carPackageAudit.setInfo(carPackageAudit.getInfo() + "---> ?????????" + ShiroUtils.getUser().getUserName() + "????????????-->" + info);

        } else if (CarPackageAudit.STATUS_REJECT.equals(auditStatus)) {
            serviceItemMapper.changeAuditStatus(carPackageAudit.getServiceItemId(), ServiceItem.AUDITSTATUS_REPLY);
            carPackageAudit.setStatus(CarPackageAudit.STATUS_REJECT);
            carPackageAudit.setInfo(carPackageAudit.getInfo() + "---> ?????????" + ShiroUtils.getUser().getUserName() + "????????????-->" + info);
        }
        carPackageAudit.setAuditTime(new Date());
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
    }
}
