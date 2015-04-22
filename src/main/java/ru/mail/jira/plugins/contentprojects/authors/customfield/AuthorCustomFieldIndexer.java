package ru.mail.jira.plugins.contentprojects.authors.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import ru.mail.jira.plugins.contentprojects.authors.Author;

public class AuthorCustomFieldIndexer extends AbstractCustomFieldIndexer {
    public AuthorCustomFieldIndexer(FieldVisibilityManager fieldVisibilityManager, CustomField customField) {
        super(fieldVisibilityManager, customField);
    }

    @Override
    public void addDocumentFieldsSearchable(Document doc, Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    @Override
    public void addDocumentFieldsNotSearchable(Document doc, Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    private void addDocumentFields(Document doc, Issue issue, Field.Index indexType) {
        Object value = customField.getValue(issue);
        if (value instanceof Author)
            doc.add(new Field(getDocumentFieldId(), ((Author) value).getDbValue(), Field.Store.YES, indexType));
    }
}
