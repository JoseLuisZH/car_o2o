package cn.wolfcode.car.business.web.controller.business;


import cn.wolfcode.car.base.domain.User;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.query.CarPackageAuditQuery;
import cn.wolfcode.car.business.service.ICarPackageAuditService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import cn.wolfcode.car.shiro.ShiroUtils;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * 服务单项控制器
 */
@Controller
@RequestMapping("business/carPackageAudit")
public class CarPackageAuditController {
    //模板前缀
    private static final String prefix = "business/carPackageAudit/";

    @Autowired
    private ICarPackageAuditService carPackageAuditService;

    //页面------------------------------------------------------------
    //列表
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }

    @RequestMapping("/auditPage")
    public String auditPage(Long id, Model model){
        model.addAttribute("id", id);
        return prefix + "audit";
    }

    @RequestMapping("/todoPage")
    public String todoPage() {
        return prefix + "todo";
    }

    @RequestMapping("/donePage")
    public String donePage(){
        return prefix + "donePage";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> query(CarPackageAuditQuery qo){
        return carPackageAuditService.query(qo);
    }

    @RequestMapping("/todoQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> todoQuery(CarPackageAuditQuery qo) {
        return carPackageAuditService.todoQuery(qo);
    }


    @RequestMapping("/doneQuery")
    @ResponseBody
    public TablePageInfo<CarPackageAudit> doneQuery(CarPackageAuditQuery qo){
        User user = ShiroUtils.getUser();
        qo.setInfo(user.getUserName());
        return carPackageAuditService.query(qo);
    }

    @RequestMapping("/processImg")
    @ResponseBody
    public void processImg(Long id, HttpServletResponse response) throws IOException {
        InputStream inputStream = carPackageAuditService.processImg(id);
        IOUtils.copy(inputStream,response.getOutputStream());
    }

    @RequestMapping("/cancelApply")
    @ResponseBody
    public AjaxResult cancelApply(Long id) {
        carPackageAuditService.cancelApply(id);
        return AjaxResult.success();
    }

    @RequestMapping("/audit")
    @ResponseBody
    public AjaxResult audit(Long id, Integer auditStatus, String info) {
        carPackageAuditService.audit(id,auditStatus,info);
        return AjaxResult.success();
    }

}
