package com.searchengine.SearchEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
		if (term != null && !term.isBlank()) {	
			long startTime = System.currentTimeMillis();
			HashMap<String, String> map = searchForTerm(term);
			long elapsed = System.currentTimeMillis() - startTime;
			
			model.addAttribute("term", "Results for term: " + term + " (Executed in " + elapsed + " ms)");
			model.addAttribute("map", map);
		}
		
		return "actions.html";
	}
	
	public static HashMap<String, String> searchForTerm(String term) throws FileNotFoundException {
		RestUploadController.downloadOutput();
		
		File f = new File("output.txt");
		Scanner sc = new Scanner(f);
		
		HashMap<String, String> map = new HashMap<String, String>();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] line_parts = line.split("\t");
			
			if (line_parts.length != 2) {
				continue;
			}
			
			if (line_parts[0].equals(term)) {
				String dict = line_parts[1].substring(1, line_parts[1].length()-1);		
				String[] dict_parts = dict.split(", ");
				
				for (String str : dict_parts) {
					String[] file_frequency = str.split("=");
					
					if (file_frequency.length != 2) {
						continue;
					}
					
					map.put(file_frequency[0], file_frequency[1]);
				}
			}
		}
		
		sc.close();
		f.delete();
		
		return map;
	}
}
