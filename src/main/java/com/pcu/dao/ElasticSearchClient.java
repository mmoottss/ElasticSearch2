
package com.pcu.dao;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ElasticSearchClient {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchClient.class);
    private static RestHighLevelClient httpClient=null;
    
    private static String elasticHttpHosts = "192.168.0.171:9201,192.168.0.172:9201";
    private static String elasticUserId = "elastic";
    private static String elasticPassword = "elevisor";

    
    public static RestHighLevelClient getHttpClient(){
    	if(httpClient==null) initialize();
        return httpClient;
    }

    private static void initialize()  {
        initializeRestHighLevelClient();
    }

    private static void initializeRestHighLevelClient()  {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(elasticUserId, elasticPassword));

        Optional<HttpHost[]> httpHosts=parseHttpNodes(elasticHttpHosts);

        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts.get())
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        httpClient = new RestHighLevelClient(restClientBuilder);
    }

    public static  void close() {
        try {
            httpClient.close();
        }catch (Exception e){
        }
    }

    private static List<AddressPairs> parseTransportNodes(String nodes) {
        List<AddressPairs> pairsList = new LinkedList<>();
        String[] nodesSplit = nodes.split(",");
        for (String node : nodesSplit) {
            String host = node.split(":")[0];
            String port = node.split(":")[1];
            pairsList.add(new AddressPairs(host, Integer.valueOf(port)));
        }

        return pairsList;
    }
    private static Optional<HttpHost[]> parseHttpNodes(String nodes) {

        logger.info("elasticsearch http hosts : {}", nodes);
        String[] nodesSplit = nodes.split(",");

        HttpHost[] httpHosts = Arrays.stream(nodesSplit)
                .map((node) -> {
                    String host = node.split(":")[0];
                    String port = node.split(":")[1];
                    return new HttpHost(host, Integer.valueOf(port));
                })
                .toArray(size -> new HttpHost[size]);
        return httpHosts.length > 0 ? Optional.of(httpHosts) : Optional.empty();
    }
    static  class AddressPairs {
        private String host;
        private Integer port;

        AddressPairs(String host, Integer port) {
            this.host = host;
            this.port = port;
        }
    }

    public static boolean deleteIndex(String indexName) {

        try{
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            AcknowledgedResponse response = httpClient.indices().delete(request, RequestOptions.DEFAULT);
            if (!response.isAcknowledged()) {
                logger.warn("deleteIndex Error indexName="+indexName);
                return false;
            } else {
                logger.info("deleteIndex Successfully indexName="+indexName);
            }
            return true;
        }catch (IOException e){
            logger.warn("deleteIndex Failures  indexName="+indexName+", msg="+e.getMessage(),e);
            return false;
        }
    }

    public static  void putTemplate(org.elasticsearch.client.indices.PutIndexTemplateRequest request) throws Exception{

        try{
            AcknowledgedResponse response = httpClient.indices().putTemplate(request,RequestOptions.DEFAULT);
            if (!response.isAcknowledged()) {
                logger.warn("putTemplate Error While Updating Template");
            } else {
                logger.info("putTemplate Successfully");
            }
        }catch (Throwable e){
            logger.warn("putTemplate Failures msg="+e.getMessage(),e);
            throw e;
        }
    }

    public static  boolean existsIndex(String indexName) {
        GetIndexRequest request = new GetIndexRequest(indexName);

        try{
            return httpClient.indices().exists(request,RequestOptions.DEFAULT);
        }catch (IOException e){
            logger.warn("existsIndex Failures indexName="+indexName+", msg="+e.getMessage(),e);
        }
        return false;

    }

    public static boolean index(String indexName, Map<String, Object> map) {
        return index(indexName,null,map);
    }

    public static boolean index(String indexName, String docId, Map<String, Object> map) {
         boolean isSuccess=true;

        try {
            IndexRequest request = new IndexRequest(indexName).source(map);
            if(docId!=null) request.id(docId);

            IndexResponse response= httpClient.index(request,RequestOptions.DEFAULT);

            if (response.getResult() == DocWriteResponse.Result.CREATED) {
               // Handle (if needed) the case where the document was created for the first time
            } else if (response.getResult() == DocWriteResponse.Result.UPDATED) {
                // Handle (if needed) the case where the document was rewritten as it was already existing
            }
            ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
                 // Handle the situation where number of successful shards is less than total shards
            }
            if (shardInfo.getFailed() > 0) {
                isSuccess=false;
                for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                    logger.warn("Index Failures indexName="+indexName+", docId="+docId+", msg="+failure.reason());
                }
            }
        } catch (IOException e) {
            logger.warn("get Failures indexName="+indexName+", docId="+docId+", msg=" + e.getMessage(), e);
            isSuccess=false;
        }
        return isSuccess;
    }


    public static boolean delete(String indexName, String docId) {
        DeleteRequest request = new DeleteRequest(indexName,docId);

        try {
            DeleteResponse deleteResponse = httpClient.delete(request, RequestOptions.DEFAULT);
            ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();

//            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
//            }else{
//                logger.warn("Delete Failures indexName="+indexName+", docId="+docId);
//            }
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure :shardInfo.getFailures()) {
                    logger.warn("Delete Failures indexName="+indexName+", docId="+docId+", msg="+failure.reason());
                }
                return false;
            }
        } catch (IOException e) {
            logger.warn("get Failures indexName="+indexName+", docId="+docId+", msg=" + e.getMessage(), e);
            return false;
        }
        return true;
    }


    public static SearchResponse search(SearchRequest searchRequest) {

        try {
            SearchResponse searchResponse = httpClient.search(searchRequest,RequestOptions.DEFAULT);
            return searchResponse;
        } catch (IOException e) {
            logger.warn("search Failures msg=" + e.getMessage(), e);
            return null;
        }
    }

    public static SearchResponse scroll(SearchScrollRequest searchScrollRequest) {

        try {
            SearchResponse searchResponse = httpClient.scroll(searchScrollRequest,RequestOptions.DEFAULT);
            return searchResponse;
        } catch (IOException e) {
            logger.warn("search Failures msg=" + e.getMessage(), e);
            return null;
        }
    }

    public static ClearScrollResponse clearScroll(ClearScrollRequest clearScrollRequest) {
        try {
            ClearScrollResponse clearScrollResponse = httpClient.clearScroll(clearScrollRequest,RequestOptions.DEFAULT);
            return clearScrollResponse;
        } catch (IOException e) {
            logger.warn("search Failures msg=" + e.getMessage(), e);
            return null;
        }
    }


    public static Map<String, Object> get(String indexName, String docId) {
        GetRequest request = new GetRequest(indexName, docId);
        try {
            GetResponse response = httpClient.get(request, RequestOptions.DEFAULT);
            if(response.isExists())  return response.getSourceAsMap();
            else return null;
        } catch (IOException e) {
            logger.warn("get Failures indexName="+indexName+", docId="+docId+", msg=" + e.getMessage(), e);
            return null;
        }
    }

    public static String update(String indexName, String docId,Map<String, Object> map) {
        UpdateRequest request = new UpdateRequest(indexName, docId).doc(map);

        logger.info("update indexName="+indexName+", docId="+docId+", map="+map.keySet().toString());
        try {
            UpdateResponse updateResponse = httpClient.update(request, RequestOptions.DEFAULT);

            if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
                logger.warn("Update(Created) Success indexName="+indexName+", docId="+docId);
            } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                logger.warn("Update(Updated) Success indexName="+indexName+", docId="+docId);
            } else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
                logger.warn("Update(Deleted) Success indexName="+indexName+", docId="+docId);
            } else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
                logger.warn("Update(NoOp) Success indexName="+indexName+", docId="+docId);
            }

            flushIndex(indexName);
            return "Success";
        } catch (IOException e) {
            StringBuilder msg=new StringBuilder();
            logger.warn("Update Failures indexName="+indexName+", docId="+docId+", msg=" + e.getMessage(), e);
            msg.append("Fail");
            msg.append("\n");
            msg.append(e.getMessage());
            return msg.toString();
        }
    }

    public static void updateAsync(String indexName, String docId,Map<String, Object> map) {
        UpdateRequest request=new UpdateRequest(indexName,docId).upsert(map);

        httpClient.updateAsync(request,RequestOptions.DEFAULT,new ActionListener<UpdateResponse>() {
                    @Override
                    public void onResponse(UpdateResponse bulkResponse) {
                        logger.info("Update Async Success indexName="+indexName+", docId="+docId);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        logger.warn("Update Async Failures indexName="+indexName+", docId="+docId+", msg="+e.getMessage(),e);
                    }
                }
        );

    }

