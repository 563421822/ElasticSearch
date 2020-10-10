package demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.Person;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.junit.Test;
import util.ESClient;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

public class Demo2 {
    ObjectMapper mapper = new ObjectMapper();
    RestHighLevelClient client = ESClient.getClient();
    String index = "person";
    String type = "man";

    /**
     * 创建索引
     *
     * @throws IOException
     */
    @Test
    public void createIndex() throws IOException {
//        准备关于索引的settings
        Settings.Builder settings = Settings.builder()
                .put("number_of_shards", 3)
                .put("number_of_replicas", 1);
//准备关于索引的结构mappings
        XContentBuilder mappings = JsonXContent.contentBuilder()
                .startObject()
                .startObject("properties")
                .startObject("name")
                .field("type", "text")
                .endObject()
                .startObject("age")
                .field("type", "integer")
                .endObject()
                .startObject("birthday")
                .field("type", "date")
                .field("format", "yyyy-MM-dd")
                .endObject()
                .endObject()
                .endObject();
//        将settings和mapping封装到一个Request对象
        CreateIndexRequest request = new CreateIndexRequest(index)
                .settings(settings)
                .mapping(type, mappings);
//        通过client对象去连接ES并执行创建索引
        CreateIndexResponse resp = client.indices().create(request, RequestOptions.DEFAULT);
//        输出
        System.out.println("resp = " + resp);
    }

    /**
     * 检查索引是否存在
     *
     * @throws IOException
     */
    @Test
    public void exists() throws IOException {
//        准备request对象
        GetIndexRequest request = new GetIndexRequest();
        request.indices(index);
//        通过client去操作
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
//        输出
        System.out.println(exists);
    }

    /**
     * 删除索引
     *
     * @throws IOException
     */
    @Test
    public void delete() throws IOException {
//        准备request对象
        DeleteIndexRequest request = new DeleteIndexRequest();
        request.indices(index);
//        通过client对象执行
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
//        获取返回结果
        System.out.println(delete.isAcknowledged());
    }

    /**
     * 创建文档
     *
     * @throws IOException
     */
    @Test
    public void createDoc() throws IOException {
//        准备一个json数据
        Person person = new Person(1, "张三", (byte) 23, new Date());
        String json = mapper.writeValueAsString(person);
        System.out.println(json);
        IndexRequest request = new IndexRequest(index, type, person.getId().toString());
        request.source(json, XContentType.JSON);
//        通过client对象执行添加
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    /**
     * 修改文档
     *
     * @throws IOException
     */
    @Test
    public void updateDoc() throws IOException {
//        创建一个Map,指定需要修改的内容
        HashMap<Object, Object> doc = new HashMap<>();
        doc.put("name", "张大三");
        String docId = "1";
//        创建request对象,封装数据
        UpdateRequest request = new UpdateRequest(index, type, docId);
        request.doc(doc);
//        通过client对象执行
        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
//        输出结果
        System.out.println(update.getGetResult().toString());
    }

    /**
     * 删除文档
     *
     * @throws IOException
     */
    @Test
    public void deleteDoc() throws IOException {
//        封装Request对象
        DeleteRequest request = new DeleteRequest(index, type, "1");
//        client执行
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
//        输出结果
        System.out.println(response.getResult().toString());
    }

    /**
     * 批量添加
     *
     * @throws IOException
     */
    @Test
    public void bulkCreateDoc() throws IOException {
//        准备多个json数据
        Person p1 = new Person(2, "张四", (byte) 23, new Date());
        Person p2 = new Person(2, "张无", (byte) 23, new Date());
        Person p3 = new Person(2, "张六", (byte) 23, new Date());

        String json1 = mapper.writeValueAsString(p1);
        String json2 = mapper.writeValueAsString(p2);
        String json3 = mapper.writeValueAsString(p3);
//        创建Request,将准备好的数据封装进去
        BulkRequest request = new BulkRequest();
        request.add(new IndexRequest(index, type, p1.getId().toString()).source(json1, XContentType.JSON));
        request.add(new IndexRequest(index, type, p2.getId().toString()).source(json2, XContentType.JSON));
        request.add(new IndexRequest(index, type, p3.getId().toString()).source(json3, XContentType.JSON));
//      用client执行
        BulkResponse resp = client.bulk(request, RequestOptions.DEFAULT);
//        输出结果
        System.out.println("resp.toString() = " + resp.toString());
    }

    @Test
    public void bulkDeleteDoc() throws IOException {
//        封装请求对象
        BulkRequest req = new BulkRequest();
        req.add(new DeleteRequest(index, type, "1"));
        req.add(new DeleteRequest(index, type, "2"));
        req.add(new DeleteRequest(index, type, "3"));
//        client执行
        BulkResponse resp = client.bulk(req, RequestOptions.DEFAULT);
//        输出
        System.out.println("resp = " + resp);
    }
}
