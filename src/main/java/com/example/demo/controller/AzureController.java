package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.AzureBlobAdapter;
import com.microsoft.azure.storage.StorageException;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import java.net.URISyntaxException;
import java.util.List;
import org.springframework.core.io.Resource;

@RestController
@RequestMapping("/")
public class AzureController {

    @Autowired
    private AzureBlobAdapter azureBlobAdapter;
	private String file;

    @PostMapping("/container")
    public ResponseEntity createContainer(@RequestBody String containerName){
        boolean created = azureBlobAdapter.createContainer(containerName);
        return ResponseEntity.ok(created);
    }

    @PostMapping
    public ResponseEntity upload(@RequestParam MultipartFile multipartFile){
        URI url = azureBlobAdapter.upload(multipartFile);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/blobs")
    public ResponseEntity getAllBlobs(@RequestHeader(value="containerName") String containerName){
        List<URI> uris = azureBlobAdapter.listBlobs(containerName);
        return ResponseEntity.ok(uris);
    }

    @DeleteMapping
    public ResponseEntity delete(@RequestParam String containerName, @RequestParam String blobName){
        azureBlobAdapter.deleteBlob(containerName, blobName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/")
    public ResponseEntity getBlobs(@RequestHeader(value="blobName") String blobName) throws URISyntaxException, StorageException, IOException{
        azureBlobAdapter.getBlob(blobName);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/images/{blobName}")
    public ResponseEntity getImages(@PathVariable  String blobName) throws URISyntaxException, StorageException, IOException{
        Resource file = azureBlobAdapter.download(blobName);
        return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);	
    }
    
    
    
    

}