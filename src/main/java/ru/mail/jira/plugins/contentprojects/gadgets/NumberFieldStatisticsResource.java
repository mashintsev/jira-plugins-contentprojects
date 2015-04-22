package ru.mail.jira.plugins.contentprojects.gadgets;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.charts.util.ChartUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.*;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.util.RedundantClausesQueryOptimizer;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.util.DefaultFieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.FieldValueToDisplayTransformer;
import com.atlassian.jira.issue.statistics.util.ObjectToFieldValueMapper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.query.Query;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.RestExecutor;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import ru.mail.jira.plugins.contentprojects.statistics.Sample;
import ru.mail.jira.plugins.contentprojects.statistics.SampleAccessor;
import ru.mail.jira.plugins.contentprojects.statistics.Statistic;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@Produces({ MediaType.APPLICATION_JSON })
@Path("/numberFieldStatistics")
public class NumberFieldStatisticsResource {
    private static final String FILTER_PREFIX = "filter-";
    private static final String PROJECT_PREFIX = "project-";
    private static final String JQL_PREFIX = "jql-";
    private static final String PROJECT = "project";

    private final ChartUtils chartUtils;
    private final CustomFieldManager customFieldManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SampleAccessor sampleAccessor;
    private final SearchService searchService;

    public NumberFieldStatisticsResource(ChartUtils chartUtils, CustomFieldManager customFieldManager, JiraAuthenticationContext jiraAuthenticationContext, SampleAccessor sampleAccessor, SearchService searchService) {
        this.chartUtils = chartUtils;
        this.customFieldManager = customFieldManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.sampleAccessor = sampleAccessor;
        this.searchService = searchService;
    }

    private SearchRequest getSearchRequestAndValidate(String projectOrFilterId, Map<String, Object> params) {
        if (StringUtils.isEmpty(projectOrFilterId))
            throw new IllegalArgumentException("gadget.common.required.query");

        SearchRequest searchRequest = null;
        try {
            searchRequest = chartUtils.retrieveOrMakeSearchRequest(projectOrFilterId, params);
        } catch (IllegalArgumentException ignore) {
        }

        if (searchRequest == null) {
            if (projectOrFilterId.startsWith(FILTER_PREFIX)) {
                throw new IllegalArgumentException("gadget.common.invalid.filter");
            } else if (projectOrFilterId.startsWith(PROJECT_PREFIX)) {
                throw new IllegalArgumentException("gadget.common.invalid.project");
            } else if (projectOrFilterId.startsWith(JQL_PREFIX)) {
                throw new IllegalArgumentException("gadget.common.invalid.jql");
            } else
                throw new IllegalArgumentException("gadget.common.invalid.projectOrFilterId");
        }
        return searchRequest;
    }

    @GET
    @Path("/validate")
    public Response validate(@QueryParam("projectOrFilterId") final String projectOrFilterId) {
        return new RestExecutor<Void>() {
            @Override
            protected Void doAction() {
                try {
                    getSearchRequestAndValidate(projectOrFilterId, new HashMap<String, Object>());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(String.format("{ \"errors\": [ { \"field\": \"projectOrFilterId\", \"error\": %s, \"params\": [] } ] }", JSONObject.quote(e.getMessage())));
                }
                return null;
            }
        }.getResponse();
    }

    private String getIssueNavigatorUrl(SearchRequest searchRequest) {
        if (searchRequest == null)
            return null;

        Query optimizedQuery = new RedundantClausesQueryOptimizer().optimizeQuery(searchRequest.getQuery());
        return "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(jiraAuthenticationContext.getUser().getDirectoryUser(), optimizedQuery);
    }

