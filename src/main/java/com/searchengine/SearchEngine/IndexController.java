package com.searchengine.SearchEngine;

import java.io.IOException;

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
		if (term != null && !term.isEmpty()) {			
			model.addAttribute("term", "Searching for term: " + term);
		}
		
		return "actions.html";
	}
}
