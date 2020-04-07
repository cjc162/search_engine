package com.searchengine.SearchEngine;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RestUploadController {
	
    @PostMapping("/api/upload/multi")
    public ResponseEntity<?> uploadFileMulti(
            @RequestParam("files") MultipartFile[] uploadfiles) {

        String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
                .filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

        if (StringUtils.isEmpty(uploadedFileName)) {
            return new ResponseEntity("please select a file!", HttpStatus.OK);
        }

        try {

            saveUploadedFiles(Arrays.asList(uploadfiles));

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity("Successfully uploaded - " + uploadedFileName, HttpStatus.OK);

    }

    public void saveUploadedFiles(List<MultipartFile> files) throws IOException {
    	final String PROJECT_ID = "utility-vista-273402";
    	final String BUCKET_NAME = "dataproc-staging-us-west1-388522857539-gkh9vufp";
    	
    	Page<Blob> blobs = listObjectsWithPrefix(PROJECT_ID, BUCKET_NAME, "data/");
    	deleteObjects(PROJECT_ID, BUCKET_NAME, blobs);

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue;
            }

            byte[] bytes = file.getBytes();
            uploadObject(PROJECT_ID, BUCKET_NAME, "data/" + file.getOriginalFilename(), bytes);
        }

    }
    
    public static void uploadObject(String projectId, String bucketName, String objectName, byte[] bytes) throws IOException {    	  
    	  	Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    	    BlobId blobId = BlobId.of(bucketName, objectName);
    	    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    	    storage.create(blobInfo, bytes);
    }
    
    public static void deleteObjects(String projectId, String bucketName, Page<Blob> blobs) {
    	    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    	    
    	    for (Blob blob : blobs.iterateAll()) {
        	    storage.delete(bucketName, blob.getName());
    	    }
    }
    
    public static void deleteObject(String projectId, String bucketName, String name) {
	    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
	    
    	 storage.delete(bucketName, name);
    }
    
    private static Page<Blob> listObjectsWithPrefix(String projectId, String bucketName, String directoryPrefix) {
    	    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    	    Bucket bucket = storage.get(bucketName);

    	    Page<Blob> blobs = bucket.list(Storage.BlobListOption.prefix(directoryPrefix), Storage.BlobListOption.currentDirectory());
    	    
    	    return blobs;
    }
}
