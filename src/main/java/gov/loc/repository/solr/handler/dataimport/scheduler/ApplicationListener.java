package gov.loc.repository.solr.handler.dataimport.scheduler;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.util.IOUtils;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * ServletContentListener that constructs/destroys Quartz Scheduler.
 * 
 * The location of the quartz.properties file is set to Solr's config directory.
 * 
 */
public class ApplicationListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationListener.class);
	
	private static final String SCHEDULER = "scheduler";
	
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		Scheduler sched = (Scheduler)servletContextEvent.getServletContext().getAttribute(SCHEDULER);
		if (sched != null) {
			try {
				sched.shutdown();
				logger.info("Shut down scheduler");
			} catch (SchedulerException e) {
				logger.error(MessageFormat.format("Error shutting down scheduler: {0}",  e.getMessage(), e));
			}
		}
	}

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		StdSchedulerFactory sf = new StdSchedulerFactory();
		try {
			logger.debug("Initializing scheduler factory");
			sf.initialize(loadQuartzProperties());
		} catch (IOException e) {
			logger.error(MessageFormat.format("Error loading quartz.properties: {0}",  e.getMessage(), e));			
		} catch (SchedulerException e) {
			logger.error(MessageFormat.format("Error initializing scheduler factory: {0}",  e.getMessage(), e));
		}

		try {
			Scheduler sched = sf.getScheduler();
			//Start it
			sched.start();
			
			//Save in servletContext
			servletContextEvent.getServletContext().setAttribute(SCHEDULER, sched);
			
			logger.info("Started up scheduler");

		} catch (SchedulerException e) {
			logger.error(MessageFormat.format("Error creating scheduler: {0}",  e.getMessage(), e));
		}
	}

	private Properties loadQuartzProperties() throws IOException {
		SolrResourceLoader resourceLoader = new SolrResourceLoader(null);
		try {
			Properties quartzProperties = new Properties();
			quartzProperties.load(resourceLoader.openConfig("quartz.properties"));
			return quartzProperties;
		} finally {
			IOUtils.closeQuietly(resourceLoader);
		}
		
	}
}
