package com.example.pinecone.pineconedemo;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.proto.UpsertResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PineconedemoApplicationTests {

    @Mock
    private Pinecone mockPinecone;
    
    @Mock
    private Index mockIndex;

    private static final String TEST_INDEX_NAME = "test-index";
    private static final String TEST_NAMESPACE = "test-ns";
    private static final List<Float> TEST_VECTOR = Arrays.asList(0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f);

    @BeforeEach
    void setUp() {
        PineconedemoApplication.resetPineconeClient();
    }

    @Test
    void testUpsertVector() {
        when(mockPinecone.getIndexConnection(any())).thenReturn(mockIndex);
        
        try (MockedStatic<PineconedemoApplication> mockedStatic = Mockito.mockStatic(PineconedemoApplication.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> PineconedemoApplication.getPineconeClientInstance(any())).thenReturn(mockPinecone);
            
            // Create a real UpsertResponse object
            UpsertResponse response = UpsertResponse.newBuilder().setUpsertedCount(1).build();
            when(mockIndex.upsert("test-id", TEST_VECTOR, TEST_NAMESPACE))
                .thenReturn(response);

            int result = PineconedemoApplication.upsertVectorInIndex(TEST_INDEX_NAME, "test-id", TEST_VECTOR, TEST_NAMESPACE);
            
            assertEquals(1, result);
            verify(mockIndex).upsert("test-id", TEST_VECTOR, TEST_NAMESPACE);
        }
    }

    @Test
    void testGetPineconeClientInstance_WithoutApiKey() {
        assertThrows(NullPointerException.class, 
            () -> PineconedemoApplication.getPineconeClientInstance(null),
            "Should throw NullPointerException when API key is not set");
    }

    @Test
    void testGetPineconeClientInstance_WithApiKey() {
        assertDoesNotThrow(() -> PineconedemoApplication.getPineconeClientInstance("test-api-key"),
            "Should not throw exception when API key is set");
    }
}
