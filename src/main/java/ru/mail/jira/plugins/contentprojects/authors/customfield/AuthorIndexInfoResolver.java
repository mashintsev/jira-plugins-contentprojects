package ru.mail.jira.plugins.contentprojects.authors.customfield;

import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import ru.mail.jira.plugins.contentprojects.authors.Author;

import java.util.Collections;
import java.util.List;

public class AuthorIndexInfoResolver implements IndexInfoResolver<Author> {
    @Override
    public List<String> getIndexedValues(String rawValue) {
        return Collections.singletonList(rawValue);
    }

    @Override
    public List<String> getIndexedValues(Long rawValue) {
        return Collections.singletonList("&" + rawValue);
    }

    @Override
    public String getIndexedValue(Author indexedObject) {
        return indexedObject.getDbValue();
    }
}
