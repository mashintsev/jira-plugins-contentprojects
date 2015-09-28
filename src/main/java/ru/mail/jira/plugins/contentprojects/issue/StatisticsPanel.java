package ru.mail.jira.plugins.contentprojects.issue;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.velocity.NumberTool;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.web.ContextProvider;
import org.apache.log4j.Logger;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import ru.mail.jira.plugins.contentprojects.statistics.SampleAccessor;
import ru.mail.jira.plugins.contentprojects.statistics.Statistic;

import java.util.*;

public class StatisticsPanel implements ContextProvider {
    private static final String JQL = "project = %d AND issuetype = %s AND cf[%d] > \"-37d\"";

    private static final Logger log = Logger.getLogger(StatisticsPanel.class);
    private final List<CustomField> customFields;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final SearchService searchService;
    private final SampleAccessor sampleAccessor;

    public StatisticsPanel(JiraAuthenticationContext jiraAuthenticationContext, SearchService searchService, SampleAccessor sampleAccessor) {
        this.customFields = Arrays.asList(
                CommonUtils.getCustomField(Consts.COST_CF_ID),
                CommonUtils.getCustomField(Consts.HIT_COST_CF_ID),
                CommonUtils.getCustomField(Consts.SHARE_COST_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_CF_ID),
                CommonUtils.getCustomField(Consts.SHARES_CF_ID),
                CommonUtils.getCustomField(Consts.SHARES_FACEBOOK_CF_ID),
                CommonUtils.getCustomField(Consts.SHARES_MYMAIL_CF_ID),
                CommonUtils.getCustomField(Consts.SHARES_ODNOKLASSNIKI_CF_ID),
                CommonUtils.getCustomField(Consts.SHARES_TWITTER_CF_ID),
                CommonUtils.getCustomField(Consts.SHARES_VKONTAKTE_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_FACEBOOK_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_MYMAIL_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_ODNOKLASSNIKI_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_TWITTER_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_VKONTAKTE_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_GOOGLE_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_YANDEX_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_OTHERS_CF_ID),
                CommonUtils.getCustomField(Consts.SCROLL_CF_IDS.get(0)),
                CommonUtils.getCustomField(Consts.SCROLL_CF_IDS.get(1)),
                CommonUtils.getCustomField(Consts.SCROLL_CF_IDS.get(2)),
                CommonUtils.getCustomField(Consts.SCROLL_CF_IDS.get(3)),
                CommonUtils.getCustomField(Consts.SCROLL_CF_IDS.get(4)),
                CommonUtils.getCustomField(Consts.TOTAL_TIME_CF_ID),
                CommonUtils.getCustomField(Consts.TIME_INTERVAL_CF_IDS.get(0)),
                CommonUtils.getCustomField(Consts.TIME_INTERVAL_CF_IDS.get(1)),
                CommonUtils.getCustomField(Consts.TIME_INTERVAL_CF_IDS.get(2)),
                CommonUtils.getCustomField(Consts.ESTIMATED_TIME_CF_ID),
                CommonUtils.getCustomField(Consts.COMMENTS_CF_ID),
                CommonUtils.getCustomField(Consts.SHARE_RATIO_CF_ID),
                CommonUtils.getCustomField(Consts.SOCIAL_ENGAGEMENT_CF_ID),
                CommonUtils.getCustomField(Consts.HITS_TOUCH_CF_ID),
                CommonUtils.getCustomField(Consts.SCROLL_TOUCH_CF_IDS.get(0)),
                CommonUtils.getCustomField(Consts.SCROLL_TOUCH_CF_IDS.get(1)),
                CommonUtils.getCustomField(Consts.SCROLL_TOUCH_CF_IDS.get(2)),
                CommonUtils.getCustomField(Consts.SCROLL_TOUCH_CF_IDS.get(3)),
                CommonUtils.getCustomField(Consts.SCROLL_TOUCH_CF_IDS.get(4))
        );
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.searchService = searchService;
        this.sampleAccessor = sampleAccessor;
    }

    @Override
    public void init(Map<String, String> paramMap) throws PluginParseException {
    }

    private Field getDoubleField(List<Statistic> fieldStatisticValues, int index, Issue issue, boolean greaterIsBetter) {
        Statistic statisticValues = fieldStatisticValues.get(index);
        Double statisticValue = statisticValues == null ? null : statisticValues.getMedian();

        CustomField customField = customFields.get(index);
        Double issueValue = (Double) issue.getCustomFieldValue(customField);

        return new Field(customField.getName(), statisticValue, issueValue, String.format("#%s-val", customField.getId()), greaterIsBetter);
    }

