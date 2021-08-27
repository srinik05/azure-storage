package com.example.demo.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Component
public class AzureBlobAdapter {

	private static final Logger logger = LoggerFactory.getLogger(AzureBlobAdapter.class);

	@Autowired
	private CloudBlobClient cloudBlobClient;

	@Autowired
	private CloudBlobContainer cloudBlobContainer;

	@Autowired
	private Environment environment;

	public boolean createContainer(String containerName){
		boolean containerCreated = false;
		CloudBlobContainer container = null;
		try {
			container = cloudBlobClient.getContainerReference(containerName);
		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (StorageException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		try {
			containerCreated = container.createIfNotExists(BlobContainerPublicAccessType.CONTAINER, new BlobRequestOptions(), new OperationContext());
		} catch (StorageException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return containerCreated;
	}

	public URI upload(MultipartFile multipartFile){
		URI uri = null;
		System.out.println("Upload");
		CloudBlockBlob blob = null;
		try {
			blob = cloudBlobContainer.getBlockBlobReference(multipartFile.getOriginalFilename());
			System.out.println("Blob : "+ blob);

			blob.upload(multipartFile.getInputStream(), -1);
			uri = blob.getUri();
			System.out.println("URI : "+ uri);

		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (StorageException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return uri;
	}

	public List<URI> listBlobs(String containerName){
		System.out.println("Container Name "+containerName);

		List<URI> uris = new ArrayList<>();
		try {
			CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
			for (ListBlobItem blobItem : container.listBlobs()) {
				uris.add(blobItem.getUri());
				System.out.println("URI  1::  " +blobItem.getStorageUri());
				System.out.println("parent :: " +blobItem.getParent());
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (StorageException e) {
			e.printStackTrace();
		}
		return uris;
	}

	public void getBlob(String blobName) throws URISyntaxException, StorageException, IOException {

		CloudBlobContainer container = cloudBlobClient.getContainerReference("iids-container");
		CloudBlockBlob blob = container.getBlockBlobReference(blobName);

		blob.downloadToFile(blobName);
	}


	public String downloadFile(String blobitem) {
		System.out.println("Download BEGIN {}"+ blobitem);
		BlobContainerClient containerClient = containerClient();
		BlobClient blobClient = containerClient.getBlobClient(blobitem);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		blobClient.download(os);
		System.out.println("Download END");
		return StreamUtils.copyToString(os, Charset.defaultCharset());
	}

	private BlobContainerClient containerClient() {
		BlobServiceClient serviceClient = new BlobServiceClientBuilder()
				.connectionString(environment.getProperty("azure.storage.ConnectionString")).buildClient();
		BlobContainerClient container = serviceClient.getBlobContainerClient(environment.getProperty("azure.storage.container.name"));
		return container;
	}

	public void deleteBlob(String containerName, String blobName){
		System.out.println("Container name blobName" + containerName + blobName);
		try {
			CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
			CloudBlockBlob blobToBeDeleted = container.getBlockBlobReference(blobName);
			blobToBeDeleted.deleteIfExists();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (StorageException e) {
			e.printStackTrace();
		}
	}

	public Resource download(String blobName) throws IOException {
		/*
		 * BlobContainerClient containerClient = containerClient(); BlobClient
		 * blobClient = containerClient.getBlobClient(blobName); ByteArrayOutputStream
		 * os = new ByteArrayOutputStream(); blobClient.download(os); return new
		 * Resource().getInputStream().read(os);
		 */

		try {
			CloudBlobContainer container = cloudBlobClient.getContainerReference(blobName);
			Resource resource = new UrlResource(container.getUri());
			System.out.println(container.getUri());
			System.out.println(container.getName());
			System.out.println(container.getStorageUri());
			System.out.println("-----"+resource.getFilename()  + "-----" + resource.getDescription() + " ----"+ resource.getURL() + "---"+ resource.getFile() );
			System.out.println("--------------------------------------------------------------");
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}else {
				throw new FileNotFoundException(
						"Could not read file: " + blobName);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (StorageException e) {
			e.printStackTrace();
		}
		return null;
	
	}
}
