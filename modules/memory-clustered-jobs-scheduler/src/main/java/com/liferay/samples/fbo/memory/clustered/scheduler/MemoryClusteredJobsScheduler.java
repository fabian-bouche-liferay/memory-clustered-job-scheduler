package com.liferay.samples.fbo.memory.clustered.scheduler;

import com.liferay.dispatch.executor.DispatchTaskClusterMode;
import com.liferay.dispatch.model.DispatchTrigger;
import com.liferay.dispatch.service.DispatchTriggerLocalService;
import com.liferay.dispatch.service.DispatchTriggerLocalServiceUtil;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle;
import com.liferay.samples.fbo.memory.clustered.scheduler.configuration.MemoryClusteredJobsSchedulerConfiguration;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author fabian-liferay
 */
@Component(
		immediate = true,
		configurationPid = "com.liferay.samples.fbo.memory.clustered.scheduler.configuration.MemoryClusteredJobsSchedulerConfiguration",
		service = MemoryClusteredJobsScheduler.class
		)
public class MemoryClusteredJobsScheduler {

	@Activate
	@Modified
	public void activate(Map<String, Object> properties) {
		_memoryClusteredJobsSchedulerConfiguration = ConfigurableUtil.createConfigurable(
				MemoryClusteredJobsSchedulerConfiguration.class, properties);
		
		if(_memoryClusteredJobsSchedulerConfiguration.memoryClusteredJobsSchedulerAutoTriggerEnabled()) {
			
			
			Set<String> blackListedJobsSet = new HashSet<>(
					Arrays.asList(
							_memoryClusteredJobsSchedulerConfiguration.blackListedMemoryClusteredJobs()));
			
			_dispatchTriggerLocalService.getDispatchTriggers(
					true,
					DispatchTaskClusterMode.SINGLE_NODE_MEMORY_CLUSTERED
					)
			.stream().filter(trigger -> !blackListedJobsSet.contains(trigger.getName()))
			.forEach( trigger -> scheduleJob(trigger));
		}
	}
	
	protected void scheduleJob(DispatchTrigger trigger) {
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTime(trigger.getStartDate());
		int startDateMinute = calendar.get(Calendar.MINUTE);
		int startDateHour = calendar.get(Calendar.HOUR);
		int startDateDay = calendar.get(Calendar.DAY_OF_MONTH);
		int startDateMonth = calendar.get(Calendar.MONTH);
		int startDateYear = calendar.get(Calendar.YEAR);

		int endDateMinute = 0;
		int endDateHour = 0;
		int endDateDay = 0;
		int endDateMonth = 0;
		int endDateYear = 0;
		boolean neverEnd = true;

		if(trigger.getEndDate() != null) {
			calendar.setTime(trigger.getEndDate());
			endDateMinute = calendar.get(Calendar.MINUTE);
			endDateHour = calendar.get(Calendar.HOUR);
			endDateDay = calendar.get(Calendar.DAY_OF_MONTH);
			endDateMonth = calendar.get(Calendar.MONTH);
			endDateYear = calendar.get(Calendar.YEAR);
			neverEnd = false;
		}

		try {
			trigger = DispatchTriggerLocalServiceUtil.updateDispatchTrigger(
					trigger.getDispatchTriggerId(), trigger.isActive(), trigger.getCronExpression(),
					DispatchTaskClusterMode.SINGLE_NODE_MEMORY_CLUSTERED, endDateMonth,
					endDateDay, endDateYear, endDateHour, endDateMinute,
					neverEnd, trigger.isOverlapAllowed(), startDateMonth,
					startDateDay, startDateYear, startDateHour,
					startDateMinute, trigger.getTimeZoneId());
		} catch (PortalException e) {
			_log.error("Failed to schedule job " + trigger.getName(), e);
		}

		_log.debug("Successfully scheduled job " + trigger.getName());
		
	}

	@Reference
	private DispatchTriggerLocalService _dispatchTriggerLocalService;
	
	@Reference(target = ModuleServiceLifecycle.PORTAL_INITIALIZED)
	private ModuleServiceLifecycle _moduleServiceLifecycle;
	
	private volatile MemoryClusteredJobsSchedulerConfiguration _memoryClusteredJobsSchedulerConfiguration;
	
	private static final Log _log = LogFactoryUtil.getLog(
			MemoryClusteredJobsScheduler.class);
}