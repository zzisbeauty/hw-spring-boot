package com.hanwei.rag.service.impl;

import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanwei.application.entity.ApplicationConfig;
import com.hanwei.application.entity.ApplicationInfo;
import com.hanwei.application.mapper.ApplicationInfoMapper;
import com.hanwei.application.service.IApplicationConfigService;
import com.hanwei.application.service.IApplicationInfoService;
import com.hanwei.core.base.QueryGenerator;
import com.hanwei.core.common.api.CommonAPI;
import com.hanwei.core.common.api.vo.Result;
import com.hanwei.rag.bo.ChoiceRagBO;
import com.hanwei.rag.entity.RagInfo;
import com.hanwei.rag.mapper.RagInfoMapper;
import com.hanwei.rag.service.IRagInfoService;
import com.hanwei.rag.vo.RagRecallVO;
import com.hanwei.util.yanjiuyuan.YanjiuyuanHelper;
import com.hanwei.util.shuiwu.ShuiWuHelper;
import com.hanwei.util.yanjiuyuan.YjyResult;
import jakarta.servlet.ServletOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.lang.reflect.Field;


/**
 * @Description: 知识库基础信息管理
 * @Author: hanwei
 * @Date:   2025-05-26
 * @Version: V1.0
 */
@Service
@Slf4j
public class RagInfoServiceImpl extends ServiceImpl<RagInfoMapper, RagInfo> implements IRagInfoService {
    @Value("${excel.batchSaveCount}")
    private Integer BATCH_SAVE_COUNT;
    @Autowired
    private CommonAPI commonApi;
    @Autowired
    private YanjiuyuanHelper yanjiuyuanHelper;
    @Autowired
    private ShuiWuHelper shuiwuHelper;
    @Autowired
    private IApplicationConfigService applicationConfigService;
    @Autowired
    private ApplicationInfoMapper applicationInfoMapper;

    /**
     * 以下方法需要支持直接访问文件流（网关允许）
     * @param outputStream
     * @param paramMap
     * @param ragInfo
     */
    @Override
    public void exportData(ServletOutputStream outputStream, Map paramMap, RagInfo ragInfo) {
        QueryWrapper<RagInfo> queryWrapper = QueryGenerator.initQueryWrapper(ragInfo, paramMap);
        List<RagInfo> list = list(queryWrapper);
        EasyExcel.write(outputStream, RagInfo.class).sheet("知识库基础信息管理").doWrite(list);
    }

    /**
     * 如果网关不知道直接获取文件流，转为base64后返回前端
     * @param paramMap
     * @param ragInfo
     */
    @Override
    public String exportDataToBase64(Map paramMap, RagInfo ragInfo) {
        QueryWrapper<RagInfo> queryWrapper = QueryGenerator.initQueryWrapper(ragInfo, paramMap);
        List<RagInfo> list = list(queryWrapper);
        //字典值转换
        //List<JSON> listJson = commonApi.translateResultByDict(list);
        //List<RagInfo> result = listJson.stream().map(e -> JSON.toJavaObject(e,RagInfo.class)).collect(Collectors.toList());
        String excelContent = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        EasyExcel.write(outputStream, RagInfo.class).sheet("知识库基础信息管理").doWrite(list);
        excelContent = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        return excelContent;
    }

