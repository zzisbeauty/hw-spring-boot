package com.hanwei.util.shuiwu;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.http.Method;
import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.hanwei.core.common.api.vo.Result;
import com.hanwei.rag.bo.ChoiceRagBO;
import com.hanwei.rag.bo.MessageBO;
import com.hanwei.rag.entity.RagInfo;
import com.hanwei.rag.vo.RagRecallVO;
import com.hanwei.util.yanjiuyuan.YjyResult;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author CX
 * @version : [v1.0]
 * @description : [调用研究院提供接口]
 * @createTime : [2025/5/26 10:00]
 * @updateRemark : [说明本次修改内容]
 */
@Service
@Slf4j
public class ShuiWuHelper {
    @Value("${shuiwu.urlPrefix}")
    private String urlPrefix;

    @Value("${shuiwu.bearerToken}")
    private String bearerToken;

    private final String USERNAME= "UserName"; // 研究院鉴权临时用，后期去掉
    private final String WORKNO="H05583"; // 研究院鉴权临时用，后期去掉


    /**
     * 获取知识库列表 - 本地接口版本
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param ragName 知识库名称（可选）
     * @return Result<Object>
     */
    public Result<?> getLocalKnowledgeBaseList(String pageNo, String pageSize, String ragName) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("page", pageNo);
            paramMap.put("page_size", pageSize);
            if (StrUtil.isNotEmpty(ragName)) {paramMap.put("keywords", ragName);}
            log.info("调用本地获取知识库列表接口 发送参数: " + paramMap);
            HttpRequest request = HttpUtil.createGet(urlPrefix + "/v1/kb/list")
                    .header("Content-Type", "application/json").form("page", pageNo).form("page_size", pageSize);
            if (StrUtil.isNotEmpty(ragName)) {request.form("keywords", ragName);}
            String resultStr = request.execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr); // 解析本地接口返回的结果
            if (response.getInt("code") == 0) { // 成功情况
                Object data = response.get("data");
                log.info("调用本地获取知识库列表接口成功 返回数据: " + data);
                return Result.OK("获取知识库列表成功", data);
            } else { // 失败情况
                String message = response.getStr("message");
                log.error("调用本地获取知识库列表接口失败: " + message);
                return Result.error("获取知识库列表失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地获取知识库列表接口异常", e);
            return Result.error("获取知识库列表异常: " + e.getMessage());
        }
    }

    // 创建知识库
    public Result<?> createLocalKnowledgeBase(String ragName) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("name", ragName);
            log.info("调用本地创建知识库接口 发送参数: " + paramMap);
            String resultStr = HttpUtil.createPost(urlPrefix + "/v1/kb/create")
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(paramMap)).execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用本地创建知识库接口成功 返回数据: " + data);
                return Result.OK("创建知识库成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地创建知识库接口失败: " + message);
                return Result.error("创建知识库失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地创建知识库接口异常", e);
            return Result.error("创建知识库异常: " + e.getMessage());
        }
    }

    // 获取知识库详情 - √
    public Result<?> getLocalKnowledgeBaseDetail(String kbId) {
        try {
            log.info("调用本地获取知识库详情接口 发送参数: " + kbId);
            String resultStr = HttpUtil.createGet(urlPrefix + "/v1/kb/detail")
                    .header("Content-Type", "application/json").form("kb_id", kbId)
                    .execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用本地获取知识库详情接口成功");
                return Result.OK("获取知识库详情成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地获取知识库详情接口失败: " + message);
                return Result.error("获取知识库详情失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地获取知识库详情接口异常", e);
            return Result.error("获取知识库详情异常: " + e.getMessage());
        }
    }

    /**
     * 编辑知识库
     * @param kbId 知识库ID  必填参数
     * @param weights 混合检索语义权重（可选）
     * @param searchMethod 检索方法（可选）
     * @param topK 默认召回的片段数量（可选）
     * @param scoreThresholdEnabled 语义检索阈值开关（可选）
     * @param scoreThreshold 语义检索召回阈值（可选）
     * @return Result<Object>
     */
    public Result<?> editLocalKnowledgeBase(
            String kbId, Double weights, String searchMethod,
            Integer topK, Boolean scoreThresholdEnabled, Double scoreThreshold
    ) {
        try {
            System.out.println("\n--- 测试编辑知识库 ---");
            System.out.println("知识库ID: " + kbId);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("kb_id", kbId);
            // 可选参数 - 只有非空时才添加
            if (weights != null) {
                jsonBody.put("weights", weights);
            }
            if (StrUtil.isNotEmpty(searchMethod)) {
                jsonBody.put("search_method", searchMethod);
            }
            if (topK != null) {
                jsonBody.put("top_k", topK);
            }
            if (scoreThresholdEnabled != null) {
                jsonBody.put("score_threshold_enabled", scoreThresholdEnabled);
            }
            if (scoreThreshold != null) {
                jsonBody.put("score_threshold", scoreThreshold);
            }

            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder().url(urlPrefix + "/v1/kb/update").patch(body).build();
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            System.out.println("响应结果: " + responseBody);
            JSONObject responseJson = JSONUtil.parseObj(responseBody);
            if (responseJson.getInt("code") == 0) {
                Object data = responseJson.get("data");
                System.out.println("✅ 编辑知识库成功");
                return Result.OK("编辑知识库成功", data);
            } else {
                String message = responseJson.getStr("message");
                System.out.println("❌ 编辑知识库失败: " + message);
                return Result.error("编辑知识库失败: " + message);
            }

        } catch (Exception e) {
            System.out.println("❌ 编辑知识库异常: " + e.getMessage());
            e.printStackTrace();
            return Result.error("编辑知识库异常: " + e.getMessage());
        }
    }

    /**
     * 删除知识库
     * @param kbId 知识库ID
     * @return Result<Object>
     */
    public Result<?> deleteLocalKnowledgeBase(String kbId) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("kb_id", kbId);
            log.info("调用本地删除知识库接口 发送参数: " + paramMap);
            String resultStr = HttpUtil.createRequest(Method.DELETE, urlPrefix + "/v1/kb/rm")
                    .header("Content-Type", "application/json").body(JSONUtil.toJsonStr(paramMap))
                    .execute().body();

            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) { // 成功情况
                Object data = response.get("data");
                log.info("调用本地删除知识库接口成功 返回数据: " + data);
                return Result.OK("删除知识库成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地删除知识库接口失败: " + message);
                return Result.error("删除知识库失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地删除知识库接口异常", e);
            return Result.error("删除知识库异常: " + e.getMessage());
        }
    }

    // 检索召回
    public Result<?> ragRecallLocal(
            String kbId, String question, Integer topK,
            Boolean score_threshold_enabled, Float vector_similarity_weight,Float similarity_threshold) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("kb_id", kbId);
            paramMap.put("question", question);
            paramMap.put("top_k", topK);
            paramMap.put("score_threshold_enabled", score_threshold_enabled);
            paramMap.put("vector_similarity_weight", vector_similarity_weight);
            paramMap.put("similarity_threshold", similarity_threshold);
            log.info("调用本地知识库召回接口 发送参数: " + paramMap);
            String resultStr = HttpUtil.createPost(urlPrefix + "/v1/chunk/retrieval_test")
                    .header("Content-Type", "application/json").body(JSONUtil.toJsonStr(paramMap)).execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用本地知识库召回接口成功");
                return Result.OK("知识库召回成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地知识库召回接口失败: " + message);
                return Result.error("知识库召回失败: " + message);
            }

        } catch (Exception e) {
            log.error("调用本地知识库召回接口异常", e);
            return Result.error("知识库召回异常: " + e.getMessage());
        }
    }

    // 上传文件
    public Result<?> uploadFileToDataset(
            String kbId, String filePath, String separator,
            String docForm, String mode, String indexingTechnique,
            Boolean removeExtraSpaces, Boolean removeUrlsEmails, Integer maxTokens
    ) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {return Result.error("文件不存在: " + filePath);}
            String originalFileName = file.getName();
            // 构造 data_json，模拟Python代码的参数结构
            Map<String, Object> dataJson = buildDataJson(
                    indexingTechnique != null ? indexingTechnique : "high_quality",
                    docForm != null ? docForm : "text_model",
                    "Chinese",
                    separator != null ? separator : "\n",
                    maxTokens != null ? maxTokens : 1000,
                    removeUrlsEmails != null ? removeUrlsEmails : true,
                    mode != null ? mode : "custom"
            );
            String dataJsonStr = JSONUtil.toJsonStr(dataJson);
            String uploadUrl = "http://10.30.30.97:8080/v1/datasets/" + kbId + "/document/create_by_file";
            // 准备multipart/form-data参数，包含原始文件名
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("file", file);
            paramMap.put("data", dataJsonStr);
            paramMap.put("name", originalFileName);

            log.info("调用 Dify 文件上传API，URL: " + uploadUrl);
            log.info("原始文件名: " + originalFileName);
            log.info("data_json参数: " + dataJsonStr);

            String resultStr = HttpUtil.createPost(uploadUrl)
                    .header("Authorization", "Bearer " + bearerToken)
                    .form(paramMap).execute().body();
            log.info("Dify 上传接口返回: " + resultStr);

            // 解析返回结果 - 针对 Dify 接口格式进行判断
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response == null) {return Result.error("接口返回为空");}

            // Dify 接口的成功判断逻辑（根据实际返回格式调整）
            if (
                    response.containsKey("document_id") ||
                    response.containsKey("id") ||
                    response.containsKey("batch") ||
                    (response.containsKey("created_at") && response.get("created_at") != null)
            ) {
                log.info("Dify 文件上传成功");
                return Result.OK("文件上传成功", response);
            } else if (response.containsKey("code")) {
                // 如果有错误码字段
                Integer code = response.getInt("code");
                if (code != null && code == 200) {
                    return Result.OK("文件上传成功", response);
                } else {
                    String message = response.getStr("message", response.getStr("detail", "上传失败"));
                    log.error("Dify 文件上传失败，错误码: {}, 消息: {}", code, message);
                    return Result.error("文件上传失败: " + message);
                }
            } else if (response.containsKey("error")) {
                // 如果有错误信息字段
                String errorMsg = response.getStr("error");
                log.error("Dify 文件上传失败: " + errorMsg);
                return Result.error("文件上传失败: " + errorMsg);
            } else {
                // 兜底判断：如果没有明确的错误信息，且有返回内容，认为成功
                log.info("Dify 文件上传完成，返回内容: " + resultStr);
                return Result.OK("文件上传成功", response);
            }

        } catch (Exception e) {
            log.error("调用 Dify 文件上传接口异常", e);
            return Result.error("文件上传异常: " + e.getMessage());
        }
    }

    // 构造data_json参数，完全模拟Python代码的结构
    private Map<String, Object> buildDataJson(
            String indexingTechnique, String docForm, String docLanguage,
            String separator, Integer maxTokens, Boolean removeUrlsEmails, String mode
    ) {
        Map<String, Object> dataJson = new HashMap<>();
        dataJson.put("indexing_technique", indexingTechnique);
        dataJson.put("doc_form", docForm);
        dataJson.put("doc_language", docLanguage);
        // 构造 process_rule
        Map<String, Object> processRule = new HashMap<>();
        Map<String, Object> rules = new HashMap<>();
        // pre_processing_rules
        List<Map<String, Object>> preProcessingRules = new ArrayList<>();
        Map<String, Object> removeSpacesRule = new HashMap<>();
        removeSpacesRule.put("id", "remove_extra_spaces");
        removeSpacesRule.put("enabled", true);
        preProcessingRules.add(removeSpacesRule);

        Map<String, Object> removeUrlsRule = new HashMap<>();
        removeUrlsRule.put("id", "remove_urls_emails");
        removeUrlsRule.put("enabled", removeUrlsEmails);
        preProcessingRules.add(removeUrlsRule);

        rules.put("pre_processing_rules", preProcessingRules);

        // segmentation
        Map<String, Object> segmentation = new HashMap<>();
        segmentation.put("separator", separator);
        segmentation.put("max_tokens", maxTokens);
        rules.put("segmentation", segmentation);

        processRule.put("rules", rules);
        processRule.put("mode", mode);

        dataJson.put("process_rule", processRule);
        return dataJson;
    }

    // 查询知识库中的文档列表
    public Result<?> getLocalKnowledgeBaseFileList(String kbId, String pageNo, String pageSize, String fileName) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("kb_id", kbId);
            paramMap.put("page", pageNo != null ? pageNo : "1");
            paramMap.put("page_size", pageSize != null ? pageSize : "10");
            if (StrUtil.isNotEmpty(fileName)) {paramMap.put("keywords", fileName);}
            log.info("调用本地查询知识库文件列表接口 发送参数: " + paramMap);
            HttpRequest request = HttpUtil.createGet(urlPrefix + "/v1/document/list")
                    .header("Content-Type", "application/json")
                    .form("kb_id", kbId)
                    .form("page", pageNo != null ? pageNo : "1")
                    .form("page_size", pageSize != null ? pageSize : "10");
            if (StrUtil.isNotEmpty(fileName)) {
                request.form("keywords", fileName);
            }
            String resultStr = request.execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用本地查询知识库文件列表接口成功");
                return Result.OK("查询文件列表成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地查询知识库文件列表接口失败: " + message);
                return Result.error("查询文件列表失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地查询知识库文件列表接口异常", e);
            return Result.error("查询文件列表异常: " + e.getMessage());
        }
    }

    // 本地直接请求服务器下载文件，可以下载文件到本地
    public Result<?> downloadFileFromServer(String kbId, String documentId, String downloadDir) {
        try {
            // 获取项目根路径并创建downloads目录
            String projectRoot = System.getProperty("user.dir");
            String defaultDownloadDir = projectRoot + File.separator + "downloads";
            String actualDownloadDir = StrUtil.isNotEmpty(downloadDir) ? downloadDir : defaultDownloadDir;
            // 确保下载目录存在
            File downloadDirFile = new File(actualDownloadDir);
            if (!downloadDirFile.exists()) {
                downloadDirFile.mkdirs();
                log.info("创建下载目录: " + actualDownloadDir);
            }
            // 第一步：调用获取文件信息的接口
            String fileInfoUrl = "http://10.30.30.97:8080/v1/datasets/" + kbId + "/documents/" + documentId + "/upload-file";
            log.info("调用获取文件信息接口，URL: " + fileInfoUrl);
            HttpResponse infoResponse = HttpUtil.createGet(fileInfoUrl)
                    .header("Authorization", "Bearer " + bearerToken).execute();
            if (infoResponse.getStatus() != 200) {
                log.error("获取文件信息失败，HTTP状态码: " + infoResponse.getStatus());
                return Result.error("获取文件信息失败，HTTP状态码: " + infoResponse.getStatus());
            }
            // 解析文件信息
            String infoResponseBody = infoResponse.body();
            log.info("文件信息响应: " + infoResponseBody);
            JSONObject fileInfo = JSONUtil.parseObj(infoResponseBody);
            // 获取下载信息
            String downloadUrl = fileInfo.getStr("download_url");
            String fileName = fileInfo.getStr("name");
            if (StrUtil.isEmpty(fileName)) {
                fileName = "document_" + documentId;
            }
            if (StrUtil.isEmpty(downloadUrl)) {
                return Result.error("未找到下载链接");
            }
            // 处理相对路径URL（模拟Python逻辑）
            if (downloadUrl.startsWith("/")) {
                downloadUrl = "http://10.30.30.97:8080" + downloadUrl;
            }
            log.info("实际下载URL: " + downloadUrl);
            log.info("文件名: " + fileName);
            // 第二步：下载实际文件
            HttpResponse downloadResponse = HttpUtil.createGet(downloadUrl).execute();
            if (downloadResponse.getStatus() != 200) {
                return Result.error("下载文件失败，状态码: " + downloadResponse.getStatus());
            }
            // 保存文件
            String filePath = actualDownloadDir + File.separator + fileName;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                byte[] fileBytes = downloadResponse.bodyBytes();
                fos.write(fileBytes);
                fos.flush();
            }
            log.info("文件下载成功，保存路径: " + filePath);
            // 构造返回数据（模拟Python返回格式）
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("file_path", filePath);
            resultData.put("file_info", fileInfo);
            resultData.put("download_url", downloadUrl);
            return Result.OK("文档已成功下载到: " + filePath, resultData);
        } catch (Exception e) {
            log.error("下载文件异常", e);
            return Result.error("下载失败: " + e.getMessage());
        }
    }

    // 知识库文件删除 - docIdList
    public Result<?> deleteLocalDocument(String kbId, String docId) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("kb_id", kbId);
            paramMap.put("doc_id", docId);
            log.info("调用本地删除文档接口 发送参数: " + paramMap);
            String resultStr = HttpUtil.createRequest(Method.DELETE, urlPrefix + "/v1/document/rm")
                    .header("Content-Type", "application/json").body(JSONUtil.toJsonStr(paramMap))
                    .execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用本地删除文档接口成功");
                return Result.OK("删除文档成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地删除文档接口失败: " + message);
                return Result.error("删除文档失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地删除文档接口异常", e);
            return Result.error("删除文档异常: " + e.getMessage());
        }
    }

    // 文档切片查询
    public Result<?> getLocalDocumentSlicingList(String kbId, String docId, String keywords) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("kb_id", kbId);
            paramMap.put("doc_id", docId);
            if (StrUtil.isNotEmpty(keywords)) {paramMap.put("keywords", keywords);}
            log.info("调用本地查询文档切片列表接口 发送参数: " + paramMap);
            HttpRequest request = HttpUtil.createGet(urlPrefix + "/v1/chunk/list")
                    .header("Content-Type", "application/json").form("kb_id", kbId).form("doc_id", docId);
            if (StrUtil.isNotEmpty(keywords)) {
                request.form("keywords", keywords);
            }
            String resultStr = request.execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用本地查询文档切片列表接口成功");
                return Result.OK("查询文档切片列表成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地查询文档切片列表接口失败: " + message);
                return Result.error("查询文档切片列表失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地查询文档切片列表接口异常", e);
            return Result.error("查询文档切片列表异常: " + e.getMessage());
        }
    }

    // 文档启停状态控制
    public Result<?> controlLocalDocumentStatus(String kbId, String docId, String action) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("kb_id", kbId);
            paramMap.put("doc_id", docId);
            paramMap.put("action", action);
            log.info("调用本地文档状态控制接口 发送参数: " + paramMap);
            String resultStr = HttpUtil.createPost(urlPrefix + "/v1/document/change_status")
                    .header("Content-Type", "application/json").body(JSONUtil.toJsonStr(paramMap))
                    .execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                String actionDesc = "enable".equals(action) ? "启用" : "禁用";
                log.info("调用本地文档状态控制接口成功");
                return Result.OK("文档" + actionDesc + "成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地文档状态控制接口失败: " + message);
                return Result.error("文档状态控制失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地文档状态控制接口异常", e);
            return Result.error("文档状态控制异常: " + e.getMessage());
        }
    }

    // 设置系统的默认模型
    public Result<?> setLocalDefaultModel(String model, String modelType, String provider) {
        try {
            if (StrUtil.isEmpty(model)) {
                return Result.error("model参数不能为空");
            }
            if (StrUtil.isEmpty(modelType)) {
                return Result.error("model_type参数不能为空");
            }
            if (StrUtil.isEmpty(provider)) {
                return Result.error("provider参数不能为空");
            }

            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("model", model);
            paramMap.put("model_type", modelType);
            paramMap.put("provider", provider);
            log.info("调用本地设置默认模型接口 发送参数: " + paramMap);
            String resultStr = HttpUtil.createPost(urlPrefix + "/v1/user/set_default")
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(paramMap))
                    .execute()
                    .body();

            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用本地设置默认模型接口成功");
                return Result.OK("设置默认模型成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地设置默认模型接口失败: " + message);
                return Result.error("设置默认模型失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地设置默认模型接口异常", e);
            return Result.error("设置默认模型异常: " + e.getMessage());
        }
    }

    // 获取模型列表
    public Result<?> getLocalModelList(String modelType) {
        try {
            if (StrUtil.isEmpty(modelType)) {
                return Result.error("model_type参数不能为空");
            }
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("model_type", modelType);
            log.info("调用本地获取模型列表接口 发送参数: " + paramMap);
            HttpRequest request = HttpUtil.createGet(urlPrefix + "/v1/llm/my_llms")
                    .header("Content-Type", "application/json").form("model_type", modelType);
            String resultStr = request.execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用本地获取模型列表接口成功");
                return Result.OK("获取模型列表成功", data);
            } else {
                String message = response.getStr("message");
                log.error("调用本地获取模型列表接口失败: " + message);
                return Result.error("获取模型列表失败: " + message);
            }
        } catch (Exception e) {
            log.error("调用本地获取模型列表接口异常", e);
            return Result.error("获取模型列表异常: " + e.getMessage());
        }
    }

    public Result<?> chunkStatusSwitch(String kbId, String docId, List<String> chunkIds, Integer availableInt) {
        try {
            if (StrUtil.isEmpty(kbId)) {return Result.error("kb_id参数不能为空");}
            if (StrUtil.isEmpty(docId)) {return Result.error("doc_id参数不能为空");}
            if (chunkIds == null || chunkIds.isEmpty()) {return Result.error("chunk_ids参数不能为空");}
            if (availableInt == null) {return Result.error("available_int参数不能为空");}
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("kb_id", kbId);
            paramMap.put("doc_id", docId);
            paramMap.put("chunk_ids", chunkIds);
            paramMap.put("available_int", availableInt);
            log.info("调用研究院文档切片状态切换接口 发送参数: " + paramMap);
            String resultStr = HttpUtil.createPost(urlPrefix + "/v1/chunk/switch")
                    .header(USERNAME, WORKNO).body(JSONUtil.toJsonStr(paramMap)).execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用研究院文档切片状态切换接口成功");
                return Result.OK("文档切片状态切换成功", data);
            } else {
                String errorMessage = response.getStr("message");
                log.error("调用研究院文档切片状态切换接口失败: " + errorMessage);
                return Result.error("文档切片状态切换失败: " + errorMessage);
            }
        } catch (Exception e) {
            log.error("调用研究院文档切片状态切换接口异常", e);
            return Result.error("文档切片状态切换异常: " + e.getMessage());
        }
    }

    public Result<?> getDocumentStatus(String kbId, String batch) {
        try {
            if (StrUtil.isEmpty(kbId)) {return Result.error("kb_id参数不能为空");}
            if (StrUtil.isEmpty(batch)) {return Result.error("batch参数不能为空");}
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("kb_id", kbId);
            paramMap.put("batch", batch);
            log.info("调用研究院获取文档处理进度接口 发送参数: " + paramMap);
            String resultStr = HttpUtil.createPost(urlPrefix + "/v1/document/status")
                    .header(USERNAME, WORKNO).body(JSONUtil.toJsonStr(paramMap)).execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用研究院获取文档处理进度接口成功");
                return Result.OK("获取文档处理进度成功", data);
            } else {
                String errorMessage = response.getStr("message");
                log.error("调用研究院获取文档处理进度接口失败: " + errorMessage);
                return Result.error("获取文档处理进度失败: " + errorMessage);
            }
        } catch (Exception e) {
            log.error("调用研究院获取文档处理进度接口异常", e);
            return Result.error("获取文档处理进度异常: " + e.getMessage());
        }
    }

    /**
     * 设置知识库以及调用模型对话
     * @param userId 用户ID
     * @param message 消息内容
     * @param conversationId 会话ID
     * @param kbId 知识库ID
     * @return
     * @throws Exception
     */
    public Result<?> modelDialogueNew(String userId, String message, String conversationId, String kbId) {
        try {
            if (StrUtil.isEmpty(userId)) {return Result.error("user_id参数不能为空");}
            if (StrUtil.isEmpty(message)) {return Result.error("message参数不能为空");}
            if (StrUtil.isEmpty(kbId)) {return Result.error("kb_id参数不能为空");}
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("user_id", userId);
            paramMap.put("message", message);
            paramMap.put("conversation_id", conversationId);
            paramMap.put("kb_id", kbId);
            log.info("调用研究院模型对话接口 发送参数: " + paramMap);
            String resultStr = HttpUtil.createPost(urlPrefix + "/v1/conversation/completion_db")
                    .header(USERNAME, WORKNO).body(JSONUtil.toJsonStr(paramMap)).execute().body();
            JSONObject response = JSONUtil.parseObj(resultStr);
            if (response.getInt("code") == 0) {
                Object data = response.get("data");
                log.info("调用研究院模型对话接口成功");
                return Result.OK("模型对话调用成功", data);
            } else {
                String errorMessage = response.getStr("message");  // 重命名为 errorMessage
                log.error("调用研究院模型对话接口失败: " + errorMessage);
                return Result.error("模型对话调用失败: " + errorMessage);
            }
        } catch (Exception e) {
            log.error("调用研究院模型对话接口异常", e);
            return Result.error("模型对话调用异常: " + e.getMessage());
        }
    }



    //================================大模型知识库调用=========================================/

    /**
     * 查询知识库知识图谱
     * @param id
     * @return
     * @throws Exception
     */
    public YjyResult getRagGraphInfo(String id) throws Exception{
        log.info("调用研究院获取知识库知识图谱接口 发送报文: " + id);
        String resultStr = HttpUtil.createGet(urlPrefix+"/v1/kb/"+id+"/knowledge_graph")
                .header(USERNAME,WORKNO).execute().body();
        YjyResult result = JSONUtil.toBean(resultStr, YjyResult.class);
        log.info("调用研究院获取知识库知识图谱接口 返回报文: " +result);
        return result;
    }
}






