package com.hhy.yunshu.utils;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 云枢-接口ApiUtil
 * @author JiangZhiyan
 */
@SuppressWarnings("unchecked")
public class ApiUtils {

    private static final String USERNAME = "13958054607";
    private static final String PASSWORD = "123456";
    private static final String CORP_ID = "988441da792954ab35c2f4657eb6378f";
    private static final String urlGetList = "http://33.69.3.216/api/api/runtime/query/list";
    private static final String urlCreateData = "http://33.69.3.216/api/api/runtime/form/submit";
    private static final String urlUpdateData = "http://33.69.3.216/api/api/runtime/form/save";
    private static final String urlDeleteData = "http://33.69.3.216/api/api/runtime/query/delete_data";
    private static final String urlBatchUpdate = "http://33.69.3.216/api/api/runtime/query/batchUpdate";
    private String schemaCode;
    private final String token;

    /**
     * @param schemaCode  - 云枢表名
     */
    public ApiUtils(String schemaCode) {
        this.schemaCode = schemaCode;
        try {
            token = initToken();
        }catch (Exception e) {
            XxlJobHelper.log("获取token错误:" + e.getMessage());
            throw new RuntimeException("获取token错误:" + e.getMessage());
        }
    }

    public void setSchemaCode(String schemaCode) {
        this.schemaCode = schemaCode;
    }

    /**
     * 获取token
     */
    private String initToken() {
        String accessToken = token;
        if (token == null) {
            // 1.获取index和key
            JSONObject result = JSONObject.parseObject(HttpUtil.get("http://33.69.3.216/api/public/getKey"));
            // 2.获取code
            Map<String, Object> data = new HashMap<>(6);
            data.put("username", USERNAME);
            data.put("password", this.encrypt(result.get("key").toString(), PASSWORD));
            data.put("url", "http://33.69.3.216/api/login?redirect_uri=http://33.69.3.216/api/oauth/authorize?client_id=api&response_type=code&scope=read&redirect_uri=http://33.69.3.216/oauth");
            data.put("portal", true);
            data.put("corpId", CORP_ID);
            data.put("index", result.get("index"));
            String body = HttpRequest.post("http://33.69.3.216/api/login/Authentication/get_code")
                    .header("Content-Type", "application/json;charset=utf-8")
                    .body((JSON.toJSONString(data))).execute().body();
            result = JSONObject.parseObject(body);
            // 3.获取token
            data = new HashMap<>(5);
            data.put("code", result.get("code"));
            data.put("url", "http://33.69.3.216/api");
            data.put("client_secret", "c31b32364ce19ca8fcd150a417ecce58");
            data.put("client_id", "api");
            data.put("redirect_uri", "http://33.69.3.216/oauth");
            body = HttpRequest.get("http://33.69.3.216/api/login/Authentication/get_token")
                    .header("Content-Type", "application/json;charset=utf-8")
                    .form(data).execute().body();
            result = JSONObject.parseObject(body);
            accessToken = result.get("access_token").toString();
        }
        return accessToken;
    }

    /**
     * 使用公钥对密码进行加密
     * @param publicKeyStr 公钥
     * @param password 明文密码
     * @return 密文密码
     */
    private String encrypt(String publicKeyStr, String password) {
        return new RSA(null,publicKeyStr).encryptBase64(password, StandardCharsets.UTF_8, KeyType.PublicKey);
    }

    /**
     * 获取请求头信息
     */
    private Header[] getHttpHeaders() {
        List<Header> headerList = new ArrayList<>();
        if (token != null) {
            headerList.add(new BasicHeader("Authorization", "Bearer " + this.token));
        }
        headerList.add(new BasicHeader("Content-Type", "application/json;charset=utf-8"));
        return headerList.toArray(new Header[0]);
    }

