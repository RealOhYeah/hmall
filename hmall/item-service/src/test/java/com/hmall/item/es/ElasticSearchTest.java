package com.hmall.item.es;

import cn.hutool.json.JSONUtil;
import com.hmall.common.utils.CollUtils;
import com.hmall.item.domain.po.ItemDoc;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
     * 聚合查询---2
     * @throws IOException
     */
    @Test
    void testAggTwo() throws IOException {
        // 1.创建Request
        SearchRequest request = new SearchRequest("items");

        // 设置返回的文档数是0 (只需要返回的聚合结果，可以省去文档信息的返回)
        request.source().size(0);

        // 2.准备请求参数
        String brandAggName = "brandAgg";
        request.source().aggregation(
                AggregationBuilders.terms(brandAggName).field("brand").size(10)
        );

        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4.解析聚合结果
        Aggregations aggregations = response.getAggregations();

        // 4.1.根据聚合名称获取对应的聚合
        Terms brandTerms = aggregations.get(brandAggName);

        // 4.2.获取聚合中的桶(buckets)
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();

        // 4.3.遍历桶内数据
        for (Terms.Bucket bucket : buckets) {

            // 4.4.获取桶内key
            String brand = bucket.getKeyAsString();
            System.out.print("brand = " + brand);
            long count = bucket.getDocCount();
            System.out.println("; count = " + count);
        }
    }


    /**
     * 聚合查询---1
     * @throws IOException
     */
    @Test
    void testAggOne() throws IOException {
        // 1.创建Request
        SearchRequest request = new SearchRequest("items");

        // 2.准备请求参数
        BoolQueryBuilder bool = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("category", "手机"))
                .filter(QueryBuilders.rangeQuery("price").gte(300000));

        // 设置返回的文档数是0 (只需要返回的聚合结果，可以省去文档信息的返回)
        request.source().query(bool).size(0);
        // 3.聚合参数
        request.source().aggregation(
                AggregationBuilders.terms("brand_agg").field("brand").size(5)
        );
        // 4.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 5.解析聚合结果
        Aggregations aggregations = response.getAggregations();
        // 5.1.获取品牌聚合
        Terms brandTerms = aggregations.get("brand_agg");
        // 5.2.获取聚合中的桶
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        // 5.3.遍历桶内数据
        for (Terms.Bucket bucket : buckets) {
            // 5.4.获取桶内key
            String brand = bucket.getKeyAsString();
            System.out.print("brand = " + brand);
            long count = bucket.getDocCount();
            System.out.println("; count = " + count);
        }
    }

    /**
     * 分页查询
     * @throws IOException
     */
    @Test
    void testPageAndSort() throws IOException {
        // 模拟前端传递过来的分页参数
        int pageNo = 1, pageSize = 5;

        // 1.创建Request
        SearchRequest request = new SearchRequest("items");
        // 2.组织请求参数
        // 2.1.搜索条件参数
        request.source().query(QueryBuilders.matchQuery("name", "脱脂牛奶"));
        // 2.2.排序参数
        request.source()
                .sort("sold", SortOrder.DESC)
                .sort("price", SortOrder.ASC);
        // 2.3.分页参数
        request.source().from((pageNo - 1) * pageSize).size(pageSize);
        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        handleResponse(response);
    }

    /**
     * 查询所有
     * @throws IOException
     */
    @Test
    void testMatchAll() throws IOException {
        // 1.创建Request
        SearchRequest request = new SearchRequest("items");
        // 2.组织请求参数
        request.source().query(QueryBuilders.matchAllQuery());
        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        // 4.解析响应
        handleResponse(response);
    }

    /**
     * 根据复杂条件查询
     * @throws IOException
     */
    @Test
    void testSearch() throws IOException {
        // 1.创建Request
        SearchRequest request = new SearchRequest("items");

        // 2.组织请求参数
        request.source().query(QueryBuilders.boolQuery()
                            .must(QueryBuilders.matchQuery("name", "脱脂牛奶"))
                            .filter(QueryBuilders.termQuery("brand", "德亚"))
                            .filter(QueryBuilders.rangeQuery("price").lt(30000))
        );

        // 3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        // 4.解析响应
        handleResponse(response);
    }


    /**
     * 解析结果(包含对高亮查询的处理)
     * @param response
     */
    private void handleResponse(SearchResponse response) {
        SearchHits searchHits = response.getHits();
        // 1.获取总条数
        long total = searchHits.getTotalHits().value;
        System.out.println("共搜索到" + total + "条数据");
        // 2.遍历结果数组
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            // 3.得到_source，也就是原始json文档
            String source = hit.getSourceAsString();
            // 4.反序列化---转为ItemDoc
            ItemDoc item = JSONUtil.toBean(source, ItemDoc.class);
            // 5.获取高亮结果
            Map<String, HighlightField> hfs = hit.getHighlightFields();
            if (CollUtils.isNotEmpty(hfs)) {
                // 5.1.有高亮结果，获取name的高亮结果
                HighlightField hf = hfs.get("name");
                if (hf != null) {
                    // 5.2.获取第一个高亮结果片段，就是商品名称的高亮值
                    // 注意：如果数组这里有很多高亮的字段，可以进行拼接然后输出
                    String hfName = hf.getFragments()[0].string();
                    // 用高亮的结果覆盖非高亮的结果
                    item.setName(hfName);
                }
            }
            System.out.println(item);
        }
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
