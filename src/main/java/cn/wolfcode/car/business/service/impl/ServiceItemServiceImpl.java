package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.CarPackageAuditVO;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.mapper.CarPackageAuditMapper;
import cn.wolfcode.car.business.mapper.ServiceItemMapper;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class ServiceItemServiceImpl implements IServiceItemService {

    @Autowired
    private ServiceItemMapper serviceItemMapper;

    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private CarPackageAuditMapper carPackageAuditMapper;


    @Override
    public TablePageInfo<ServiceItem> query(ServiceItemQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<ServiceItem>(serviceItemMapper.selectForList(qo));
    }


    @Override
    public void save(ServiceItem serviceItem) {
        ServiceItem newServiceItem = new ServiceItem();
        newServiceItem.setName(serviceItem.getName());
        newServiceItem.setOriginalPrice(serviceItem.getOriginalPrice());
        newServiceItem.setDiscountPrice(serviceItem.getDiscountPrice());
        newServiceItem.setCarPackage(serviceItem.getCarPackage());
        newServiceItem.setServiceCatalog(serviceItem.getServiceCatalog());
        newServiceItem.setInfo(serviceItem.getInfo());
        newServiceItem.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()));
        newServiceItem.setAuditStatus(ServiceItem.AUDITSTATUS_INIT);
        serviceItemMapper.insert(newServiceItem);
    }

    @Override
    public ServiceItem get(Long id) {
        return serviceItemMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(ServiceItem serviceItem) {
        ServiceItem newServiceItem = new ServiceItem();
        if (ServiceItem.SALESTATUS_ON.equals(serviceItem.getSaleStatus())) {
            throw new BusinessException("已经上架了");
        }else if (ServiceItem.AUDITSTATUS_AUDITING.equals(serviceItem.getAuditStatus())) {
            throw new BusinessException("害在审核呢");
        }
        newServiceItem.setId(serviceItem.getId());
        newServiceItem.setName(serviceItem.getName());
        newServiceItem.setOriginalPrice(serviceItem.getOriginalPrice());
        newServiceItem.setDiscountPrice(serviceItem.getDiscountPrice());
        newServiceItem.setCarPackage(serviceItem.getCarPackage());
        newServiceItem.setServiceCatalog(serviceItem.getServiceCatalog());
        newServiceItem.setInfo(serviceItem.getInfo());
        newServiceItem.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()));
        if (ServiceItem.AUDITSTATUS_APPROVED.equals(serviceItem.getAuditStatus())) {
            newServiceItem.setAuditStatus(ServiceItem.AUDITSTATUS_INIT);
        }
        serviceItemMapper.updateByPrimaryKey(newServiceItem);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            serviceItemMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<ServiceItem> list() {
        return serviceItemMapper.selectAll();
    }

    @Override
    public void saleOn(Long id, Integer saleStatus) {
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(id);
        if (ServiceItem.CARPACKAGE_YES.equals(serviceItem.getCarPackage()) && !ServiceItem.AUDITSTATUS_APPROVED.equals(serviceItem.getAuditStatus())) {
            throw new BusinessException("系统错误");
        } else if (ServiceItem.AUDITSTATUS_AUDITING.equals(serviceItem.getAuditStatus())) {
            throw new BusinessException("系统错误");
        }
        serviceItemMapper.changeStatus(id,saleStatus);
    }

    @Override
    public void saleOff(Long id, Integer saleStatus) {
        serviceItemMapper.changeStatus(id,saleStatus);
    }

    @Override
    public void startAudit(CarPackageAuditVO vo) {
        // 根据id查出要审核的套餐信息
        ServiceItem serviceItem = serviceItemMapper.selectByPrimaryKey(vo.getId());

        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(vo.getBpmnInfoId());
        // 如果是不是套餐就抛出异常
        if (!ServiceItem.CARPACKAGE_YES.equals(serviceItem.getCarPackage())) {
            throw new BusinessException("非法操作");
        }
        // 如果套餐在审核中或者审核通过抛出异常
        if (ServiceItem.AUDITSTATUS_AUDITING.equals(serviceItem.getAuditStatus()) || ServiceItem.AUDITSTATUS_APPROVED.equals(serviceItem.getAuditStatus())) {
            throw new BusinessException("非法操作");
        }

        // 创建套餐审核对象并把现有的信息设置进去
        CarPackageAudit carPackageAudit = new CarPackageAudit();
        carPackageAudit.setServiceItemId(serviceItem.getId());
        carPackageAudit.setServiceItemInfo(serviceItem.getInfo());
        carPackageAudit.setServiceItemPrice(serviceItem.getDiscountPrice());
        carPackageAudit.setCreator(ShiroUtils.getUser().getUserName());
        carPackageAudit.setAuditorId(vo.getDirector());
        carPackageAudit.setBpmnInfoId(vo.getBpmnInfoId());
        // 把套餐状态改成审核中
        carPackageAudit.setStatus(CarPackageAudit.STATUS_IN_ROGRESS);
        carPackageAudit.setCreateTime(new Date());
        // 先把数据加到数据库，对象就有id
        carPackageAuditMapper.insert(carPackageAudit);
        HashMap<String, Object> map = new HashMap<>();
        // 设置审核人
        map.put("director",vo.getDirector());
        // 如果财务id不为空，则也把财务加进去
        if (vo.getFinance() != null) {
            map.put("finance",vo.getFinance());
        }
        // 流程图定义了折扣价变量，把审核套装里的折扣价设置进去
        map.put("discountPrice",carPackageAudit.getServiceItemPrice().longValue());
        // 通过key开启流程实例，cpa对象的id可以作为businessKey，这样一整个流程下来通过id就能知道改套餐信息
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(bpmnInfo.getActProcessKey(), carPackageAudit.getId().toString(), map);
        // 开启流程实例之后返回流程实例对象，把id设置进去再更新cpa对象
        carPackageAudit.setInstanceId(instance.getId());
        carPackageAuditMapper.updateByPrimaryKey(carPackageAudit);
        // 服务单项的状态改成审核中
        serviceItemMapper.changeAuditStatus(vo.getId(),ServiceItem.AUDITSTATUS_AUDITING);
    }

}
