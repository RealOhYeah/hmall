package com.hmall.item.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.directory.SearchResult;
import java.io.IOException;

public class ElasticSearchTest {

    private RestHighLevelClient client;

    /**
     * 连接es
     */
    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.88.132:9200")
        ));
    }

    /**
     * 测试连接
     */
    @Test
    void test() {
        System.out.println("连接信息" + client.toString());
    }

    /**
     * 查询所有
     * @throws IOException
     */
    @Test
    void testMatchAll() throws IOException {
        // 1.创建request对象
        SearchRequest request = new SearchRequest("items");

        // 2.配置request参数
        request.source()
                .query(QueryBuilders.matchAllQuery());

        // 3.发送请求
        client.search(request, RequestOptions.DEFAULT);

        System.out.println("result" + request);


    }


    /**
     * 关闭连接
     * @throws IOException
     */
    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

}
