package cn.wolfcode.car.business.web.controller.business;


import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.web.AjaxResult;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.poi.util.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 服务单项控制器
 */
@Controller
@RequestMapping("business/bpmnInfo")
public class BpmnInfoController {
    //模板前缀
    private static final String prefix = "business/bpmnInfo/";

    @Autowired
    private IBpmnInfoService bpmnInfoService;

    //页面------------------------------------------------------------
    //列表
    @RequestMapping("/listPage")
    public String listPage(){
        return prefix + "list";
    }

    @RequestMapping("/deployPage")
    public String deployPage() {
        return prefix + "deploy";
    }

    //数据-----------------------------------------------------------
    //列表
    @RequiresPermissions("business:bpmnInfo:list")
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo){
        return bpmnInfoService.query(qo);
    }

    @RequestMapping("/upload")
    @ResponseBody
    public AjaxResult upload(MultipartFile file) throws IOException {
        String path = bpmnInfoService.upload(file);
        return AjaxResult.success("操作成功",path);
    }

    @RequestMapping("/deploy")
    @ResponseBody
    public AjaxResult deploy(String path, String bpmnType, String info) throws IOException {
        bpmnInfoService.deploy(path,bpmnType,info);
        return AjaxResult.success();
    }

    @RequestMapping("/remove")
    @ResponseBody
    public AjaxResult remove(Long id) {
        bpmnInfoService.remove(id);
        return AjaxResult.success();
    }


    @RequestMapping("/readResource")
    @ResponseBody
    public void readResource(String type, String deploymentId, HttpServletResponse response) throws IOException {
        InputStream inputStream = bpmnInfoService.readResource(type, deploymentId, response);
        IOUtils.copy(inputStream,response.getOutputStream());
    }
}