//    public static String clone(String sourceIndexName, String targetIndexName) {
//        StringBuilder msg=new StringBuilder();
//
//        try {
//            ResizeRequest resizeRequest = new ResizeRequest(sourceIndexName,targetIndexName);
//
//            ResizeResponse resizeResponse = httpClient.indices().clone(resizeRequest, RequestOptions.DEFAULT);
//
//            if(resizeResponse.isAcknowledged()){
//                logger.info("clone Success sourceIndexName="+sourceIndexName+", targetIndexName="+targetIndexName);
//                msg.append("Success");
//            }
//            flushIndex(targetIndexName);
//        }catch (Exception e){
//            logger.warn("clone Failures sourceIndexName="+sourceIndexName+", targetIndexName="+targetIndexName);
//            msg.append("Fail");
//            msg.append("\n");
//            msg.append(e.getMessage());
//        }
//        return msg.toString();
//
//    }

    public static String bulk(BulkRequest request, String flushIndex) {
        StringBuilder msg=new StringBuilder();

        try {
            BulkResponse bulkResponse= httpClient.bulk(request,RequestOptions.DEFAULT);
            if(bulkResponse.hasFailures()){
                logger.warn("Bulk Failures msg="+bulkResponse.buildFailureMessage());
                msg.append("Fail");
                msg.append("\n");
                msg.append(bulkResponse.buildFailureMessage());
            }else{
                logger.info("Bulk Success cnt="+bulkResponse.getItems().length);
                msg.append("Success");
            }
            if(flushIndex!=null) flushIndex(flushIndex);
        }catch (Exception e){
            logger.warn("Bulk Async Failures msg="+e.getMessage(),e);
            msg.append("Fail");
            msg.append("\n");
            msg.append(e.getMessage());
        }
        return msg.toString();
    }


    public static String bulk(BulkRequest request, String[] flishIndices) {
        StringBuilder msg=new StringBuilder();

        try {
            BulkResponse bulkResponse= httpClient.bulk(request,RequestOptions.DEFAULT);
            if(bulkResponse.hasFailures()){
                logger.warn("Bulk Failures msg="+bulkResponse.buildFailureMessage());
                msg.append("Fail");
                msg.append("\n");
                msg.append(bulkResponse.buildFailureMessage());
            }else{
                 logger.info("Bulk Success cnt="+bulkResponse.getItems().length);
                  msg.append("Success");
            }
            if(flishIndices!=null) flushIndex(flishIndices);
        }catch (Exception e){
            logger.warn("Bulk Async Failures msg="+e.getMessage(),e);
            msg.append("Fail");
            msg.append("\n");
            msg.append(e.getMessage());
        }
        return msg.toString();
    }


    public static void bulkAsync(BulkRequest request) {
        httpClient.bulkAsync(request,RequestOptions.DEFAULT,new ActionListener<BulkResponse>() {
                    @Override
                    public void onResponse(BulkResponse bulkResponse) {
                        if(bulkResponse.hasFailures()){
                            logger.warn("Bulk Async Failures msg="+bulkResponse.buildFailureMessage());
                        }else{
//                            logger.info("Bulk Async Success cnt="+bulkResponse.getItems().length);
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        logger.warn("Bulk Async Failures msg="+e.getMessage(),e);
                    }
                }
        );
    }

    public static void flushIndex(String index) {
        String[] str={index};
        flushIndex(str);
    }

    public static void flushIndex(String[] indices) {
        try{
            FlushRequest request = new FlushRequest(indices);
            FlushResponse response = httpClient.indices().flush(request, RequestOptions.DEFAULT);

            for (ShardOperationFailedException failure : response.getShardFailures()) {
                logger.warn("unexpected flush failure " + failure.reason());
            }
            StringBuilder sb=new StringBuilder();
            for(String s:indices) sb.append(" ").append(s);
            logger.info("flushIndex indexName ="+ sb.toString());
        }catch (Exception e){
            logger.warn(e.getMessage(),e);
        }
    }
}
