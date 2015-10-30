package ru.mail.jira.plugins.contentprojects.issue.functions;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.contentprojects.common.Consts;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class UpdateUrlFunction extends AbstractJiraFunctionProvider {
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public UpdateUrlFunction(ApplicationProperties applicationProperties, JiraAuthenticationContext jiraAuthenticationContext) {
        this.applicationProperties = applicationProperties;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    private String getRedirectUrl(String url) throws Exception {
        String redirectUrl;

        URLConnection connection = new URL(url).openConnection();
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        redirectUrl = connection.getURL().toString();
        inputStream.close();

        return redirectUrl;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);

        CustomField urlCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.URL_FIELD));

        String url = (String) issue.getCustomFieldValue(urlCf);
        if (StringUtils.isEmpty(url)){
            String message = jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.emptyFieldsError");
            AbstractFunctionFactory.sendErrorEmail(jiraAuthenticationContext, applicationProperties, message, issue.getKey());
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.emptyFieldsError"));
        }

        try {
            String redirectUrl = getRedirectUrl(url);
            if (StringUtils.isNotEmpty(redirectUrl))
                issue.setCustomFieldValue(urlCf, redirectUrl);
        } catch (Exception e) {
            String message = jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.updateUrlError");
            AbstractFunctionFactory.sendErrorEmail(jiraAuthenticationContext, applicationProperties, message, issue.getKey());
            throw new WorkflowException(message, e);
        }
    }
}