    /**
     * 发送HTTP请求
     *
     * @param method - HTTP请求方式 { GET|POST|DELETE }
     * @param url    - 请求路径
     * @param data   - 请求的数据
     */
    private Object sendRequest(String method, String url, Map<String, Object> data) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            Header[] headers = this.getHttpHeaders();
            HttpRequestBase request;
            method = method.toUpperCase();
            if ("GET".equalsIgnoreCase(method)) {
                // GET请求
                URIBuilder uriBuilder = new URIBuilder(url);
                if (data != null) {
                    // 添加请求参数
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        uriBuilder.addParameter(entry.getKey(), (String) entry.getValue());
                    }
                }
                request = new HttpGet(uriBuilder.build());
            } else if ("POST".equalsIgnoreCase(method)) {
                // POST请求
                request = new HttpPost(url);
                HttpEntity entity = new StringEntity(JSONObject.toJSONString(data), StandardCharsets.UTF_8);
                ((HttpPost) request).setEntity(entity);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                // DELETE请求
                request = new HttpDeleteWithBody(url);
                HttpEntity entity = new StringEntity(JSONObject.toJSONString(data), StandardCharsets.UTF_8);
                ((HttpDeleteWithBody) request).setEntity(entity);
            } else {
                throw new RuntimeException("不支持的HTTP请求方式");
            }
            // 设置请求头
            request.setHeaders(headers);
            // 发送请求并获取返回结果
            HttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            Map<String, Object> result = JSONObject.parseObject(response.getEntity().getContent(), Map.class);
            if (statusCode >= 400) {
                throw new RuntimeException("请求错误，Error Code: " + result.get("errcode") + ", Error Msg: " + result.get("errmsg"));
            } else {
                // 处理返回结果
                return result;
            }
        }
    }

    /**
     * 获取全部表单数据
     * @return - 全部的表单数据
     */
    public List<Map<String, Object>> getAllFormData() {
        return this.getFormData(Collections.emptyList(),0,Integer.MAX_VALUE);
    }

    /**
     * 按条件获取表单数据
     *
     * @param filters - 过滤条件
     * @param page  - 第几页
     * @param size - 数据条数
     * @return - 返回的数据
     */
    public List<Map<String, Object>> getFormData(final List<Map<String, Object>> filters, final int page, final int size) {
        List<Map<String, Object>> data = null;
        try {
            // 构造请求数据
            Map<String, Object> requestData = new HashMap<String, Object>(6) {
                {
                    put("filters", filters);
                    put("mobile", false);
                    put("page", page);
                    put("size", size);
                    put("queryCode", schemaCode);
                    put("schemaCode", schemaCode);
                }
            };
            Map<String, Object> result = (Map<String, Object>) this.sendRequest("POST", urlGetList, requestData);
            data = (List<Map<String, Object>>) ((Map<String,Object>) result.get("data")).get("content");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 搜索单条数据
     *
     * @param dataId - 要查询的数据id
     * @return 表单数据
     */
    public Map<String, Object> getDataById(String dataId) {
        List<Map<String,Object>> filters = new ArrayList<>(1);
        Map<String,Object> filterMap = new HashMap<String,Object>(5){
            {
                put("op", "Eq");
                put("propertyCode", "id");
                put("propertyType", 0);
                put("propertyValue", dataId);
                put("propertyValueName", "");
            }
        };
        filters.add(filterMap);
        List<Map<String, Object>> formData = this.getFormData(filters, 0, 1);
        if (formData.isEmpty()) {
            return null;
        }else {
            return formData.get(0);
        }
    }

    /**
     * 创建单条数据
     *
     * @param rowData - 创建数据内容
     * @return 创建的数据
     */
    public Map<String, Object> createData(Map<String, Object> rowData) {
        Map<String, Object> data = null;
        try {
            Map<String, Object> requestData = new HashMap<String, Object>(9) {
                {
                    put("workflowCode",null);
                    put("workItemId",null);
                    put("workflowInstanceId",null);
                    put("bizObject",new HashMap<String, Object>(5) {
                        {
                            put("id",null);
                            put("data",rowData);
                            put("schemaCode",schemaCode);
                            put("sheetCode",schemaCode);
                            put("workflowInstanceId","");
                        }
                    });
                    put("agree",true);
                    put("actionCode","submit");
                    put("depatmentId","988441da792954ab35c2f4657eb6378f");
                    put("formType","2");
                    put("replayToken","");
                }
            };
            Map<String, Object> result = (Map<String, Object>) this.sendRequest("POST", urlCreateData, requestData);
            data = (Map<String, Object>) result.get("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


    /**
     * 更新单条数据
     * @param dataId 数据id
     * @param rowData 更新后的值
     */
    public Map<String, Object> updateData(String dataId, Map<String,Object> rowData) {
        Map<String, Object> data = null;
        try {
            Map<String, Object> requestData = new HashMap<String, Object>(9) {
                {
                    put("workflowCode",null);
                    put("workItemId",null);
                    put("workflowInstanceId",null);
                    put("bizObject",new HashMap<String, Object>(5) {
                        {
                            put("id",dataId);
                            put("data",rowData);
                            put("schemaCode",schemaCode);
                            put("sheetCode",schemaCode);
                            put("workflowInstanceId","");
                        }
                    });
                    put("replayToken","");
                }
            };
            Map<String, Object> result = (Map<String, Object>) this.sendRequest("POST", urlUpdateData, requestData);
            data = (Map<String, Object>) result.get("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 批量更新数据
     * @param ids 批量数据id
     * @param propertyCode 字段名称
     * @param modifiedValue 字段更改后的值
     */
    public Map<String, Object> updateDataBatch(List<String> ids, String propertyCode, Object modifiedValue) {
        Map<String, Object> data = null;
        try {
            Map<String, Object> requestData = new HashMap<String, Object>(6) {
                {
                    put("modifiedValue",modifiedValue);
                    put("objectIds",ids);
                    put("propertyCode",propertyCode);
                    put("queryCode",schemaCode);
                    put("schemaCode",schemaCode);
                    put("sheetCode",schemaCode);
                }
            };
            Map<String, Object> result = (Map<String, Object>) this.sendRequest("POST", urlBatchUpdate, requestData);
            data = (Map<String, Object>) result.get("data");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 删除数据
     * @param ids 待删除的数据id
     */
    public Map<String, Object> deleteData(String... ids) {
        Map<String, Object> result = null;
        try {
            Map<String, Object> requestData = new HashMap<String, Object>(2) {
                {
                    put("ids",ids);
                    put("schemaCode",schemaCode);
                }
            };
            result = (Map<String, Object>) this.sendRequest("DELETE", urlDeleteData, requestData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        // 1.获取index和key
        JSONObject result = JSONObject.parseObject(HttpUtil.get("http://33.69.3.216/api/public/getKey"));
        // 2.获取code
        Map<String, Object> data = new HashMap<>(6);
        data.put("username", USERNAME);
        String pwd = new RSA(null, result.get("key").toString()).encryptBase64(PASSWORD, StandardCharsets.UTF_8, KeyType.PublicKey);
        data.put("password", pwd);
        data.put("url", "http://33.69.3.216/api/login?redirect_uri=http://33.69.3.216/api/oauth/authorize?client_id=api&response_type=code&scope=read&redirect_uri=http://33.69.3.216/oauth");
        data.put("portal", true);
        data.put("corpId", CORP_ID);
        data.put("index", result.get("index"));
        String body = HttpRequest.post("http://33.69.3.216/api/login/Authentication/get_code")
                .header("Content-Type", "application/json;charset=utf-8")
                .body(JSON.toJSONString(data)).execute().body();
        result = JSONObject.parseObject(body);
        // 3.获取token
        data = new HashMap<>(5);
        data.put("code", result.get("code"));
        data.put("url", "http://33.69.3.216/api");
        data.put("client_secret", "c31b32364ce19ca8fcd150a417ecce58");
        data.put("client_id", "api");
        data.put("redirect_uri", "http://33.69.3.216/oauth");
        body = HttpRequest.get("http://33.69.3.216/api/login/Authentication/get_token")
                .header("Content-Type", "application/json;charset=utf-8")
                .form(data).execute().body();
        result = JSONObject.parseObject(body);
        String accessToken = result.get("access_token").toString();
        System.out.println(accessToken);
    }
}
