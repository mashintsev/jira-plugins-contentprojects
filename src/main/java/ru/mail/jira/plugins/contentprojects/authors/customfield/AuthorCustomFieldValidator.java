package ru.mail.jira.plugins.contentprojects.authors.customfield;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.jql.validator.SupportedOperatorsValidator;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;

import javax.annotation.Nonnull;

public class AuthorCustomFieldValidator implements ClauseValidator {
    @SuppressWarnings("unchecked")
    private final SupportedOperatorsValidator supportedOperatorsValidator = new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);

    @Nonnull
    @Override
    public MessageSet validate(User searcher, @Nonnull TerminalClause terminalClause) {
        return supportedOperatorsValidator.validate(searcher, terminalClause);
    }
}
