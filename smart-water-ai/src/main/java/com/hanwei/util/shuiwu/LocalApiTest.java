package com.hanwei.util.shuiwu;
import java.io.File;
import java.lang.reflect.Field;
import com.hanwei.core.common.api.vo.Result;

/**
 * 本地API独立测试类
 */

public class LocalApiTest {
    // private static final String BASE_URL = "http://10.0.15.21:5627/hanwei";
    private ShuiWuHelper shuiWuHelper;

    public LocalApiTest() {
        this.shuiWuHelper = new ShuiWuHelper();
        try {
            Field urlPrefixField = ShuiWuHelper.class.getDeclaredField("urlPrefix");
            urlPrefixField.setAccessible(true);
            urlPrefixField.set(shuiWuHelper, "http://10.0.15.21:5627/hanwei");
        } catch (Exception e) {
            System.err.println("设置urlPrefix失败: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        LocalApiTest test = new LocalApiTest();
        System.out.println("=== 开始测试本地API ===");

        // 测试创建知识库
        // test.testCreateKnowledgeBase("测试知识库-" + System.currentTimeMillis());
        // 测试获取知识库列表
        // test.testGetKnowledgeBaseList("1", "10", null);
        // 测试编辑知识库
        // test.testEditKnowledgeBase("edccc869-3275-479c-8e59-a850e412eb6f", 0.6, null, null, null, null);
        // test.testEditKnowledgeBase("edccc869-3275-479c-8e59-a850e412eb6f", 0.6, "hybrid_search", 8, false, 0.3);
        // 删除知识库
        // test.testDeleteKnowledgeBase("88009f22-59a4-4a5e-850e-293cfc8f2a9c");
        // 获取知识库详情测试
        // test.testGetKnowledgeBaseDetail("e5176734-ead9-44cf-8bd6-124bc73564e0");
        // 召回测试
        // test.testRagRecall("e5176734-ead9-44cf-8bd6-124bc73564e0", "洋大人的宠儿", 2);
        // 上传文件
        /**
        test.testUploadFileToDataset(
                "02e54617-65ef-435f-815a-85ed6579e634",
                "C:\\Users\\lenovo\\Desktop\\datasets-demo\\222222.txt",
                "\n",
                "text_model",
                "custom",
                "high_quality",
                true,
                true,
                1000
        );
         */

        // 获取知识库文件列表
        // test.testGetKnowledgeBaseFileList("02e54617-65ef-435f-815a-85ed6579e634", "1", "10", null);

        // 启停文档
        // test.testControlDocumentStatus("02e54617-65ef-435f-815a-85ed6579e634", "60f9f674-2b7b-49b8-bfde-f4ac208c7872", "disable");

        // 下载文件（单文件） - 下载到接口服务器时测试
        // 不用 test.testDownloadFile("02e54617-65ef-435f-815a-85ed6579e634", "60f9f674-2b7b-49b8-bfde-f4ac208c7872", null);
        // 文件下载到本地时调用
        // test.testDownloadFileFromServer("02e54617-65ef-435f-815a-85ed6579e634", "60f9f674-2b7b-49b8-bfde-f4ac208c7872", null);
        // 不用 test.testDownloadFileFromServer("e0b33974-287c-42cf-bf85-9aa09fa9ae59", "doc123456", "C:\\temp\\downloads");

        // 删除文档
        // test.testDeleteDocument("02e54617-65ef-435f-815a-85ed6579e634", "60f9f674-2b7b-49b8-bfde-f4ac208c7872");

        // 查询知识库指定文档的切分得到的片段s - todo 这接口有一些问题，没有指定 page 和 size 参数，查出来的 chunks 补全
        // test.testGetDocumentSlicingList("02e54617-65ef-435f-815a-85ed6579e634", "1ead7466-dd54-4caf-a0a4-dd1e3174901f", null);

        // 测试本地模型
        test.testGetModelList("llm");  // embedding  rerank

        // 设置默认模型
        // test.testSetDefaultModel("qwen3:4b", "llm", "langgenius/ollama/ollama");

        System.out.println("=== 测试完成 ===");
    }

    public Result<?> testGetModelList(String modelType) {
        System.out.println("\n--- 测试获取模型列表 ---");
        System.out.println("模型类型: " + modelType);
        Result<?> result = shuiWuHelper.getLocalModelList(modelType);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        if (result.getResult() != null) {
            System.out.println("返回数据: " + result.getResult());
        }
        return result;
    }

    public Result<?> testSetDefaultModel(String model, String modelType, String provider) {
        System.out.println("\n--- 测试设置默认模型 ---");
        System.out.println("模型: " + model + ", 类型: " + modelType + ", 提供商: " + provider);
        Result<?> result = shuiWuHelper.setLocalDefaultModel(model, modelType, provider);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        if (result.getResult() != null) {
            System.out.println("返回数据: " + result.getResult());
        }
        return result;
    }

    public Result<?> testControlDocumentStatus(String kbId, String docId, String action) {
        System.out.println("\n--- 测试文档状态控制 ---");
        System.out.println("知识库ID: " + kbId + ", 文档ID: " + docId + ", 操作: " + action);
        Result<?> result = shuiWuHelper.controlLocalDocumentStatus(kbId, docId, action);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        if (result.getResult() != null) {
            System.out.println("返回数据: " + result.getResult());
        }
        return result;
    }

    public Result<?> testGetDocumentSlicingList(String kbId, String docId, String keywords) {
        System.out.println("\n--- 测试查询文档切片列表 ---");
        System.out.println("知识库ID: " + kbId + ", 文档ID: " + docId + ", 关键词: " + keywords);
        Result<?> result = shuiWuHelper.getLocalDocumentSlicingList(kbId, docId, keywords);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        if (result.getResult() != null) {System.out.println("返回数据: " + result.getResult());}
        return result;
    }

    // 删除知识库中的文档
    public Result<?> testDeleteDocument(String kbId, String docId) {
        System.out.println("\n--- 测试删除文档 ---");
        System.out.println("知识库ID: " + kbId + ", 文档ID: " + docId);
        Result<?> result = shuiWuHelper.deleteLocalDocument(kbId, docId);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        if (result.getResult() != null) {
            System.out.println("返回数据: " + result.getResult());
        }
        return result;
    }

    // 文件下载到本地时调用
    public Result<?> testDownloadFileFromServer(String kbId, String documentId, String downloadDir) {
        System.out.println("\n--- 测试直接下载文件到本地 ---");
        System.out.println("知识库ID: " + kbId + ", 文档ID: " + documentId + ", 下载路径: " + downloadDir);
        Result<?> result = shuiWuHelper.downloadFileFromServer(kbId, documentId, downloadDir);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        if (result.getResult() != null) {
            System.out.println("返回数据: " + result.getResult());
        }
        return result;
    }

    public Result<?> testGetKnowledgeBaseFileList(String kbId, String pageNo, String pageSize, String fileName) {
        System.out.println("\n--- 测试查询知识库文件列表 ---");
        System.out.println("知识库ID: " + kbId);
        Result<?> result = shuiWuHelper.getLocalKnowledgeBaseFileList(kbId, pageNo, pageSize, fileName);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        if (result.getResult() != null) {
            System.out.println("返回数据: " + result.getResult());
        }
        return result;
    }

    public Result<?> testUploadFileToDataset(
            String kbId, String filePath, String separator, String docForm,
            String mode, String indexingTechnique, Boolean removeExtraSpaces, Boolean removeUrlsEmails, Integer maxTokens
    ) {
        System.out.println("\n--- 测试直接上传文件到数据集 ---");
        System.out.println("文件路径: " + filePath);
        Result<?> result = shuiWuHelper.uploadFileToDataset(kbId, filePath, separator, docForm,
                mode, indexingTechnique, removeExtraSpaces,
                removeUrlsEmails, maxTokens);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        if (result.getResult() != null) {
            System.out.println("返回数据: " + result.getResult());
        }
        return result;
    }

    public Result<?> testRagRecall(
            String kbId, String question, Integer topK,
            Boolean score_threshold_enabled, Float vector_similarity_weight,Float similarity_threshold) {
        System.out.println("\n--- 测试知识库召回 ---");
        Result<?> result = shuiWuHelper.ragRecallLocal(
                kbId, question, topK,score_threshold_enabled,vector_similarity_weight,similarity_threshold
        );
        System.out.println(result);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        return result;
    }

    public Result<?> testGetKnowledgeBaseDetail(String kbId) {
        System.out.println("\n--- 测试获取知识库详情 ---");
        Result<?> result = shuiWuHelper.getLocalKnowledgeBaseDetail(kbId);
        System.out.println(result);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        return result;
    }

    public Result<?> testDeleteKnowledgeBase(String kbId) {
        System.out.println("\n--- 测试删除知识库 ---");
        Result<?> result = shuiWuHelper.deleteLocalKnowledgeBase(kbId);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        return result;
    }

    public Result<?> testEditKnowledgeBase(String kbId, Double weights, String searchMethod,
                                           Integer topK, Boolean scoreThresholdEnabled, Double scoreThreshold) {
        System.out.println("\n--- 测试编辑知识库 ---");
        Result<?> result = shuiWuHelper.editLocalKnowledgeBase(kbId, weights, searchMethod, topK, scoreThresholdEnabled, scoreThreshold);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        return result;
    }

    public Result<?> testGetKnowledgeBaseList(String pageNo, String pageSize, String keywords) {
        System.out.println("\n--- 测试获取知识库列表 ---");
        Result<?> result = shuiWuHelper.getLocalKnowledgeBaseList(pageNo, pageSize, keywords);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        return result;
    }

    public Result<?> testCreateKnowledgeBase(String name) {
        System.out.println("\n--- 测试创建知识库 ---");
        Result<?> result = shuiWuHelper.createLocalKnowledgeBase(name);
        System.out.println("结果: " + (result.isSuccess() ? "✅ 成功" : "❌ 失败"));
        return result;
    }
}
