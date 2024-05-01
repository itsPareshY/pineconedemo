package com.example.pinecone.pineconedemo;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.DescribeIndexStatsResponse;
import org.openapitools.client.model.IndexList;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PineconedemoApplication {

	private static final String PINECONE_API_KEY = "PINECONE_API_KEY";
	public static final String DOCS_QUICKSTART_INDEX = "docs-quickstart-index";

	private static Pinecone pineconeClientInstance ;

	public static void main(String[] args) throws InterruptedException {
		Pinecone pc = getPineConeClientInstance();
		pc.createServerlessIndex(DOCS_QUICKSTART_INDEX, "cosine", 8, "aws", "us-east-1");
		System.out.println("Waiting 1 min after creating index  "+DOCS_QUICKSTART_INDEX);
        Thread.sleep(10000);

		List<Float> values1 = Arrays.asList(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f);
		List<Float> values2 = Arrays.asList(0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f);
		List<Float> values3 = Arrays.asList(0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f);
		List<Float> values4 = Arrays.asList(0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f);
		List<Float> values5 = Arrays.asList(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f);
		List<Float> values6 = Arrays.asList(0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f);
		List<Float> values7 = Arrays.asList(0.7f, 0.7f, 0.7f, 0.7f, 0.7f, 0.7f, 0.7f, 0.7f);
		List<Float> values8 = Arrays.asList(0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f);

		upsertVectorInIndex(DOCS_QUICKSTART_INDEX,"vec1", values1, "ns1");
		upsertVectorInIndex(DOCS_QUICKSTART_INDEX,"vec2", values2, "ns1");
		upsertVectorInIndex(DOCS_QUICKSTART_INDEX,"vec3", values3, "ns1");
		upsertVectorInIndex(DOCS_QUICKSTART_INDEX,"vec4", values4, "ns1");
		upsertVectorInIndex(DOCS_QUICKSTART_INDEX,"vec5", values5, "ns2");
		upsertVectorInIndex(DOCS_QUICKSTART_INDEX,"vec6", values6, "ns2");
		upsertVectorInIndex(DOCS_QUICKSTART_INDEX,"vec7", values7, "ns2");
		upsertVectorInIndex(DOCS_QUICKSTART_INDEX,"vec8", values8, "ns2");

		IndexList indexList = pc.listIndexes();
		System.out.println("Index List "+indexList.toJson()+"\n\n");
		printIndexStats(DOCS_QUICKSTART_INDEX);
		System.out.println("\nWaiting 5 min before delete index  : "+DOCS_QUICKSTART_INDEX);
		Thread.sleep(50000);

		//Index cleanup to release resources
		pc.deleteIndex(DOCS_QUICKSTART_INDEX);
	}

	private static void printIndexStats(String indexName) {
		Index index = getPineConeClientInstance().getIndexConnection(indexName);
		DescribeIndexStatsResponse indexStatsResponse = index.describeIndexStats(null);
		System.out.println("Index Stats :\n"+indexStatsResponse);
	}

	private static Pinecone getPineConeClientInstance() {
		if ((null == pineconeClientInstance)){
			String pineconeApiKey = Optional.ofNullable(System.getenv(PINECONE_API_KEY))
					.orElseThrow(
							() -> new NullPointerException("pineconeApiKey is not set in the environment"));
			pineconeClientInstance = new Pinecone.Builder(pineconeApiKey).build();
		}
		return pineconeClientInstance;
	}

	public static int upsertVectorInIndex(String indexName, String id, List<Float> vectorList, String namespace){
		Index index = getPineConeClientInstance().getIndexConnection(indexName);
		return index.upsert(id, vectorList, namespace).getUpsertedCount();
	}
}
