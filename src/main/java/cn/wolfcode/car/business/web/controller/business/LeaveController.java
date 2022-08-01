package cn.wolfcode.car.business.web.controller.business;


import cn.wolfcode.car.base.domain.User;
import cn.wolfcode.car.base.service.IUserService;
import cn.wolfcode.car.business.domain.CarPackageAudit;
import cn.wolfcode.car.business.domain.Leave;
import cn.wolfcode.car.business.query.LeaveQuery;
import cn.wolfcode.car.business.service.ILeaveService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import cn.wolfcode.car.shiro.ShiroUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 服务单项控制器
 */
@Controller
@RequestMapping("business/leave")
public class LeaveController {
    //模板前缀
    private static final String prefix = "business/leave/";

    @Autowired
    private ILeaveService leaveService;

    @Autowired
    private IUserService userService;

    //页面------------------------------------------------------------
    //列表
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }


    @RequestMapping("/app")
    private String app(Model model) {
        List<User> shopOwners = userService.queryByRoleKey("shopOwner");
        List<User> financials = userService.queryByRoleKey("financial");
        model.addAttribute("directors",shopOwners);
        model.addAttribute("finances",financials);
        return prefix + "app";
    }

//    @RequestMapping("/auditPage")
//    public String auditPage(Long id, Model model){
//        model.addAttribute("id", id);
//        return prefix + "audit";
//    }

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
    public TablePageInfo<Leave> query(LeaveQuery qo){
        return leaveService.query(qo);
    }

    @RequestMapping("startApp")
    @ResponseBody
    public AjaxResult startApp(Leave leave,Long auditHrId) {
        leave.setStatus(CarPackageAudit.STATUS_IN_ROGRESS);
        leaveService.insert(leave);
        leaveService.startApp(leave,auditHrId);
        return AjaxResult.success();
    }


//    @RequestMapping("/todoQuery")
//    @ResponseBody
//    public TablePageInfo<Leave> todoQuery(LeaveQuery qo) {
//        return leaveService.todoQuery(qo);
//    }
//
//
//    @RequestMapping("/doneQuery")
//    @ResponseBody
//    public TablePageInfo<Leave> doneQuery(LeaveQuery qo){
//        User user = ShiroUtils.getUser();
//        qo.setInfo(user.getUserName());
//        return leaveService.query(qo);
//    }

}
