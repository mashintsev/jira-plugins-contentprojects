package ru.mail.jira.plugins.contentprojects.statistics;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.query.Query;

import java.util.List;
import java.util.SortedMap;

public class SampleAccessor {
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SearchProvider searchProvider;

    public SampleAccessor(JiraAuthenticationContext jiraAuthenticationContext, SearchProvider searchProvider) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchProvider = searchProvider;
    }

    public Sample getSample(Query query, List<CustomField> customFields) throws SearchException {
        SampleCollector collector = new SampleCollector(customFields);
        searchProvider.search(query, jiraAuthenticationContext.getUser(), collector);
        return collector.getSample();
    }

    public <T> SortedMap<T, Sample> getGroupedSamples(Query query, List<CustomField> customFields, StatisticsMapper<T> statisticsMapper) throws SearchException {
        GroupedSamplesCollector<T> collector = new GroupedSamplesCollector<T>(customFields, statisticsMapper);
        searchProvider.search(query, jiraAuthenticationContext.getUser(), collector);
        return collector.getGroupedSamples();
    }
}
