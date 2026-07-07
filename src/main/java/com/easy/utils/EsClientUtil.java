package com.easy.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;

public class EsClientUtil {
    // 获取客户端实例（对外提供统一访问入口）
    // 单例客户端实例

    private static ElasticsearchClient esClient;

    // 初始化客户端（静态代码块，项目启动时执行一次）
    static {
        try {
            // 1. 配置ES服务端地址
            String serverUrl = "http://127.0.0.1:9200";

            // 2. 创建带认证的RestClient
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials("elastic", "123456"));  // 使用配置中的用户名和密码

            RestClient restClient = RestClient
                    .builder(HttpHost.create(serverUrl))
                    .setHttpClientConfigCallback(httpClientBuilder ->
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();


            // 注册JSR310模块，支持LocalDate, LocalDateTime等
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // 3. 创建传输层
            RestClientTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper(objectMapper)
            );

            // 4. 创建最终的ES客户端
            esClient = new ElasticsearchClient(transport);
            System.out.println("ES 8.19.17客户端初始化成功！");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ES客户端初始化失败，请检查服务端地址和账号密码");
        }
    }

    public static ElasticsearchClient getEsClient() {
        return esClient;
    }

}
