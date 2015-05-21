package com.helicaltech.pcni.scheduling;

import com.helicaltech.pcni.exceptions.ConfigurationException;
//import com.helicaltech.pcni.useractions.DeleteOperationHandler;
import org.apache.log4j.Logger;
import org.quartz.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * responsible for creating new job.call <code>Scheduler</code>
 * <code>Trigger</code>
 * </p>
 *
 * @author PK
 * @version 1.1
 */
public class ScheduleProcess {
	private static final Logger logger = Logger.getLogger(ScheduleProcess.class);

	/**
	 * <p>
	 * In this method we map data into Datamap
	 * </p>
	 *
	 * @param cronexpression
	 *            a <code>String</code> specify cron expression
	 * @param className
	 *            a <code>String</code> specify class name which perform job or
	 *            class which overridde execute method
	 * @param jobName
	 *            a <code>String</code> specify the unique name of job
	 * @param jobGroup
	 *            a <code>String</code> specify group of job
	 * @param path
	 *            a <code>String</code> specify Scheduling.xml path
	 * @param jsonobject
	 *            a <code>JSONObject</code> specify details contain in schedule
	 *            tag
	 * @param baseUrl
	 *            a <Code>String</code> specify base URL like:
	 *            http://localhost:9090/Example/test.html
	 * @see ScheduleProcessCall
	 */
	@SuppressWarnings("unchecked")
	public net.sf.json.JSONObject scheduleJob(String cronexpression, ISchedule className, String jobName, String jobGroup, String path,
			net.sf.json.JSONObject jsonobject, String baseUrl) {
		logger.debug("Inside schedule job");
		logger.debug("jsonobject:  " + jsonobject);
		logger.debug("Cron Expression: " + cronexpression);
		Scheduler sch = SchedulerUtility.getInstance();
		String startDate = jsonobject.getJSONObject("ScheduleOptions").getString("StartDate");

		logger.debug("startDate:  " + startDate);
		DateFormat formatter = null;
		Date sDate = null;
		Date eDate = null;
		String ScheduledTime = "";
		if (jsonobject.getJSONObject("ScheduleOptions").containsKey("ScheduledTime")) {
			ScheduledTime = jsonobject.getJSONObject("ScheduleOptions").getString("ScheduledTime");
			logger.debug("startDate:  " + startDate);
			startDate = startDate + " " + ScheduledTime;
		}

		try {
			formatter = new SimpleDateFormat("yyyy-MM-dd k:m:s");

			sDate = (Date) formatter.parse(startDate);

			logger.debug("sDate: " + sDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String endDate = "";
		if (jsonobject.getJSONObject("ScheduleOptions").getString("endsRadio").equalsIgnoreCase("on")) {
			endDate = jsonobject.getJSONObject("ScheduleOptions").getString("EndDate");
			endDate = endDate + " " + ScheduledTime;
			logger.debug("endDate: " + endDate);

			try {
				formatter = new SimpleDateFormat("yyyy-MM-dd k:m:s");
				eDate = (Date) formatter.parse(endDate);
				logger.debug("eDate:  " + eDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		TriggerUtility tr = new TriggerUtility();
		Trigger trigger1 = tr.getInstance(cronexpression, jobName, sDate, eDate);
		net.sf.json.JSONObject jobdata = new net.sf.json.JSONObject();

		try {
			if (!sch.checkExists(JobKey.jobKey(jobGroup + "." + jobName))) {
				JobDetail job = JobBuilder.newJob((Class<? extends Job>) className.getClass()).withIdentity(jobName, jobGroup).build();
				logger.debug("sch.getJobGroupNames()" + sch.getJobGroupNames());
				job.getJobDataMap().put("jobinput", jobName);
				job.getJobDataMap().put("path", path);
				job.getJobDataMap().put("baseUrl", baseUrl);
				job.getJobDataMap().put("jsonobject", jsonobject);
				sch.start();

				sch.scheduleJob(job, trigger1);
			} else if (sch.checkExists(JobKey.jobKey(jobGroup + "." + jobName))) {
				logger.debug("JOB EXIST");
			} else {
				try {
					throw new ConfigurationException("JOB is already executing");
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}
		} catch (SchedulerException e) {
			e.printStackTrace();

		}
		return jobdata;
	}

	public String stopJob(Scheduler sch) {
		try {
			sch.shutdown();
		} catch (SchedulerException e) {

			e.printStackTrace();
		}

		return "jobStoped";
	}

	/**
	 * delete() is responsible for delete job from scheduler
	 *
	 * @param jobkey
	 *            a <code>String</code> which specify the job key which has to
	 *            be delete.
	 * @see DeleteOperationHandler ,EFWController, XmlOperationWithParser
	 */
	public String delete(String jobkey) {
		try {
			logger.debug("Delet job from schedule ,Schedule id: " + jobkey);
			SchedulerUtility.getInstance().deleteJob(JobKey.jobKey(jobkey, "DEFAULT"));
		} catch (SchedulerException e) {

			e.printStackTrace();
		}
		return "jobdeleted";
	}

	public List<JobExecutionContext> listOfExecutingJob() {
		List<JobExecutionContext> listOfJobs = null;
		try {
			listOfJobs = SchedulerUtility.getInstance().getCurrentlyExecutingJobs();

		} catch (SchedulerException e) {

			e.printStackTrace();
		}
		return listOfJobs;
	}

	public String listOfCurrentlyExecutingJob() {

		JobKey key = JobKey.jobKey("1", "DEFAULT");

		try {
			logger.debug("Found job identified by: " + SchedulerUtility.getInstance().getJobDetail(key).isConcurrentExectionDisallowed());
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

		return "ok";
	}

	public JobDetail jobDetail() {
		JobDetail jobdetail = null;
		try {
			jobdetail = SchedulerUtility.getInstance().getJobDetail(JobKey.jobKey("1", "DEFAULT"));
		} catch (SchedulerException e) {

			e.printStackTrace();
		}
		return jobdetail;
	}

	public String pauseJob(String jobkey) {
		try {
			// scheduler.pauseJob(jobkey);
			SchedulerUtility.getInstance().pauseJob(JobKey.jobKey(jobkey, "DEFAULT"));
		} catch (SchedulerException e) {

			e.printStackTrace();
		}
		return "pausejob";
	}

	public String pauseAllJob(Scheduler scheduler) {
		try {
			scheduler.pauseAll();
		} catch (SchedulerException e) {

			e.printStackTrace();
		}
		return "pausealljob";
	}

	/**
	 * updateTriger method is responsible to update trigger
	 *
	 * @param previusCronExpression
	 *            a <code>String</code> which specify the previous
	 *            cronExpression
	 * @param newCronExpression
	 *            a <code>String</code> which specify the cronExpression which
	 *            has to be updated in trigger.
	 * @param jobName
	 *            a <code>String</code> which specify the trigger name
	 * @param sDate
	 *            a <code>String</code> which specify the start date
	 * @param eDate
	 *            a <code>String</code> which specify the end date
	 */
	@SuppressWarnings("unchecked")
	public void updateTriger(String previusCronExpression, String newCronExpression, String jobName, Date sDate, Date eDate) {
		logger.debug("Update trigger call..");
		TriggerUtility trigerUtility = new TriggerUtility();
		// Date sDate = null;
		// Date eDate = null;
		Trigger trigger = trigerUtility.getInstance(previusCronExpression, jobName, sDate, eDate);
		@SuppressWarnings("rawtypes")
		TriggerBuilder triggerBuilder = trigger.getTriggerBuilder();

		try {
			Trigger newTrigger = triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(newCronExpression)).build();
			logger.debug("trigger.getKey():" + trigger.getKey());
			SchedulerUtility.getInstance().rescheduleJob(trigger.getKey(), newTrigger);
		} catch (ParseException e) {
			logger.debug("Exception in update trigger..");
			e.printStackTrace();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}

	}
}
