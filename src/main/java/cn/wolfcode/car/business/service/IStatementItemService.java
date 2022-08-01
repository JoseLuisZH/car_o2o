package cn.wolfcode.car.business.service;

import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.query.StatementItemQuery;
import cn.wolfcode.car.business.query.StatementQuery;
import cn.wolfcode.car.common.base.page.TablePageInfo;

import java.util.List;

/**
 * 客户服务接口
 */
public interface IStatementItemService {

    /**
     * 分页
     * @param qo
     * @return
     */
    TablePageInfo<StatementItem> query(StatementItemQuery qo);


    /**
     * 查询全部
     * @return
     */
    List<StatementItem> list();

    void saveItem(List<StatementItem> list);

    void pay(Long statementId);
}
