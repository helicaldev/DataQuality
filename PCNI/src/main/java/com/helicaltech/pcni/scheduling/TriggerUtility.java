package com.helicaltech.pcni.scheduling;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.text.ParseException;
import java.util.Date;

/**
 * <p>
 * This class is responsible to create instance for Trigger
 * </p>
 *
 * @author Prashansa
 * @see ScheduleJob
 * @see UpdateTrigger
 */
public class TriggerUtility {
	private static final Logger logger = Logger.getLogger(TriggerUtility.class);
	private Trigger trigger = null;

	/**
	 * <p>
	 * This method is responsible for creating Trigger instance on the basis of
	 * cronExplession Start date and EndDate.
	 * </p>
	 *
	 * @param cronExpression
	 *            contains <code>String</code> of cron expression
	 * @param jobName
	 *            contains <code>String</code> use as a trigger group
	 * @param startDate
	 *            contains <code>String</code> of start date
	 * @param endDate
	 *            contains <code>String</code> of end date expression
	 *            <p>
	 *            if endDate contains null or "" then trigger will not consider
	 *            endDate it will end according to cron expression
	 *            </p>
	 * @return Trigger
	 */
	public Trigger getInstance(String cronExpression, String jobName, Date startDate, Date endDate) {

		if (trigger == null) {
			try {
				if (endDate != null) {
					logger.debug("startDate: " + startDate);
					logger.debug("endDate" + endDate);
					trigger = TriggerBuilder.newTrigger().withIdentity(jobName, "DEFAULT").startAt(startDate)
							.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing()).endAt(endDate)
							.build();
				} else {
					logger.debug("End date dose not exist");
					trigger = TriggerBuilder.newTrigger().withIdentity(jobName, "DEFAULT").startAt(startDate)
							.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing()).build();
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return trigger;
		}
		return trigger;
	}
}
