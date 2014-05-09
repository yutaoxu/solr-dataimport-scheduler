package gov.loc.repository.quartz;


import java.text.MessageFormat;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;

/**
 * Quartz Job that invokes Solr DataImport using a POST.
 * 
 * The following job data map pairs are used:
 * * solrUrl: The url of the solr webapp (default is http://localhost:8080/solr)
 * * command: full-import or delta-import (default is full-import)
 * * clean: true or false (default is true)
 * * commit: true or false (default is true)
 * * optimize: true or false (default is false) 
 * * cores: comma-separated list of cores (defaults to the default core)
 */
public class DataImportJob implements Job {
	
	private static final Logger logger = LoggerFactory.getLogger(DataImportJob.class);

	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		//Get values from job data map
		JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();		
		String solrUrl = getString(jobDataMap, "solrUrl", "http://localhost:8080/solr");
		String command = getString(jobDataMap, "command", "full-import");
		String clean = getString(jobDataMap, "clean", "true");
		String commit = getString(jobDataMap, "commit", "true");
		String optimize = getString(jobDataMap, "optimize", "false");
		
		String cores = jobDataMap.getString("cores");
		if (cores == null) {
			//Default core
			post(solrUrl, null, command, clean, commit, optimize);
		} else {
			for(String core : cores.split(", *")) {
				post(solrUrl, command, core, clean, commit, optimize);				
			}
		}
	}

	private void post(
			String solrUrl, 
			String command, 
			String core, 
			String clean, 
			String commit, 
			String optimize) throws JobExecutionException {
		//Construct the url
		String completeSolrUrl = solrUrl
				+ (core != null ? "/" + core : "")
				+ "/dataimport";
		
		//Create a description of the POST used for logging
		String postDescr = MessageFormat.format("POST to {0} with command={1}, clean={2},  commit={3}, and optimize={4}",
				completeSolrUrl,
				command,
				clean,
				commit,
				optimize);
		
		//Do the post
		String responseContent = null;
		Integer statusCode = null;
		try {
			logger.debug("Performing " + postDescr);
			Response response = Request.Post(completeSolrUrl)
				.bodyForm(Form.form()
					.add("command", command)
					.add("clean", clean)
					.add("commit", commit)
					.add("optimize", optimize)
					.build())
				.execute();
			HttpResponse httpResponse = response.returnResponse();
			responseContent = EntityUtils.toString(httpResponse.getEntity());
			statusCode = httpResponse.getStatusLine().getStatusCode();
		} catch (Exception e) {
			String msg = MessageFormat.format("Error performing {0}: {1}", postDescr, e.getMessage());
			logger.error(msg, e);
			throw new JobExecutionException(msg, e, false);
		}
		//200 = success
		if (statusCode != 200) {
			//An error has occurred
			logger.error(MessageFormat.format("{0} returned {1,number,#}: {2}", 
				postDescr, 
				statusCode,
				responseContent));
			throw new JobExecutionException(
				MessageFormat.format("{0} returned {1,number,#}: {2}", postDescr, statusCode),
				false);
		} else {
			logger.debug(MessageFormat.format("{0} successful: {1}", 
					postDescr,
					responseContent));
		}
	}
	
	private String getString(JobDataMap jobDataMap, String key, String defaultString) {
		String value = jobDataMap.getString(key);
		if (value != null) {
			return value;
		}
		return defaultString;
	}
}