    private Field getSearchEnginesField(List<Statistic> fieldStatisticValues, int[] indexes, Issue issue, boolean greaterIsBetter) {
        Statistic total = fieldStatisticValues.get(indexes[0]);
        Statistic google = fieldStatisticValues.get(indexes[1]);
        Statistic yandex = fieldStatisticValues.get(indexes[2]);
        Statistic others = fieldStatisticValues.get(indexes[3]);
        SearchEngines statisticValue;
        if (total == null || google == null || yandex == null || others == null)
            statisticValue = null;
        else
            statisticValue = new SearchEngines(total.getMedian(), google.getMedian(), yandex.getMedian(), others.getMedian());

        CustomField customField = customFields.get(indexes[0]);
        Double issueValue = (Double) issue.getCustomFieldValue(customField);

        return new Field(customField.getName(), statisticValue, issueValue, String.format("#%s-val div", customField.getId()), greaterIsBetter);
    }

    private Field getSocialMediaField(List<Statistic> fieldStatisticValues, int[] indexes, Issue issue, boolean greaterIsBetter) {
        Statistic total = fieldStatisticValues.get(indexes[0]);
        Statistic facebook = fieldStatisticValues.get(indexes[1]);
        Statistic mymail = fieldStatisticValues.get(indexes[2]);
        Statistic odnoklassniki = fieldStatisticValues.get(indexes[3]);
        Statistic twitter = fieldStatisticValues.get(indexes[4]);
        Statistic vkontakte = fieldStatisticValues.get(indexes[5]);
        SocialMedia statisticValue;
        if (total == null || facebook == null || mymail == null || odnoklassniki == null || twitter == null || vkontakte == null)
            statisticValue = null;
        else
            statisticValue = new SocialMedia(total.getMedian(), facebook.getMedian(), mymail.getMedian(), odnoklassniki.getMedian(), twitter.getMedian(), vkontakte.getMedian());

        CustomField customField = customFields.get(indexes[0]);
        Double issueValue = (Double) issue.getCustomFieldValue(customField);

        return new Field(customField.getName(), statisticValue, issueValue, String.format("#%s-val div", customField.getId()), greaterIsBetter);
    }

    @Override
    public Map<String, Object> getContextMap(Map<String, Object> paramMap) {
        try {
            Issue issue = (Issue) paramMap.get("issue");

            String jql = String.format(JQL, issue.getProjectObject().getId(), issue.getIssueTypeId(), Consts.PUBLISHING_DATE_CF_ID);
            SearchService.ParseResult parseResult = searchService.parseQuery(jiraAuthenticationContext.getUser().getDirectoryUser(), jql);
            if (!parseResult.isValid())
                throw new Exception(String.format("Unable to parse JQL, %s", parseResult.getErrors()));
            List<Statistic> fieldStatisticValues = sampleAccessor.getSample(parseResult.getQuery(), customFields).calculateStatistics();

            List<Field> fields = new LinkedList<Field>();
            fields.add(getDoubleField(fieldStatisticValues, 0, issue, false));
            fields.add(getDoubleField(fieldStatisticValues, 1, issue, false));
            fields.add(getDoubleField(fieldStatisticValues, 2, issue, false));
            fields.add(getDoubleField(fieldStatisticValues, 3, issue, true));
            fields.add(getSocialMediaField(fieldStatisticValues, new int[] { 4, 5, 6, 7, 8, 9 }, issue, true));
            fields.add(getSocialMediaField(fieldStatisticValues, new int[] { 10, 11, 12, 13, 14, 15 }, issue, true));
            fields.add(getSearchEnginesField(fieldStatisticValues, new int[] { 16, 17, 18, 19 }, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 20, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 21, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 22, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 23, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 24, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 25, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 26, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 27, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 28, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 29, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 30, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 31, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 32, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 33, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 34, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 35, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 36, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 37, issue, true));
            fields.add(getDoubleField(fieldStatisticValues, 38, issue, true));

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("numberTool", new NumberTool(jiraAuthenticationContext.getI18nHelper().getLocale()));
            params.put("fields", fields);
            return params;
        } catch (Exception e) {
            log.error(e);
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class Field {
        private final String name;
        private final Number statisticValue;
        private final String selector;
        private final Boolean greater;
        private final Boolean better;

        public Field(String name, Number statisticValue, Number issueValue, String selector, boolean greaterIsBetter) {
            this.name = name;
            this.statisticValue = statisticValue;
            if (issueValue != null && statisticValue != null) {
                this.selector = selector;
                this.greater = issueValue.doubleValue() >= statisticValue.doubleValue();
                this.better = !greaterIsBetter ^ this.greater || issueValue.doubleValue() == statisticValue.doubleValue();
            } else {
                this.selector = null;
                this.greater = null;
                this.better = null;
            }
        }

        public String getName() {
            return name;
        }

        public Number getStatisticValue() {
            return statisticValue;
        }

        public String getSelector() {
            return selector;
        }

        public Boolean isGreater() {
            return greater;
        }

        public Boolean isBetter() {
            return better;
        }
    }
}
