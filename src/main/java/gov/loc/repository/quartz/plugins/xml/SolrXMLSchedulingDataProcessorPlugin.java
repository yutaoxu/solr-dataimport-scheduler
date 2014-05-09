package gov.loc.repository.quartz.plugins.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.util.IOUtils;
import org.quartz.plugins.xml.XMLSchedulingDataProcessorPlugin;

public class SolrXMLSchedulingDataProcessorPlugin extends
		XMLSchedulingDataProcessorPlugin {

	/*
	 * Overrides XMLSchedulingDataProcessorPlugin to set the base path of
	 * filenames to Solr config directory.
	 * 
	 * @see org.quartz.plugins.xml.XMLSchedulingDataProcessorPlugin#setFileNames(java.lang.String)
	 */
	@Override
	public void setFileNames(String fileNames) {
		if (fileNames == null) {
			super.setFileNames(fileNames);
			return;
		}
		
		//Get Solr's config directory from the SolrResourceLoader
		SolrResourceLoader resourceLoader = new SolrResourceLoader(null);
		String configDir;
		try {
			configDir = SolrResourceLoader.normalizeDir(resourceLoader.getConfigDir());
		} finally {
			IOUtils.closeQuietly(resourceLoader);
		}
		
		//Add the config directory to the filenames
        List<String> fullFileNameList = new ArrayList<String>();
        for(String fileName : fileNames.split(", *")) {
            fullFileNameList.add(configDir + fileName);        	
        }
        
        //Re-concatenate
        String fullFileNames = StringUtils.join(fullFileNameList, ",");
		super.setFileNames(fullFileNames);
	}
	
}
