package ru.mail.jira.plugins.contentprojects.authors.customfield;

import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAppender;
import com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.dbc.Assertions;
import ru.mail.jira.plugins.contentprojects.authors.Author;
import ru.mail.jira.plugins.contentprojects.authors.AuthorManager;

import java.util.Comparator;

import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndClause;
import static com.atlassian.jira.issue.search.util.SearchRequestAddendumBuilder.appendAndNotClauses;

public class AuthorStatisticsMapper implements StatisticsMapper<Author>, SearchRequestAppender.Factory<Author> {
    private final AuthorManager authorManager;
    private final CustomField customField;
    private final CustomFieldInputHelper customFieldInputHelper;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public AuthorStatisticsMapper(AuthorManager authorManager, CustomField customField, CustomFieldInputHelper customFieldInputHelper, JiraAuthenticationContext jiraAuthenticationContext) {
        this.authorManager = authorManager;
        this.customField = customField;
        this.customFieldInputHelper = customFieldInputHelper;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    @Override
    public String getDocumentConstant() {
        return customField.getId();
    }

    @Override
    public Author getValueFromLuceneField(String documentValue) {
        return authorManager.getAuthor(documentValue);
    }

    @Override
    public Comparator<Author> getComparator() {
        return new Comparator<Author>() {
            public int compare(Author author1, Author author2) {
                if (author1 == null && author2 == null)
                    return 0;
                if (author1 == null)
                    return -1;
                if (author2 == null)
                    return 1;
                return author1.compareTo(author2);
            }
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        AuthorStatisticsMapper that = (AuthorStatisticsMapper) obj;
        return getDocumentConstant().equals(that.getDocumentConstant());
    }

    @Override
    public int hashCode() {
        return getDocumentConstant() != null ? getDocumentConstant().hashCode() : 0;
    }

    @Override
    public boolean isValidValue(Author value) {
        return true;
    }

    @Override
    public boolean isFieldAlwaysPartOfAnIssue() {
        return false;
    }

    @Override
    public SearchRequest getSearchUrlSuffix(Author value, SearchRequest searchRequest) {
        return getSearchRequestAppender().appendInclusiveSingleValueClause(value, searchRequest);
    }

    @Override
    public SearchRequestAppender<Author> getSearchRequestAppender() {
        return new AuthorSearchRequestAppender(customFieldInputHelper.getUniqueClauseName(jiraAuthenticationContext.getUser().getDirectoryUser(), customField.getClauseNames().getPrimaryName(), customField.getName()));
    }

    static class AuthorSearchRequestAppender implements SearchRequestAddendumBuilder.AddendumCallback<Author>, SearchRequestAppender<Author> {
        private final String clauseName;

        public AuthorSearchRequestAppender(String clauseName) {
            this.clauseName = Assertions.notNull("clauseName", clauseName);
        }

        @Override
        public void appendNonNullItem(Author value, JqlClauseBuilder clauseBuilder) {
            Object searchValue = value.getSearchValue();
            if (searchValue instanceof Long)
                clauseBuilder.addNumberCondition(clauseName, (Long) searchValue);
            else
                clauseBuilder.addStringCondition(clauseName, searchValue.toString());
        }

        @Override
        public void appendNullItem(JqlClauseBuilder clauseBuilder) {
            clauseBuilder.addEmptyCondition(clauseName);
        }

        @Override
        public SearchRequest appendInclusiveSingleValueClause(Author value, SearchRequest searchRequest) {
            return appendAndClause(value, searchRequest, this);
        }

        @Override
        public SearchRequest appendExclusiveMultiValueClause(Iterable<? extends Author> values, SearchRequest searchRequest) {
            return appendAndNotClauses(values, searchRequest, this);
        }
    }
}
