package com.helicaltech.pcni.scheduling;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Create instance of SchedulerFactory
 *
 * @author Prashansa
 * @version 1.1
 * @see ScheduleProcess
 */
public class SchedulerUtility {
	private static Scheduler scheduler = null;
	private static SchedulerFactory schedulerFactory = new StdSchedulerFactory();

	public static Scheduler getInstance() {

		if (scheduler == null) {
			try {
				scheduler = schedulerFactory.getScheduler();
				return scheduler;
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
		}
		return scheduler;
	}
}
