package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.domain.StatementItem;
import cn.wolfcode.car.business.mapper.StatementItemMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.StatementItemQuery;
import cn.wolfcode.car.business.query.StatementQuery;
import cn.wolfcode.car.business.service.IStatementItemService;
import cn.wolfcode.car.business.service.IStatementService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.shiro.ShiroUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class StatementItemServiceImpl implements IStatementItemService {

    @Autowired
    private StatementItemMapper statementItemMapper;

    @Autowired
    private StatementMapper statementMapper;

    @Override
    public TablePageInfo<StatementItem> query(StatementItemQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<StatementItem>(statementItemMapper.selectForList(qo));
    }

    @Override
    public List<StatementItem> list() {
        return statementItemMapper.selectAll();
    }

    @Override
    public void saveItem(List<StatementItem> list) {
        StatementItem item = list.remove(list.size() - 1);
        if (list == null || list.size() == 0) {
            throw new BusinessException("没有消费无法结算");
        }

        BigDecimal discountAmount = item.getItemPrice();
        Long statementId = item.getStatementId();
        statementMapper.deleteRelation(statementId);


        BigDecimal totalAmount  = new BigDecimal(0.00);
        BigDecimal totalCount = new BigDecimal(0.00);
        for (StatementItem statementItem : list) {
            totalAmount = totalAmount.add(statementItem.getItemPrice().multiply(statementItem.getItemQuantity()));
            totalCount = totalCount.add(statementItem.getItemQuantity());
            statementItemMapper.insert(statementItem);
        }
        statementMapper.updateAmount(statementId,totalAmount,totalCount,discountAmount);
    }

    @Override
    public void pay(Long statementId) {
        Statement statement = statementMapper.selectByPrimaryKey(statementId);
        if (!Statement.STATUS_CONSUME.equals(statement.getStatus())) {
            throw new BusinessException("非法操作");
        }
        statementMapper.changPayStatus(statementId,Statement.STATUS_PAID, ShiroUtils.getUser().getId());
    }

}
