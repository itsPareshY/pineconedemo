package com.example.pinecone.pineconedemo;

import io.pinecone.clients.Pinecone;
import org.openapitools.client.model.*;

public class PineconedemoApplication {

	private static final String PINECONE_API_KEY = "PINECONE_API_KEY";

	public static void main(String[] args) {
		Pinecone pc = new Pinecone.Builder(System.getenv(PINECONE_API_KEY)).build();
		pc.createServerlessIndex("docs-quickstart-index", "cosine", 8, "aws", "us-east-1");
	}

}
