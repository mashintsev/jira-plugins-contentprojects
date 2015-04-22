package ru.mail.jira.plugins.contentprojects.statistics.searchers;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.customfields.searchers.DateTimeRangeSearcher;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.util.JqlDateSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.action.util.CalendarLanguageUtil;

public class MonthSearcher extends DateTimeRangeSearcher implements CustomFieldStattable {
    private final CustomFieldInputHelper customFieldInputHelper;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final TimeZoneManager timeZoneManager;

    public MonthSearcher(JiraAuthenticationContext jiraAuthenticationContext, JqlOperandResolver jqlOperandResolver, VelocityRequestContextFactory velocityRenderContext, ApplicationProperties applicationProperties, VelocityTemplatingEngine templatingEngine, CalendarLanguageUtil calendarUtils, JqlDateSupport dateSupport, CustomFieldInputHelper customFieldInputHelper, TimeZoneManager timeZoneManager, DateTimeFormatterFactory dateTimeFormatterFactory, FieldVisibilityManager fieldVisibilityManager) {
        super(jiraAuthenticationContext, jqlOperandResolver, velocityRenderContext, applicationProperties, templatingEngine, calendarUtils, dateSupport, customFieldInputHelper, timeZoneManager, dateTimeFormatterFactory, fieldVisibilityManager);
        this.customFieldInputHelper = customFieldInputHelper;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.timeZoneManager = timeZoneManager;
    }

    @Override
    public StatisticsMapper getStatisticsMapper(CustomField customField) {
        return new MonthStatisticsMapper(customField, customFieldInputHelper, jiraAuthenticationContext, timeZoneManager);
    }
}
