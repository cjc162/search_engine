package com.searchengine.SearchEngine;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.paging.Page;
import com.google.api.services.dataproc.Dataproc;
import com.google.api.services.dataproc.model.HadoopJob;
import com.google.api.services.dataproc.model.Job;
import com.google.api.services.dataproc.model.JobPlacement;
import com.google.api.services.dataproc.model.JobReference;
import com.google.api.services.dataproc.model.SubmitJobRequest;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
public class RestUploadController {
	
	@PostMapping("/api/upload/multi")
    public ResponseEntity<?> uploadFileMulti(@RequestParam("files") MultipartFile[] uploadfiles) throws InterruptedException {

        String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename())
                .filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

        if (StringUtils.isEmpty(uploadedFileName)) {
            return new ResponseEntity<>("please select a file!", HttpStatus.OK);
        }
        
        String jobId;
        try {
            saveUploadedFiles(Arrays.asList(uploadfiles));
            jobId = makeJob();
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(jobId, HttpStatus.OK);
    }
	
	@GetMapping("/api/job")
    public ResponseEntity<?> jobDone(@RequestParam("jobId") String jobId) throws InterruptedException, IOException {
		if (getState(jobId).equals("DONE")) {
			return new ResponseEntity<>(false, HttpStatus.OK);
		} else if (getState(jobId).equals("FAILED")) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<>(true, HttpStatus.OK);
		}
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
    
    public static Page<Blob> listObjectsWithPrefix(String projectId, String bucketName, String directoryPrefix) {
    	    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    	    Bucket bucket = storage.get(bucketName);

    	    Page<Blob> blobs = bucket.list(Storage.BlobListOption.prefix(directoryPrefix), Storage.BlobListOption.currentDirectory());
    	    
    	    return blobs;
    }
    
	public static String makeJob() throws IOException, InterruptedException {
    	final String PROJECT_ID = "utility-vista-273402";
    	final String BUCKET_NAME = "dataproc-staging-us-west1-388522857539-gkh9vufp";
    	
    	Page<Blob> blobs = listObjectsWithPrefix(PROJECT_ID, BUCKET_NAME, "output/");
    	deleteObjects(PROJECT_ID, BUCKET_NAME, blobs);
		RestUploadController.deleteObject(PROJECT_ID, BUCKET_NAME, "output.txt");
		
		Random rand = new Random();
		String jobId = "search-engine-job-" + rand.nextInt(999999999);
		
		Collection<String> scopes = new ArrayList<String>();
		scopes.add("https://www.googleapis.com/auth/cloud-platform");
		
		GoogleCredential cred = GoogleCredential.getApplicationDefault().createScoped(scopes);
		
		Dataproc dataproc = new Dataproc.Builder(new NetHttpTransport(), new JacksonFactory(), cred).setApplicationName("my-webabb/1.0").build(); 
		
		Job job = dataproc.projects().regions().jobs().submit(PROJECT_ID, "us-west1", new SubmitJobRequest()
				.setJob(new Job()
						.setPlacement(new JobPlacement()
								.setClusterName("search-engine")
								.setClusterUuid("a7a6708e-4ff5-4dc2-bf7c-15d2e70e7ffc"))
						.setReference(new JobReference()
								.setProjectId(PROJECT_ID)
								.setJobId(jobId))
						.setHadoopJob(new HadoopJob()
								.setMainClass("WordCount")
								.setJarFileUris(ImmutableList.of("gs://dataproc-staging-us-west1-388522857539-gkh9vufp/JAR/WordCount.jar"))
								.setArgs(ImmutableList.of(
										"gs://dataproc-staging-us-west1-388522857539-gkh9vufp/data/", 
										"gs://dataproc-staging-us-west1-388522857539-gkh9vufp/output")))))
				.execute();
		
		return jobId;
	}
	
	public static String getState(String jobId) throws IOException {
		Collection<String> scopes = new ArrayList<String>();
		scopes.add("https://www.googleapis.com/auth/cloud-platform");
		
		GoogleCredential cred = GoogleCredential.getApplicationDefault().createScoped(scopes);
		
		Dataproc dataproc = new Dataproc.Builder(new NetHttpTransport(), new JacksonFactory(), cred).build();
		
		return dataproc.projects().regions().jobs().get("utility-vista-273402", "us-west1", jobId).execute().getStatus().getState();
	}
	
	
}
