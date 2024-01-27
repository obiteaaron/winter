package tech.obiteaaron.winter.common.tools.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tech.obiteaaron.winter.common.tools.json.JsonUtil;

import java.io.IOException;
import java.util.Map;

@Slf4j
public final class CommonOkHttpClient {

    private final OkHttpClient okHttpClient;

    public CommonOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    private final MediaType CONTENT_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public String doGet(String url) {
        return doGet(url, null, null);
    }

    public String doGet(String url, Map<String, String> urlParams, Map<String, String> headers) {
        String newUrl = buildNewUrl(url, urlParams);
        Request.Builder builder = new Request.Builder().url(newUrl);
        doAddHeaders(builder, headers);
        return doExecute(builder.build());
    }

    public String doPost(String url, String jsonBody) {
        return doPost(url, jsonBody, null, null);
    }

    public String doPost(String url, String jsonBody, Map<String, String> urlParams, Map<String, String> headers) {
        String newUrl = buildNewUrl(url, urlParams);
        Request.Builder builder = new Request.Builder().url(newUrl);
        doAddHeaders(builder, headers);
        if (StringUtils.isNotBlank(jsonBody)) {
            RequestBody requestBody = RequestBody.create(CONTENT_TYPE_JSON, jsonBody);
            builder.method("POST", requestBody);
        }
        return doExecute(builder.build());
    }

    public String doPostForm(String url, Map<String, String> formParams) {
        return doPostForm(url, formParams, null, null);
    }

    public String doPostForm(String url, Map<String, String> formParams, Map<String, String> urlParams, Map<String, String> headers) {
        String newUrl = buildNewUrl(url, urlParams);
        Request.Builder builder = new Request.Builder().url(newUrl);
        doAddHeaders(builder, headers);
        if (formParams != null && !formParams.isEmpty()) {
            MultipartBody.Builder builder1 = new MultipartBody.Builder().setType(MultipartBody.FORM);
            formParams.forEach(builder1::addFormDataPart);
            builder.method("POST", builder1.build());
        }
        return doExecute(builder.build());
    }

    public String doUpload(String url, Map<String, String> urlParams, Map<String, String> formParams, String fileName, byte[] bytes, Map<String, String> headers) {
        String newUrl = buildNewUrl(url, urlParams);
        Request.Builder builder = new Request.Builder().url(newUrl);
        doAddHeaders(builder, headers);
        MultipartBody.Builder builder1 = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (formParams != null && !formParams.isEmpty()) {
            formParams.forEach(builder1::addFormDataPart);
        }
        if (StringUtils.isNotBlank(fileName) && bytes != null) {
            builder1.addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("application/octet-stream"), bytes));
        }
        builder.method("POST", builder1.build());
        return doExecute(builder.build());
    }

    private void doAddHeaders(Request.Builder builder, Map<String, String> headers) {
        if (headers == null) {
            return;
        }
        headers.forEach(builder::addHeader);
    }

    private String buildNewUrl(String url, Map<String, String> urlParams) {
        if (urlParams == null || urlParams.isEmpty()) {
            return url;
        }
        LinkedMultiValueMap<String, String> linkedMultiValueMap = new LinkedMultiValueMap<>();
        urlParams.forEach(linkedMultiValueMap::set);
        UriComponents build = UriComponentsBuilder.fromHttpUrl(url).queryParams(linkedMultiValueMap).build();
        return build.toString();
    }

    private String doExecute(Request request) {
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("OkHttp_Failed {}", JsonUtil.toJsonString(response));
                throw new RuntimeException("OkHttp_Failed");
            }
            if (response.isRedirect()) {
                log.error("OkHttp_Redirect {}", JsonUtil.toJsonString(response));
                throw new RuntimeException("OkHttp_Redirect");
            }
            ResponseBody body = response.body();
            if (body == null) {
                log.error("OkHttp_EmptyBody {}", JsonUtil.toJsonString(response));
                throw new RuntimeException("OkHttp_EmptyBody");
            }
            return body.string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
