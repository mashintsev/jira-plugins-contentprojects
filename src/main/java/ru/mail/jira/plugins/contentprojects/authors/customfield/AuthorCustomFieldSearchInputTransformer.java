package ru.mail.jira.plugins.contentprojects.authors.customfield;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.searchers.transformer.AbstractSingleValueCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.query.Query;

public class AuthorCustomFieldSearchInputTransformer extends AbstractSingleValueCustomFieldSearchInputTransformer {
    public AuthorCustomFieldSearchInputTransformer(CustomField field, ClauseNames clauseNames, String urlParameterName, CustomFieldInputHelper customFieldInputHelper) {
        super(field, clauseNames, urlParameterName, customFieldInputHelper);
    }

    @Override
    public boolean doRelevantClausesFitFilterForm(User user, Query query, SearchContext searchContext) {
        return false;
    }
}
