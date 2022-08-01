package cn.wolfcode.car.business.service.impl;

import cn.wolfcode.car.business.domain.BpmnInfo;
import cn.wolfcode.car.business.domain.Statement;
import cn.wolfcode.car.business.mapper.BpmnInfoMapper;
import cn.wolfcode.car.business.mapper.StatementMapper;
import cn.wolfcode.car.business.query.BpmnInfoQuery;
import cn.wolfcode.car.business.service.IBpmnInfoService;
import cn.wolfcode.car.common.base.page.TablePageInfo;
import cn.wolfcode.car.common.config.SystemConfig;
import cn.wolfcode.car.common.exception.BusinessException;
import cn.wolfcode.car.common.util.Convert;
import cn.wolfcode.car.common.util.file.FileUploadUtils;
import com.github.pagehelper.PageHelper;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class BpmnInfoServiceImpl implements IBpmnInfoService {

    @Autowired
    private BpmnInfoMapper bpmnInfoMapper;

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public TablePageInfo<BpmnInfo> query(BpmnInfoQuery qo) {
        PageHelper.startPage(qo.getPageNum(), qo.getPageSize());
        return new TablePageInfo<BpmnInfo>(bpmnInfoMapper.selectForList(qo));
    }

    @Override
    public String upload(MultipartFile file) throws IOException {
        String extension = FileUploadUtils.getExtension(file);
        if (!"bpmn".equals(extension)) {
            throw new BusinessException("请上传bpmn格式的文件");
        }
        String path = FileUploadUtils.upload(SystemConfig.getProfile(), file);
        return path;
    }

    @Override
    public void deploy(String path, String bpmnType, String info) throws IOException {
        File file = new File(SystemConfig.getProfile(), path);
        FileInputStream fileInputStream = new FileInputStream(file);
        Deployment deployment = repositoryService.createDeployment()
                .addInputStream(path, fileInputStream)
                .deploy();
        fileInputStream.close();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();

        BpmnInfo bpmnInfo = new BpmnInfo();
        bpmnInfo.setDeploymentId(deployment.getId());
        bpmnInfo.setInfo(info);
        bpmnInfo.setBpmnType(bpmnType);
        bpmnInfo.setDeployTime(new Date());
        bpmnInfo.setActProcessKey(processDefinition.getKey());
        bpmnInfo.setActProcessId(processDefinition.getId());
        bpmnInfo.setBpmnName(processDefinition.getName());
        bpmnInfoMapper.insert(bpmnInfo);
    }

    @Override
    public void remove(Long id) {
        BpmnInfo bpmnInfo = bpmnInfoMapper.selectByPrimaryKey(id);
        bpmnInfoMapper.deleteByPrimaryKey(id);
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(bpmnInfo.getDeploymentId())
                .singleResult();
        repositoryService.deleteDeployment(processDefinition.getDeploymentId());
        File file = new File(SystemConfig.getProfile(), processDefinition.getResourceName());
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public InputStream readResource(String type, String deploymentId, HttpServletResponse response) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult();
        InputStream inputStream = null;
        if ("xml".equals(type)) {
            inputStream = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());
            return inputStream;
        }
        DefaultProcessDiagramGenerator generator = new DefaultProcessDiagramGenerator();
        inputStream = generator.generateDiagram(repositoryService.getBpmnModel(processDefinition.getId()),
                Collections.emptyList(),
                Collections.emptyList(), "宋体", "宋体", "宋体");
        return inputStream;
    }

    @Override
    public List<BpmnInfo> queryByType(String carPackage) {
        return bpmnInfoMapper.queryByType(carPackage);
    }
}
