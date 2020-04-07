package com.searchengine.SearchEngine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.dataproc.Dataproc;
import com.google.api.services.dataproc.model.HadoopJob;
import com.google.api.services.dataproc.model.Job;
import com.google.api.services.dataproc.model.JobPlacement;
import com.google.api.services.dataproc.model.JobReference;
import com.google.api.services.dataproc.model.SubmitJobRequest;
import com.google.common.collect.ImmutableList;

@Controller
public class IndexController {
	
	@RequestMapping(value="/", method= {RequestMethod.GET, RequestMethod.POST})
	public String index() {		
		return "index.html";
	}
	
	@RequestMapping(value="/actions", method= {RequestMethod.GET, RequestMethod.POST})
	public String actions(
			@RequestParam(value = "term", required = false) String term,
			Model model
	) throws IOException {		
		if (term != null && !term.isEmpty()) {
			makeJob(term);
			
			model.addAttribute("term", "Searching for term: " + term);
		}
		
		return "actions.html";
	}
	
	private static void makeJob(String term) throws IOException {
		RestUploadController.deleteObject("utility-vista-273402", "dataproc-staging-us-west1-388522857539-gkh9vufp", "output/");
		RestUploadController.deleteObject("utility-vista-273402", "dataproc-staging-us-west1-388522857539-gkh9vufp", "output.txt");
		
		Random rand = new Random(); 
		
		Collection<String> scopes = new ArrayList<String>();
		scopes.add("https://www.googleapis.com/auth/cloud-platform");
		
		GoogleCredential cred = GoogleCredential.getApplicationDefault().createScoped(scopes);
		
		Dataproc dataproc = new Dataproc.Builder(new NetHttpTransport(), new JacksonFactory(), cred)
				.build();
		
		dataproc.projects().regions().jobs().submit("utility-vista-273402", "us-west1", new SubmitJobRequest()
				.setJob(new Job()
						.setPlacement(new JobPlacement()
								.setClusterName("search-engine")
								.setClusterUuid("a7a6708e-4ff5-4dc2-bf7c-15d2e70e7ffc"))
						.setReference(new JobReference()
								.setProjectId("utility-vista-273402")
								.setJobId("search-engine-job-" + rand.nextInt(999999999)))
						.setHadoopJob(new HadoopJob()
								.setMainClass("WordCount")
								.setJarFileUris(ImmutableList.of("gs://dataproc-staging-us-west1-388522857539-gkh9vufp/JAR/WordCount.jar"))
								.setArgs(ImmutableList.of(
										"gs://dataproc-staging-us-west1-388522857539-gkh9vufp/data/", 
										"gs://dataproc-staging-us-west1-388522857539-gkh9vufp/output",
										term)))))
		.execute();
	}
}
