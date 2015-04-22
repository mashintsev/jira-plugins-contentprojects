package ru.mail.jira.plugins.contentprojects.authors.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.impl.AbstractSingleFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.UserField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import ru.mail.jira.plugins.contentprojects.authors.Author;
import ru.mail.jira.plugins.contentprojects.authors.AuthorManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AuthorCFType extends AbstractSingleFieldType<Author> implements UserField {
    private final AuthorManager authorManager;

    protected AuthorCFType(CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager, AuthorManager authorManager) {
        super(customFieldValuePersister, genericConfigManager);
        this.authorManager = authorManager;
    }

    @Nonnull
    @Override
    protected PersistenceFieldType getDatabaseType() {
        return PersistenceFieldType.TYPE_LIMITED_TEXT;
    }

    @Nullable
    @Override
    protected String getDbValueFromObject(Author author) {
        return getStringFromSingularObject(author);
    }

    @Nullable
    @Override
    protected Author getObjectFromDbValue(@Nonnull Object value) throws FieldValidationException {
        return getSingularObjectFromString((String) value);
    }

    @Override
    public String getStringFromSingularObject(Author author) {
        if (author == null)
            return "";
        return author.getDbValue();
    }

    @Override
    public Author getSingularObjectFromString(String string) throws FieldValidationException {
        try {
            return authorManager.getAuthor(string);
        } catch (Exception e) {
            throw new FieldValidationException(String.format("Unable to parse value (%s)", string));
        }
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> velocityParameters = super.getVelocityParameters(issue, field, fieldLayoutItem);
        velocityParameters.put(CustomFieldUtils.getParamKeyRequireProjectIds(), true);
        return velocityParameters;
    }

    @SuppressWarnings("unused")
    public Collection<Author> getAuthors(String currentValue, Collection<Long> projectIds) {
        Collection<Author> result = authorManager.getAuthors(projectIds);

        Author currentAuthor = authorManager.getAuthor(currentValue);
        if (currentAuthor != null && !result.contains(currentAuthor))
            result.add(currentAuthor);

        return result;
    }
}