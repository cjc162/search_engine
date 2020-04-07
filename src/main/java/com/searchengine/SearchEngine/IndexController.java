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
			model.addAttribute("term", "Searching for term: " + term);
		}
		
		return "actions.html";
	}
}
