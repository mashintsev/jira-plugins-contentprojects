package ru.mail.jira.plugins.contentprojects.statistics.searchers;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneManager;
import com.atlassian.jira.util.LuceneUtils;
import com.atlassian.jira.util.dbc.Assertions;
import org.jfree.data.time.Month;

import java.util.Comparator;
import java.util.Date;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;

public class MonthStatisticsMapper implements StatisticsMapper<Month>, SearchRequestAppender.Factory<Month> {
    private final CustomField customField;
    private final CustomFieldInputHelper customFieldInputHelper;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final TimeZoneManager timeZoneManager;
    private final String[] monthNames;

    public MonthStatisticsMapper(CustomField customField, CustomFieldInputHelper customFieldInputHelper, JiraAuthenticationContext jiraAuthenticationContext, TimeZoneManager timeZoneManager) {
        this.customField = customField;
        this.customFieldInputHelper = customFieldInputHelper;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.timeZoneManager = timeZoneManager;
        this.monthNames = new String[] {
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.january"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.february"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.march"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.april"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.may"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.june"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.july"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.august"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.september"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.october"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.november"),
                jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.statistics.monthSearcher.december")
        };
    }

    @Override
    public String getDocumentConstant() {
        return customField.getId();
    }

    @Override
    public Month getValueFromLuceneField(String documentValue) {
        if (documentValue == null)
            return null;
        Date date = LuceneUtils.stringToDate(documentValue);
        if (date == null)
            return null;
        return new Month(date, timeZoneManager.getLoggedInUserTimeZone(), jiraAuthenticationContext.getLocale()) {
            @Override
            public String toString() {
                return String.format("%s %d", monthNames[getMonth() - 1], getYearValue());
            }
        };
    }

    @Override
    public Comparator<Month> getComparator() {
        return new Comparator<Month>() {
            public int compare(Month month1, Month month2) {
                if (month1 == null && month2 == null)
                    return 0;
                if (month1 == null)
                    return -1;
                if (month2 == null)
                    return 1;
                return month1.compareTo(month2);
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        MonthStatisticsMapper that = (MonthStatisticsMapper) obj;
        return getDocumentConstant().equals(that.getDocumentConstant());
    }

    @Override
    public int hashCode() {
        return getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0;
    }

    @Override
    public boolean isValidValue(Month value) {
        return true;
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue() {
        return false;
    }

    @Override
    public SearchRequest getSearchUrlSuffix(Month month, SearchRequest searchRequest) {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(month, searchRequest);
    }

    @Override
    public SearchRequestAppender<Month> getSearchRequestAppender() {
        return new MonthSearchRequestAppender(customFieldInputHelper.getUniqueClauseName(jiraAuthenticationContext.getUser().getDirectoryUser(), customField.getClauseNames().getPrimaryName(), customField.getName()));
    }

    static class MonthSearchRequestAppender implements SearchRequestAddendumBuilder.AddendumCallback<Month>, SearchRequestAppender<Month> {
        private final String clauseName;

        public MonthSearchRequestAppender(String clauseName) {
            this.clauseName = Assertions.notNull("clauseName", clauseName);
        }

        @Override
        public void appendNonNullItem(Month value, JqlClauseBuilder clauseBuilder) {
            clauseBuilder.addDateRangeCondition(clauseName, value.getStart(), value.getEnd());
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder) {
            clauseBuilder.addEmptyCondition(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(Month value, SearchRequest searchRequest) {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable<? extends Month> values, SearchRequest searchRequest) {
            // doesn't really make sense for this implementation
            return null;
        }
    }
}