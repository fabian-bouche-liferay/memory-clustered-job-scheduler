package com.liferay.samples.fbo.memory.clustered.scheduler.configuration;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

@ExtendedObjectClassDefinition(category = "infrastructure")
@Meta.OCD(
	id = "com.liferay.samples.fbo.memory.clustered.scheduler.configuration.MemoryClusteredJobsSchedulerConfiguration",
	localization = "content/Language", name = "memory-clustered-jobs-scheduler-configuration-name"
)
public interface MemoryClusteredJobsSchedulerConfiguration {

	@Meta.AD(
			deflt = "false", description = "memory-clustered-jobs-scheduler-auto-trigger-enabled-description",
			name = "memory-clustered-jobs-scheduler-auto-trigger-enabled", required = false
		)
	public boolean memoryClusteredJobsSchedulerAutoTriggerEnabled();

	@Meta.AD(
			deflt = "",
			description = "black-listed-memory-clustered-jobs-description",
			name = "black-listed-memory-clustered-jobs", required = false
	)
	public String[] blackListedMemoryClusteredJobs();
	
}
