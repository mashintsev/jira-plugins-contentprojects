package ru.mail.jira.plugins.contentprojects.extras;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.config.LocaleManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.query.Query;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.config.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.HttpSender;
import ru.mail.jira.plugins.commons.RestExecutor;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import ru.mail.jira.plugins.contentprojects.configuration.PluginData;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/createNews")
@Produces({MediaType.APPLICATION_JSON})
public class NewsScheduler implements LifecycleAware, DisposableBean {
    private static final int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final JobRunnerKey JOB_RUNNER_KEY = JobRunnerKey.of(NewsScheduler.class.getName());
    private static final JobId JOB_ID = JobId.of(NewsScheduler.class.getName());
    private static final Logger log = Logger.getLogger(NewsScheduler.class);

    private final ApplicationProperties applicationProperties;
    private final IssueService issueService;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final LocaleManager localeManager;
    private final PluginData pluginData;
    private final ProjectManager projectManager;
    private final SchedulerService schedulerService;
    private final SearchProvider searchProvider;
    private final UserManager userManager;

    public NewsScheduler(ApplicationProperties applicationProperties, IssueService issueService, JiraAuthenticationContext jiraAuthenticationContext, LocaleManager localeManager, PluginData pluginData, ProjectManager projectManager, SchedulerService schedulerService, SearchProvider searchProvider, UserManager userManager) {
        this.applicationProperties = applicationProperties;
        this.issueService = issueService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.localeManager = localeManager;
        this.pluginData = pluginData;
        this.projectManager = projectManager;
        this.schedulerService = schedulerService;
        this.searchProvider = searchProvider;
        this.userManager = userManager;
    }

    @Override
    public void onStart() {
        try {
            schedulerService.registerJobRunner(JOB_RUNNER_KEY, new JobRunner() {
                @Nullable
                @Override
                public JobRunnerResponse runJob(JobRunnerRequest jobRunnerRequest) {
                    try {
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.DATE, -7);
                        createNewsIssues(Consts.PROJECT_IDS, calendar.getTime());
                        return JobRunnerResponse.success();
                    } catch (Exception e) {
                        log.error(e);
                        return JobRunnerResponse.failed(e);
                    }
                }
            });

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 5);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            JobConfig jobConfig = JobConfig.forJobRunnerKey(JOB_RUNNER_KEY)
                    .withSchedule(Schedule.forInterval(MILLIS_IN_DAY, calendar.getTime()))
                    .withRunMode(RunMode.RUN_ONCE_PER_CLUSTER);
            schedulerService.scheduleJob(JOB_ID, jobConfig);
        } catch (Exception e) {
            log.error(e);
        }
    }

    @Override
    public void destroy() throws Exception {
        schedulerService.unscheduleJob(JOB_ID);
        schedulerService.unregisterJobRunner(JOB_RUNNER_KEY);
    }

    private boolean newsExist(Collection<Long> projectIds, Date date, ApplicationUser user) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date endDate = calendar.getTime();

        Query query = JqlQueryBuilder.newClauseBuilder()
                .project(projectIds.toArray(new Long[projectIds.size()]))
                .and().issueType(String.valueOf(Consts.NEWS_ISSUE_TYPE_ID))
                .and().customField(Consts.PUBLISHING_DATE_CF_ID).gtEq(startDate)
                .and().customField(Consts.PUBLISHING_DATE_CF_ID).lt(endDate)
                .buildQuery();
        return searchProvider.searchCount(query, user) > 0;
    }

    private void createNewsIssues(Collection<Long> projectIds, Date date) throws Exception {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();

        ApplicationUser user = userManager.getUserByName(Consts.NEWS_USER_NAME);
        if (user == null)
            throw new IllegalStateException(String.format("User %s is not found", Consts.NEWS_USER_NAME));

        String jiraDatePattern = applicationProperties.getDefaultBackedString(APKeys.JIRA_DATE_TIME_PICKER_JAVA_FORMAT);
        DateFormat jiraDateFormat = new SimpleDateFormat(jiraDatePattern, localeManager.getLocaleFor(user));

        ApplicationUser savedUser = jiraAuthenticationContext.getUser();
        try {
            jiraAuthenticationContext.setLoggedInUser(user);

            if (newsExist(projectIds, date, user))
                throw new Exception("News issues for the date already exist");

            Collection<IssueService.CreateValidationResult> createValidationResults = new ArrayList<IssueService.CreateValidationResult>();
            for (Long projectId : projectIds) {
                Project project = projectManager.getProjectObj(projectId);

                String newsApiUrl = pluginData.getNewsApiUrl(project);
                if (StringUtils.isEmpty(newsApiUrl))
                    continue;

                String response = new HttpSender(newsApiUrl, new SimpleDateFormat(DATE_FORMAT).format(date)).sendGet();
                JSONObject json = new JSONObject(response);
                JSONArray elements = json.getJSONArray("data");
                for (int i = 0; i < elements.length(); i++) {
                    String url = elements.getJSONObject(i).getString("url");

                    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
                    issueInputParameters.setProjectId(project.getId());
                    issueInputParameters.setIssueTypeId(String.valueOf(Consts.NEWS_ISSUE_TYPE_ID));
                    issueInputParameters.setReporterId(user.getName());
                    issueInputParameters.setAssigneeId(null);
                    issueInputParameters.setSummary(url);
                    issueInputParameters.addCustomFieldValue(Consts.URL_CF_ID, url);
                    issueInputParameters.addCustomFieldValue(Consts.PUBLISHING_DATE_CF_ID, jiraDateFormat.format(date));
                    IssueService.CreateValidationResult createValidationResult = issueService.validateCreate(user.getDirectoryUser(), issueInputParameters);

                    if (!createValidationResult.isValid())
                        throw new Exception(CommonUtils.formatErrorCollection(createValidationResult.getErrorCollection()));

                    createValidationResults.add(createValidationResult);
                }
            }

            for (IssueService.CreateValidationResult createValidationResult : createValidationResults) {
                IssueService.IssueResult issueResult = issueService.create(user.getDirectoryUser(), createValidationResult);
                if (!issueResult.isValid())
                    throw new Exception(CommonUtils.formatErrorCollection(issueResult.getErrorCollection()));
            }
        } finally {
            jiraAuthenticationContext.setLoggedInUser(savedUser);
        }
    }

    @GET
    public Response createNews(@QueryParam("projectKeys") final String projectKeys,
                               @QueryParam("date") final String date) {
        return new RestExecutor<String>() {
            @Override
            protected String doAction() throws Exception {
                Collection<Long> projectIds;
                if (StringUtils.isNotEmpty(projectKeys)) {
                    projectIds = new ArrayList<Long>();
                    for (String projectKey: CommonUtils.split(projectKeys))
                        projectIds.add(projectManager.getProjectByCurrentKeyIgnoreCase(projectKey).getId());
                } else
                    projectIds = Consts.PROJECT_IDS;

                createNewsIssues(projectIds, new SimpleDateFormat(DATE_FORMAT).parse(date));
                return "Success";
            }
        }.getResponse();
    }
}
