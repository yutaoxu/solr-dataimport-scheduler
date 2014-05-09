solr-dataimport-scheduler
=========================

A scheduler for performing solr data imports.

Based on https://code.google.com/p/solr-data-import-scheduler/, but uses:
* Quartz Job Scheduler (http://quartz-scheduler.org/)
* Apache HttpClient (http://hc.apache.org/httpcomponents-client-ga/)

It is intended to be simple, not profound.

### Installation and configuration
1. Add `solr-dataimport-scheduler-<version>.jar to classpath. (Typically, this means
adding to `WEB-INF/lib`.)
2. Add the following to `WEB-INF/web.xml`:

   ```
   <!-- Listener for dataimport-scheduler. -->
   <listener>
	   <listener-class>gov.loc.repository.solr.handler.dataimport.scheduler.ApplicationListener</listener-class>
   </listener>
   ```
3. Add `quartz.properties` to `<solr_home>/conf`. `quartz.properties` can be found in
`templates/` in `solr-dataimport-scheduler-<version>.jar` or `src/main/resources/templates`
of the project. `quartz.properties` is a standard quartz properties file as
specified at http://quartz-scheduler.org/documentation/quartz-2.x/configuration/. 
For typical usage, you should not need to modify this problem.
4. Add `quartz_schedule.xml` to `<solr_home>/conf`.  An example `quartz_schedule.xml` can
be found in `templates/` in `solr-dataimport-scheduler-<version>.jar` or `src/main/resources/templates`
of the project. `quartz_schedule.xml` defines the jobs (viz., the data import) and the
triggers (i.e., the schedule) as described at http://quartz-scheduler.org/documentation/quartz-2.x/cookbook/JobInitPlugin. 
*You must modify this file.*