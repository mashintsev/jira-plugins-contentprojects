package ru.mail.jira.plugins.contentprojects.authors.customfield;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.SingleValueCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldValueGeneratingClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.EqualityQueryFactory;
import com.atlassian.jira.jql.query.GenericClauseQueryFactory;
import com.atlassian.jira.jql.query.OperatorSpecificQueryFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import ru.mail.jira.plugins.contentprojects.authors.Author;
import ru.mail.jira.plugins.contentprojects.authors.AuthorManager;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class AuthorSearcher extends AbstractInitializationCustomFieldSearcher implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable {
    private final AuthorManager authorManager;
    private final CustomFieldInputHelper customFieldInputHelper;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final JqlOperandResolver jqlOperandResolver;

    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

    public AuthorSearcher(AuthorManager authorManager, CustomFieldInputHelper customFieldInputHelper, FieldVisibilityManager fieldVisibilityManager, JiraAuthenticationContext jiraAuthenticationContext, JqlOperandResolver jqlOperandResolver) {
        this.authorManager = authorManager;
        this.customFieldInputHelper = customFieldInputHelper;
        this.fieldVisibilityManager = fieldVisibilityManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.jqlOperandResolver = jqlOperandResolver;
    }

    @Override
    public void init(CustomField field) {
        this.searcherInformation = new CustomFieldSearcherInformation(
                field.getId(),
                field.getNameKey(),
                Collections.singletonList(new AuthorCustomFieldIndexer(fieldVisibilityManager, field)),
                new AtomicReference<CustomField>(field));
        this.searchInputTransformer = new AuthorCustomFieldSearchInputTransformer(
                field,
                field.getClauseNames(),
                field.getId(),
                customFieldInputHelper);
        this.searchRenderer = new CustomFieldRenderer(
                field.getClauseNames(),
                getDescriptor(),
                field,
                new SingleValueCustomFieldValueProvider(),
                fieldVisibilityManager);
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldValueGeneratingClauseHandler(
                new AuthorCustomFieldValidator(),
                new GenericClauseQueryFactory(
                        field.getId(),
                        Collections.<OperatorSpecificQueryFactory>singletonList(new EqualityQueryFactory<Author>(new AuthorIndexInfoResolver())),
                        jqlOperandResolver),
                new AuthorClauseValuesGenerator(authorManager),
                OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
                JiraDataTypes.USER);
    }

    @Override
    public SearcherInformation<CustomField> getSearchInformation() {
        if (searcherInformation == null)
            throw new IllegalStateException("Attempt to retrieve searcherInformation off uninitialised custom field searcher.");
        return searcherInformation;
    }

    @Override
    public SearchInputTransformer getSearchInputTransformer() {
        if (searchInputTransformer == null)
            throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
        return searchInputTransformer;
    }

    @Override
    public SearchRenderer getSearchRenderer() {
        if (searchRenderer == null)
            throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
        return searchRenderer;
    }

    @Override
    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
        if (customFieldSearcherClauseHandler == null)
            throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
        return customFieldSearcherClauseHandler;
    }

    @Override
    public LuceneFieldSorter getSorter(CustomField customField) {
        return new AuthorStatisticsMapper(authorManager, customField, customFieldInputHelper, jiraAuthenticationContext);
    }

    @Override
    public StatisticsMapper getStatisticsMapper(CustomField customField) {
        return new AuthorStatisticsMapper(authorManager, customField, customFieldInputHelper, jiraAuthenticationContext);
    }
}