    @GET
    public Response getData(@QueryParam("projectOrFilterId") final String projectOrFilterId,
                            @QueryParam("fieldIds") final List<String> fieldIds,
                            @QueryParam("groupFieldId") final String groupFieldId,
                            @QueryParam("sortFieldIndex") final Integer sortFieldIndex,
                            @QueryParam("sortDesc") final boolean sortDesc) {
        return new RestExecutor<Output>() {
            @Override
            protected Output doAction() throws SearchException {
                Map<String, Object> params = new HashMap<String, Object>();
                SearchRequest searchRequest = getSearchRequestAndValidate(projectOrFilterId, params);
                String projectOrFilterName = params.containsKey(PROJECT) ? ((Project) params.get(PROJECT)).getName() : searchRequest.getName();
                String url = getIssueNavigatorUrl(searchRequest);

                List<CustomField> fields = new ArrayList<CustomField>(fieldIds.size());
                List<String> fieldNames = new ArrayList<String>(fieldIds.size());
                for (String fieldId : fieldIds) {
                    CustomField customField = customFieldManager.getCustomFieldObject(fieldId);
                    if (customField != null) {
                        fields.add(customField);
                        fieldNames.add(customField.getName());
                    }
                }

                List<Group> groups = new ArrayList<Group>();
                if (StringUtils.isEmpty(groupFieldId)) {
                    Sample sample = sampleAccessor.getSample(searchRequest.getQuery(), fields);
                    groups.add(new Group(null, null, sample.getIssueCount(), sample.calculateStatistics()));
                } else {
                    //noinspection unchecked
                    StatisticsMapper<Object> statisticsMapper = new FilterStatisticsValuesGenerator().getStatsMapper(groupFieldId);
                    FieldValueToDisplayTransformer<String> transformer = new DefaultFieldValueToDisplayTransformer(jiraAuthenticationContext.getI18nHelper(), customFieldManager);

                    SortedMap<Object, Sample> groupedSamples = sampleAccessor.getGroupedSamples(searchRequest.getQuery(), fields, statisticsMapper);
                    for (Map.Entry<Object, Sample> entry : groupedSamples.entrySet()) {
                        String title = ObjectToFieldValueMapper.transform(groupFieldId, entry.getKey(), null, transformer);
                        String groupUrl = getIssueNavigatorUrl(statisticsMapper.getSearchUrlSuffix(entry.getKey(), searchRequest));
                        groups.add(new Group(title, groupUrl, entry.getValue().getIssueCount(), entry.getValue().calculateStatistics()));
                    }

                    if (sortFieldIndex != null && sortFieldIndex >= 0)
                        Collections.sort(groups, new Comparator<Group>() {
                            @Override
                            public int compare(Group group1, Group group2) {
                                Statistic statisticValues1 = group1.getFieldStatisticValues().get(sortFieldIndex);
                                Statistic statisticValues2 = group2.getFieldStatisticValues().get(sortFieldIndex);
                                if (statisticValues1 == null && statisticValues2 == null)
                                    return 0;
                                if (statisticValues1 == null)
                                    return 1;
                                if (statisticValues2 == null)
                                    return -1;
                                int result = Double.compare(statisticValues1.getMedian(), statisticValues2.getMedian());
                                if (result == 0) {
                                    result = Double.compare(statisticValues1.getMean(), statisticValues2.getMean());
                                    if (result == 0)
                                        result = Double.compare(statisticValues1.getSum(), statisticValues2.getSum());
                                }
                                return result;
                            }
                        });
                    if (sortDesc)
                        Collections.reverse(groups);
                }

                return new Output(projectOrFilterName, url, groupFieldId, fieldNames, groups);
            }
        }.getResponse();
    }

    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    @XmlRootElement
    public class Group {
        @XmlElement
        private String title;
        @XmlElement
        private String url;
        @XmlElement
        private int issueCount;
        @XmlElement
        private List<Statistic> fieldStatisticValues;

        private Group() {
        }

        public Group(String title, String url, int issueCount, List<Statistic> fieldStatisticValues) {
            this.title = title;
            this.url = url;
            this.issueCount = issueCount;
            this.fieldStatisticValues = fieldStatisticValues;
        }

        public List<Statistic> getFieldStatisticValues() {
            return fieldStatisticValues;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @XmlRootElement
    public static class Output {
        @XmlElement
        private String projectOrFilterName;
        @XmlElement
        private String url;
        @XmlElement
        private String groupFieldId;
        @XmlElement
        private List<String> fieldNames;
        @XmlElement
        private List<Group> groups;

        private Output() {
        }

        public Output(String projectOrFilterName, String url, String groupFieldId, List<String> fieldNames, List<Group> groups) {
            this.projectOrFilterName = projectOrFilterName;
            this.url = url;
            this.groupFieldId = groupFieldId;
            this.fieldNames = fieldNames;
            this.groups = groups;
        }
    }
}
