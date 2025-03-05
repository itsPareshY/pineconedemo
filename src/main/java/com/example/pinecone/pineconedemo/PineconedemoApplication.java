package com.example.pinecone.pineconedemo;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.*;
import org.openapitools.client.model.IndexList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PineconedemoApplication {

    public static final String PINECONE_API_KEY = "PINECONE_API_KEY";
    public static final String DOCS_QUICKSTART_INDEX = "docs-quickstart-index";
    private static final int VECTOR_DIMENSION = 8;
    private static final String NAMESPACE_1 = "ns1";
    private static final String NAMESPACE_2 = "ns2";

    private static Pinecone pineconeClientInstance;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Pinecone Demo Application...");
        Pinecone pc = getPineconeClientInstance();

        try {
            // 1. Index Management
            demoIndexManagement(pc);

            // 2. Vector Operations
            demoVectorOperations(pc);

            // 3. Querying
            demoQuerying(pc);

            // 4. Index Statistics
            demoIndexStats(pc);

        } catch (Exception e) {
            System.err.println("Error in demo: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            System.out.println("\nCleaning up...");
            pc.deleteIndex(DOCS_QUICKSTART_INDEX);
            System.out.println("Demo completed!");
        }
    }

    private static void demoIndexManagement(Pinecone pc) throws InterruptedException {
        System.out.println("\n=== Index Management Demo ===");
        
        // List existing indexes
        System.out.println("Listing existing indexes...");
        IndexList indexList = pc.listIndexes();
        System.out.println("Current indexes: " + indexList.toJson());

        // Create a new serverless index
        System.out.println("\nCreating new serverless index...");
        pc.createServerlessIndex(DOCS_QUICKSTART_INDEX, "cosine", VECTOR_DIMENSION, "aws", "us-east-1");
        System.out.println("Waiting for index to be ready...");
        Thread.sleep(20000); // Wait for index to be ready

        // Describe index
        System.out.println("\nDescribing index...");
        var indexDescription = pc.describeIndex(DOCS_QUICKSTART_INDEX);
        System.out.println("Index description: " + indexDescription.toJson());
    }

    private static void demoVectorOperations(Pinecone pc) {
        System.out.println("\n=== Vector Operations Demo ===");
        Index index = pc.getIndexConnection(DOCS_QUICKSTART_INDEX);

        // Generate sample vectors
        List<List<Float>> vectors = generateSampleVectors(5);
        Map<String, Map<String, String>> metadata = generateSampleMetadata(5);

        // Upsert vectors with metadata
        System.out.println("\nUpserting vectors with metadata...");
        for (int i = 0; i < vectors.size(); i++) {
            String vectorId = "vec" + (i + 1);
            String namespace = i < 3 ? NAMESPACE_1 : NAMESPACE_2;
            
            int upsertedCount = index.upsert(vectorId, vectors.get(i), metadata.get(vectorId), namespace).getUpsertedCount();
            System.out.println("Upserted vector " + vectorId + " to namespace " + namespace + ": " + upsertedCount + " vectors");
        }

        // Fetch vectors
        System.out.println("\nFetching vectors...");
        List<String> ids = Arrays.asList("vec1", "vec2");
        var fetchResponse = index.fetch(ids, NAMESPACE_1);
        System.out.println("Fetched vectors: " + fetchResponse.getVectorsCount());

        // Update vectors
        System.out.println("\nUpdating vector...");
        List<Float> updatedVector = Collections.nCopies(VECTOR_DIMENSION, 0.9f);
        index.update("vec1", updatedVector, NAMESPACE_1);
        System.out.println("Updated vector vec1");

        // Delete vectors
        System.out.println("\nDeleting vectors...");
        index.delete(ids, NAMESPACE_1);
        System.out.println("Deleted vectors: " + ids);
    }

    private static void demoQuerying(Pinecone pc) {
        System.out.println("\n=== Querying Demo ===");
        Index index = pc.getIndexConnection(DOCS_QUICKSTART_INDEX);

        // Prepare query vector
        List<Float> queryVector = Collections.nCopies(VECTOR_DIMENSION, 0.5f);

        // Query with various parameters
        System.out.println("\nPerforming vector similarity search...");
        QueryResponse response = index.query(queryVector, NAMESPACE_1, 3, true, null);
        
        System.out.println("Query results:");
        response.getMatchesList().forEach(match -> {
            System.out.println("ID: " + match.getId() + 
                             ", Score: " + match.getScore() +
                             ", Metadata: " + match.getMetadata());
        });
    }

    private static void demoIndexStats(Pinecone pc) {
        System.out.println("\n=== Index Statistics Demo ===");
        Index index = pc.getIndexConnection(DOCS_QUICKSTART_INDEX);

        // Get index statistics
        DescribeIndexStatsResponse statsResponse = index.describeIndexStats(null);
        System.out.println("\nIndex statistics:");
        System.out.println("Dimension: " + statsResponse.getDimension());
        System.out.println("Total vector count: " + statsResponse.getTotalVectorCount());
        System.out.println("Namespaces: " + statsResponse.getNamespacesMap());
    }

    private static List<List<Float>> generateSampleVectors(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    float value = (i + 1) * 0.1f;
                    return Collections.nCopies(VECTOR_DIMENSION, value);
                })
                .collect(Collectors.toList());
    }

    private static Map<String, Map<String, String>> generateSampleMetadata(int count) {
        Map<String, Map<String, String>> metadata = new HashMap<>();
        for (int i = 0; i < count; i++) {
            String vectorId = "vec" + (i + 1);
            Map<String, String> vectorMetadata = new HashMap<>();
            vectorMetadata.put("category", "category-" + (i + 1));
            vectorMetadata.put("description", "Sample vector " + (i + 1));
            metadata.put(vectorId, vectorMetadata);
        }
        return metadata;
    }

    // For testing purposes
    static void resetPineconeClient() {
        pineconeClientInstance = null;
    }

    public static Pinecone getPineconeClientInstance() {
        return getPineconeClientInstance(System.getenv(PINECONE_API_KEY));
    }

    // For testing purposes
    static Pinecone getPineconeClientInstance(String apiKey) {
        if (pineconeClientInstance == null) {
            String pineconeApiKey = Optional.ofNullable(apiKey)
                    .orElseThrow(
                            () -> new NullPointerException("pineconeApiKey is not set in the environment"));
            pineconeClientInstance = new Pinecone.Builder(pineconeApiKey).build();
        }
        return pineconeClientInstance;
    }

    public static int upsertVectorInIndex(String indexName, String id, List<Float> vectorList, String namespace) {
        Index index = getPineconeClientInstance().getIndexConnection(indexName);
        return index.upsert(id, vectorList, namespace).getUpsertedCount();
    }
}
