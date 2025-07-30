package com.hanwei.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hanwei.api.entity.ApiParamConfig;

import java.util.List;


/**
 * @Description: 接口参数信息
 * @Author: hanwei
 * @Date:   2025-05-14
 * @Version: V1.0
 */

// IService 是来自 MyBatis-Plus 的一个通用 Service 接口，提供了常见的 CRUD 方法（比如 save, removeById, list, getById 等）
public interface IApiParamConfigService extends IService<ApiParamConfig> {

    /**
     * 根据接口ID获取参数信息
     * @param id
     * @return
     */
    List<ApiParamConfig> getParamByApiId(String id);

    /**
     * 保存接口参数信息
     * @param apiParamConfig
     */
    boolean saveApiParamConfig(ApiParamConfig apiParamConfig);

    /**
     * 根据接口ID删除参数信息
     * @param apiServiceInfoId
     */
    void removeByApiId(String apiServiceInfoId);

    /**
     * 更新接口参数信息
     * @param id
     * @param apiParamConfigList
     */
    void updateApiParamConfig(String id, List<ApiParamConfig> apiParamConfigList);
}
