package org.mifosplatform.billing.processscheduledjobs.service;

import java.util.List;

import org.mifosplatform.billing.scheduledjobs.data.JobParameterData;
import org.mifosplatform.billing.scheduledjobs.data.ScheduleJobData;

public interface SheduleJobReadPlatformService {

	//List<ScheduleJobData> retrieveSheduleJobDetails();

	List<Long> getClientIds(String query);

	Long getMessageId(String processParam);



	List<ScheduleJobData> retrieveSheduleJobDetails(String paramValue);

	JobParameterData getJobParameters(String jobName);

}
