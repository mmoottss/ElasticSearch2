package com.pcu.dao;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EsDAO {

	public static Map<String, Long> getEsStat() throws IOException {
		String indexName = "jshj";
		Map<String, Long> m = new HashMap<String, Long>();

		/*
		 * String hostname = "192.168.0.171"; int port = 9201;
		 * 
		 * final CredentialsProvider credentialsProvider = new
		 * BasicCredentialsProvider();
		 * credentialsProvider.setCredentials(AuthScope.ANY,new
		 * UsernamePasswordCredentials("elastic", "elevisor")); RestHighLevelClient
		 * client = new RestHighLevelClient(RestClient.builder(new
		 * HttpHost(hostname,port)). setHttpClientConfigCallback(new
		 * RestClientBuilder.HttpClientConfigCallback() { public HttpAsyncClientBuilder
		 * customizeHttpClient( HttpAsyncClientBuilder httpClientBuilder) { return
		 * httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider); } }));
		 */

		RestHighLevelClient client = ElasticSearchClient.getHttpClient();

		RequestOptions opts = RequestOptions.DEFAULT;

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.size(0).sort("_doc", SortOrder.ASC);

		TermsAggregationBuilder termsaggbuilder = AggregationBuilders.terms("test").field("city").size(12);
		sourceBuilder.aggregation(termsaggbuilder);

		SearchRequest searchRequest = new SearchRequest("bank-data-2021");
		searchRequest.searchType(SearchType.DFS_QUERY_THEN_FETCH).source(sourceBuilder);

		SearchResponse searchresponse = client.search(searchRequest, opts);
		Aggregations aggs = searchresponse.getAggregations();
		Terms parseterm = aggs.get("test");

		for (Terms.Bucket bucket : parseterm.getBuckets()) {
			m.put((String) bucket.getKey(), bucket.getDocCount());
		}
		return m;
	}
}
