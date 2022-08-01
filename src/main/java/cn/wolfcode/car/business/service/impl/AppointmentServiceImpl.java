package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.Appointment;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.AppointmentMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.AppointmentQuery;
import cn.wolfcode.car.business.service.IAppointmentService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AppointmentServiceImpl implements IAppointmentService {

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private StatementMapper statementMapper;


    @Override
    public TablePageInfo<Appointment> query(AppointmentQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<Appointment>(appointmentMapper.selectForList(qo));
    }


    @Override
    public void save(Appointment appointment) {
        appointment.setAppointmentTime(new Date());
        appointment.setCreateTime(new Date());
        appointmentMapper.insert(appointment);
    }

    @Override
    public Appointment get(Long id) {
        return appointmentMapper.selectByPrimaryKey(id);
    }


    @Override
    public void update(Appointment appointment) {
        appointment.setCreateTime(new Date());
        appointmentMapper.updateByPrimaryKey(appointment);
    }

    @Override
    public void deleteBatch(String ids) {
        Long[] dictIds = Convert.toLongArray(ids);
        for (Long dictId : dictIds) {
            Appointment appointment = this.get(dictId);
            if (!Appointment.STATUS_APPOINTMENT.equals(appointment.getStatus())) {
                throw new BusinessException("系统错误");
            }
            appointmentMapper.deleteByPrimaryKey(dictId);
        }
    }

    @Override
    public List<Appointment> list() {
        return appointmentMapper.selectAll();
    }

    @Override
    public void changeStatus(Long id, Integer statusArrival) {
        Appointment appointment = this.get(id);
        if (!Appointment.STATUS_APPOINTMENT.equals(appointment.getStatus())) {
            throw new BusinessException("系统错误");
        }
        appointmentMapper.changeStatus(id,statusArrival);
    }

    @Override
    public void cancelStatus(Long id, Integer statusCancel) {
        Appointment appointment = this.get(id);
        if (!Appointment.STATUS_APPOINTMENT.equals(appointment.getStatus())) {
            throw new BusinessException("系统错误");
        }
        appointmentMapper.statusCancel(id,statusCancel);
    }

    @Override
    public Long generateStatementId(Long id) {
        Appointment appointment = this.get(id);
        Statement statement = statementMapper.queryByAppId(id);
        if (statement == null) {
            statement = new Statement();
            statement.setCustomerName(appointment.getCustomerName());
            statement.setCustomerPhone(appointment.getCustomerPhone());
            statement.setActualArrivalTime(appointment.getActualArrivalTime());
            statement.setLicensePlate(appointment.getLicensePlate());
            statement.setCarSeries(appointment.getCarSeries());
            statement.setCreateTime(appointment.getCreateTime());
            statement.setServiceType(appointment.getServiceType());
            statement.setAppointmentId(id);
            statement.setInfo(appointment.getInfo());
            statementMapper.insert(statement);
        }
        appointmentMapper.changeStatus(id,Appointment.STATUS_SETTLE);
        return statement.getId();
    }
}
