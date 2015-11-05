package ru.mail.jira.plugins.contentprojects.issue.functions;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.mail.jira.plugins.commons.CommonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class UpdateUrlFunction extends AbstractJiraFunctionProvider {
    private final Logger log = LoggerFactory.getLogger(UpdateUrlFunction.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;

    public UpdateUrlFunction(JiraAuthenticationContext jiraAuthenticationContext) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    private String getRedirectUrl(String url) {
        InputStream inputStream = null;
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.connect();
            inputStream = connection.getInputStream();
            return connection.getURL().toString();
        } catch (IOException e) {
            log.error("Unable to create connection", e);
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Unable to close connection", e);
                }
        }

        return null;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);

        CustomField urlCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.URL_FIELD));

        String url = (String) issue.getCustomFieldValue(urlCf);
        if (StringUtils.isEmpty(url))
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.emptyFieldsError"));

        try {
            String redirectUrl = getRedirectUrl(url);
            if (redirectUrl != null)
                issue.setCustomFieldValue(urlCf, redirectUrl);
        } catch (Exception e) {
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.updateUrlError"), e);
        }
    }
}