    // 新增知识库数据
    @Override
    public Result<?> saveRagInfo(RagInfo ragInfo) {
        YjyResult yjyResult = null;
        try {
            yjyResult = shuiwuHelper.addRagInfo(ragInfo.getName()); //  调用业务功能
            if(null == yjyResult || 0!=yjyResult.getCode()){
                log.error("调用研究院新增知识库失败 "+ yjyResult.getMessage());
                return Result.error(200,"调用研究院新增知识库失败 "+ yjyResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用研究院新增知识库失败 "+ e.getMessage());
            return Result.error(200,"调用研究院新增知识库失败 "+ e.getMessage());
        }

        // 研究院API结果解析
        JSONObject jsonObject = (JSONObject) yjyResult.getData();
        log.info(jsonObject.toString());
        String yjyRagId = jsonObject.getStr("kb_id");
        ragInfo.setId(yjyRagId);
        // 其他一些参数设置默认值
        ragInfo.setChunkNum(0);
        ragInfo.setId(yjyRagId);
        ragInfo.setEmbdId("BAAI/bge-large-zh-v1.5@BAAI");
        ragInfo.setDocParser("DeepDOC");
        ragInfo.setSlicingMethod("General");
        ragInfo.setTokenNum(0);
        ragInfo.setPagerank(512);
        ragInfo.setIsUseGraph(false);
        ragInfo.setSimilarityThreshold(0.2);
        ragInfo.setVectorSimilarityWeight(0.3);
        ragInfo.setTopN(8);

        /**
        // 水务API结果解析
        JSONObject rootObject = (JSONObject) yjyResult.getData();
        String yjyRagId = rootObject.getStr("id");
        // log.info("解析出的 yjyRagId: {}", yjyRagId);

        // todo 接口返回有字段，最好补全一个参数解析方法，以便补全数据库表中存储的信息
        // 使用反射打印所有字段 - 对 local shuiwu api 返回的结果做一些验证
        log.info("=== RagInfo 字段调试信息（反射方式）===");
        try {
            Field[] fields = ragInfo.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(ragInfo);
                if (value instanceof String) {
                    String strValue = (String) value;
                    log.info("{}: [{}] (长度: {})", field.getName(), strValue, strValue != null ? strValue.length() : 0);
                } else {
                    log.info("{}: [{}]", field.getName(), value);
                }
            }
        } catch (Exception e) {
            log.error("反射获取字段值失败", e);
        }
        log.info("=== 字段调试信息结束 ===");
        */

        save(ragInfo); // 增一条数据， 增过程 sb web mapper 自动根据 entity 和表结构实现 sql 完成新增
        return Result.ok("保存成功");
    }

    // 更新知识库数据
    @Override
    public Result<?> updateRagInfo(RagInfo ragInfo) {
        //调用研究院更新方法
        try {
            YjyResult yjyResult = yanjiuyuanHelper.editRagInfo(ragInfo);
            if(null == yjyResult || 0!=yjyResult.getCode()){
                log.error("调用研究院更新知识库失败 "+ yjyResult.getMessage());
                return Result.error(200,"调用研究院更新知识库失败 "+ yjyResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用研究院更新知识库失败 "+ e.getMessage());
            return Result.error(200,"调用研究院更新知识库失败 "+ e.getMessage());
        }
        updateById(ragInfo);
        return Result.ok("更新成功");
    }

    // 删除知识库数据
    @Override
    public Result<?> removeRagInfoById(String id) {
        //应用是否使用校验
        List<ApplicationConfig> applicationConfigList =
                applicationConfigService.list(new LambdaQueryWrapper<ApplicationConfig>().like(ApplicationConfig::getRagIds,id));
        if(Optional.ofNullable(applicationConfigList).isPresent() && applicationConfigList.size() > 0){
            StringBuilder sb = new StringBuilder();
            sb.append("当前配置正在被应用使用,不允许删除，请先更改应用。使用得应用有:");
            for(ApplicationConfig applicationConfig : applicationConfigList){
                ApplicationInfo applicationInfo = applicationInfoMapper.selectById(applicationConfig.getApplicationId());
                sb.append(applicationInfo.getApplicationName()).append(" ");
            }
            return Result.error(200,sb.toString());
        }
        try {
            YjyResult yjyResult = yanjiuyuanHelper.deleteRagInfo(id);
            if(null == yjyResult || 0!=yjyResult.getCode()){
                log.error("调用研究院删除知识库失败 "+ yjyResult.getMessage());
                return Result.error(200,"调用研究院删除知识库失败 "+ yjyResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用研究院删除知识库失败 "+ e.getMessage());
            return Result.error(200,"调用研究院删除知识库失败 "+ e.getMessage());
        }
        //删除知识库
        removeById(id);
        return Result.ok("删除成功");
    }


    /**
     * 知识库召回测试
     * @param ragRecallVO
     * @return
     */
    @Override
    public Result<?> ragRecall(RagRecallVO ragRecallVO) {
        //调用研究院召回接口
        YjyResult yjyResult = null;
        try {
            yjyResult = yanjiuyuanHelper.ragRecall(ragRecallVO);
            if(null == yjyResult || 0!=yjyResult.getCode()){
                log.error("调用研究院知识库召回测试失败 "+ yjyResult.getMessage());
                return Result.error(200,"调用研究院知识库召回测试失败 "+ yjyResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用研究院知识库召回测试失败 "+ e.getMessage());
            return Result.error("调用研究院知识库召回测试失败 "+ e.getMessage());
        }
        //组装返回前端VO
        return Result.ok(yjyResult.getData());
    }

    // 获取知识库知识图谱
    @Override
    public Result<?> getRagGraph(String id) {
        //调用研究院获取图谱数据
        YjyResult yjyResult = null;
        try {
            yjyResult = yanjiuyuanHelper.getRagGraphInfo(id);
            if(null == yjyResult || 0!=yjyResult.getCode()){
                log.error("调用研究院获取图谱数据失败 "+ yjyResult.getMessage());
                return Result.error("调用研究院获取图谱数据失败 "+ yjyResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用研究院获取图谱数据失败 "+ e.getMessage());
            return Result.error("调用研究院获取图谱数据失败 "+ e.getMessage());
        }
        return Result.ok(yjyResult.getData()); //组装数据给前端
    }

    /**
     * 设置知识库
     * @param choiceRagBO
     * @return
     */
    @Override
    public Result<?> choiceRagByYanjiuyuan(ChoiceRagBO choiceRagBO) {
        //调用设置知识库
        YjyResult yjyResult = null;
        try {
            yjyResult = yanjiuyuanHelper.choiceRag(choiceRagBO);
            if(null == yjyResult || 0!=yjyResult.getCode()){
                log.error("调用研究院设置知识库失败 "+ yjyResult.getMessage());
                return Result.error("调用研究院设置知识库失败 "+ yjyResult.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用研究院设置知识库失败 "+ e.getMessage());
            return Result.error("调用研究院设置知识库失败 "+ e.getMessage());
        }
        //组装数据给前端
        return Result.ok(yjyResult.getData());
    }

}
