package cn.wolfcode.car.business.web.controller.business;


import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.query.StatementItemQuery;
import cn.wolfcode.car.business.query.StatementQuery;
import cn.wolfcode.car.business.service.IServiceItemService;
import cn.wolfcode.car.business.service.IStatementItemService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.web.AjaxResult;
import cn.wolfcode.car.shiro.ShiroUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

/**
 * 服务单项控制器
 */
@Controller
@RequestMapping("business/statementItem")
public class StatementItemController {

    @Autowired
    private IStatementItemService statementItemService;

    @Autowired
    private IStatementService statementService;

    //模板前缀
    private static final String prefix = "business/statementItem/";

    @RequestMapping("/itemDetail")
    public String itemDetail(Long statementId, Model model) {
        Statement statement = statementService.get(statementId);
        String userName = ShiroUtils.getUser().getUserName();
        model.addAttribute("userName",userName);
        model.addAttribute("statement", statement);
        if (!Statement.STATUS_CONSUME.equals(statement.getStatus())) {
            return prefix + "consume";
        }
        return prefix + "detail";
    }

    //列表
    @RequestMapping("/query")
    @ResponseBody
    public TablePageInfo<StatementItem> query(StatementItemQuery qo) {
        return statementItemService.query(qo);
    }



    @RequestMapping("/saveItem")
    @ResponseBody
    public AjaxResult saveItem(@RequestBody List<StatementItem> list) {
        statementItemService.saveItem(list);
        return AjaxResult.success("操作成功");
    }

    @RequestMapping("/pay")
    @ResponseBody
    public AjaxResult pay(@PathVariable Long statementId) {
        statementItemService.pay(statementId);
        return AjaxResult.success("操作成功");
    }
}
