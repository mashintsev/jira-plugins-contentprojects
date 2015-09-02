package ru.mail.jira.plugins.contentprojects.issue.functions;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.HttpSender;
import ru.mail.jira.plugins.contentprojects.configuration.PluginData;

import java.sql.Timestamp;
import java.util.Map;

public class ApiDataFunction extends AbstractJiraFunctionProvider {
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PluginData pluginData;

    public ApiDataFunction(JiraAuthenticationContext jiraAuthenticationContext, PluginData pluginData) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.pluginData = pluginData;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);

        CustomField categoryCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.CATEGORY_FIELD));
        CustomField publishingDateCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.PUBLISHING_DATE_FIELD));
        CustomField estimatedTimeCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.ESTIMATED_TIME_FIELD));
        CustomField commentsCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.COMMENTS_FIELD));
        CustomField urlCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.URL_FIELD));

        String apiUrl = pluginData.getApiUrl(issue.getProjectObject());
        if (StringUtils.isEmpty(apiUrl))
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.notConfiguredApiError"));

        String url = (String) issue.getCustomFieldValue(urlCf);
        if (StringUtils.isEmpty(url))
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.emptyFieldsError"));

        try {
            String response = new HttpSender(apiUrl, url).sendGet();
            JSONObject json = new JSONObject(response);
            JSONObject data = json.getJSONObject("data");
            String title = data.getString("title");
            String lead = data.getString("lead");
            String category = data.getString("category");
            long publishingDate = data.getLong("published") * 1000;
            Double estimatedTime = data.has("words_count") ? AbstractFunctionFactory.round(data.getLong("words_count") / 140.0) : null;
            int comments = data.getInt("comments_count");

            issue.setSummary(title);
            issue.setDescription(lead);
            issue.setCustomFieldValue(categoryCf, category);
            issue.setCustomFieldValue(publishingDateCf, new Timestamp(publishingDate));
            issue.setCustomFieldValue(estimatedTimeCf, estimatedTime);
            issue.setCustomFieldValue(commentsCf, (double) comments);
        } catch (Exception e) {
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.apiError"), e);
        }
    }
}
