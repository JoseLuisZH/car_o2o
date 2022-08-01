package cn.wolfcode.car.business.web.controller.business;


import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.query.AppointmentQuery;
import cn.wolfcode.car.business.service.IAppointmentService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 服务单项控制器
 */
@Controller
@RequestMapping("business/appointment")
public class AppointmentController {
    //模板前缀
    private static final String prefix = "business/appointment/";

    @Autowired
    private IAppointmentService appointmentService;

    //页面------------------------------------------------------------
    //列表
    @RequiresPermissions("business:appointment:view")
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }

    @RequiresPermissions("business:appointment:add")
    @RequestMapping("/addPage")
    public String addPage(){
        return prefix + "add";
    }


    @RequiresPermissions("business:appointment:edit")
    @RequestMapping("/editPage")
    public String editPage(Long id, Model model){
        model.addAttribute("appointment", appointmentService.get(id));
        return prefix + "edit";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:appointment:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<Appointment> query(AppointmentQuery qo){
        return appointmentService.query(qo);
    }


    //新增
    @RequiresPermissions("business:appointment:add")
    @RequestMapping("/add")
    @ResponseBody
    public AjaxResult addSave(Appointment appointment){
        appointmentService.save(appointment);
        return AjaxResult.success();
    }

    //编辑
    @RequiresPermissions("business:appointment:edit")
    @RequestMapping("/edit")
    @ResponseBody
    public AjaxResult edit(Appointment appointment){
        appointmentService.update(appointment);
        return AjaxResult.success();
    }

    //删除
    @RequiresPermissions("business:appointment:remove")
    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids){
        appointmentService.deleteBatch(ids);
        return AjaxResult.success();
    }

    @RequestMapping("/arrivalShop")
    @ResponseBody
    public AjaxResult arrivalShop(Long id) {
        appointmentService.changeStatus(id,Appointment.STATUS_ARRIVAL);
        return AjaxResult.success();
    }

    @RequestMapping("/cancelApp")
    @ResponseBody
    public AjaxResult cancelApp(Long id) {
        appointmentService.cancelStatus(id,Appointment.STATUS_CANCEL);
        return AjaxResult.success();
    }

    @RequestMapping("/generateStatementId")
    @ResponseBody
    public AjaxResult generateStatementId(Long id) {
        Long statementId = appointmentService.generateStatementId(id);
        return AjaxResult.success(statementId);
    }

}
