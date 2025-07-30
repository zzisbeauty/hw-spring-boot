package com.hanwei.api.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanwei.api.entity.ApiParamConfig;
import com.hanwei.api.mapper.ApiParamConfigMapper;
import com.hanwei.api.service.IApiParamConfigService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @Description: 接口参数信息
 * @Author: hanwei
 * @Date:   2025-05-14
 * @Version: V1.0
 */


// 1. 先继承了： extends ServiceImpl<ApiParamConfigMapper, ApiParamConfig>， 即继承了 mapper 的相关操作
//    这其实是 MyBatis-Plus 提供的一个通用 Service 实现类，它(ApiParamConfigMapper)已经在父类中为你注入了 ApiParamConfigMapper 的实例
//    但是 ApiParamConfigMapper 的依赖注入的实例化过程不是在 mapper 中实现的，是在当下的 service 的 impl 代码中实现的，如下：
@Service
@Slf4j
public class ApiParamConfigServiceImpl
        extends ServiceImpl<ApiParamConfigMapper, ApiParamConfig> implements IApiParamConfigService {

    /**
     * 根据接口ID获取参数信息
     * @param id
     * @return
     */
    @Override
    public List<ApiParamConfig> getParamByApiId(String id) {return list(
            new LambdaQueryWrapper<ApiParamConfig>().eq(ApiParamConfig::getApiId, id).orderByAsc(ApiParamConfig::getParamOrder)
        );
    }

    /**
     * 保存接口参数信息
     * @param apiParamConfig
     */
    @Override
    public boolean saveApiParamConfig(@Valid ApiParamConfig apiParamConfig) {
        try {
            return save(apiParamConfig);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 根据接口ID删除参数信息
     * @param apiServiceInfoId
     */
    @Override
    public void removeByApiId(String apiServiceInfoId) {
        remove(new LambdaQueryWrapper<ApiParamConfig>().eq(ApiParamConfig::getApiId, apiServiceInfoId));
    }

    /**
     * 更新接口参数信息 先删后加
     * @param apiId
     * @param apiParamConfigList
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 标注了 @Transactional，保证事务一致性：出错就回滚
    public void updateApiParamConfig(String apiId, List<ApiParamConfig> apiParamConfigList) {
        try {
            removeByApiId(apiId);
            for (ApiParamConfig apiParamConfig : apiParamConfigList) {
                apiParamConfig.setApiId(apiId);
                save(apiParamConfig);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("更新接口参数信息失败{}",e.getMessage());
        }
    }
}
