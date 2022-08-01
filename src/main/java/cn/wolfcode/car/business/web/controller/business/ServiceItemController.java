package cn.wolfcode.car.business.web.controller.business;


import cn.wolfcode.car.base.domain.User;
import cn.wolfcode.car.base.service.IUserService;
import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.CarPackageAuditVO;
import cn.wolfcode.car.business.domain.ServiceItem;
import cn.wolfcode.car.business.query.ServiceItemQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 服务单项控制器
 */
@Controller
@RequestMapping("business/serviceItem")
public class ServiceItemController {
    //模板前缀
    private static final String prefix = "business/serviceItem/";

    @Autowired
    private IServiceItemService serviceItemService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IBpmnInfoService bpmnInfoService;


    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:serviceItem:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }

    @RequiresPermissions("business:serviceItem:add")
    @RequestMapping("/addPage")
    public String addPage(){
        return prefix + "add";
    }


    @RequiresPermissions("business:serviceItem:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("serviceItem", serviceItemService.get(id));
        return prefix + "edit";
    }

    @RequestMapping("/auditPage")
    public String auditPage(Long id, Model model) {
        ServiceItem serviceItem = serviceItemService.get(id);
        List<User> shopOwners = userService.queryByRoleKey("shopOwner");
        List<User> financials = userService.queryByRoleKey("financial");
        List<BpmnInfo> info = bpmnInfoService.queryByType("car_package");
        model.addAttribute("info",info.get(0));
        model.addAttribute("directors",shopOwners);
        model.addAttribute("finances",financials);
        model.addAttribute("serviceItem",serviceItem);
        return prefix + "audit";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:serviceItem:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<ServiceItem> query(ServiceItemQuery qo){
        return serviceItemService.query(qo);
    }


    //新增
    @RequiresPermissions("business:serviceItem:add")
    @RequestMapping("/add")
    @ResponseBody
    public AjaxResult addSave(ServiceItem serviceItem){
        serviceItemService.save(serviceItem);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:serviceItem:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(ServiceItem serviceItem){
        serviceItemService.update(serviceItem);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:serviceItem:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids){
        serviceItemService.deleteBatch(ids);
        return AjaxResult.success();
    }

    @RequestMapping("/saleOn")
    @RequiresPermissions("business:serviceItem:saleOn")
    @ResponseBody
    public AjaxResult saleOn(Long id) {
        serviceItemService.saleOn(id,ServiceItem.SALESTATUS_ON);
        return AjaxResult.success();
    }

    @RequestMapping("/saleOff")
    @RequiresPermissions("business:serviceItem:saleOff")
    @ResponseBody
    public AjaxResult saleOff(Long id) {
        serviceItemService.saleOff(id,ServiceItem.SALESTATUS_OFF);
        return AjaxResult.success();
    }

    @RequestMapping("/startAudit")
    @ResponseBody
    public AjaxResult startAudit(CarPackageAuditVO vo) {
        serviceItemService.startAudit(vo);
        return AjaxResult.success();
    }

}
