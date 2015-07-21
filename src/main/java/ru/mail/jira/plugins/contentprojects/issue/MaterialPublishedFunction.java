package ru.mail.jira.plugins.contentprojects.issue;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.HttpSender;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import ru.mail.jira.plugins.contentprojects.configuration.PluginData;

import java.sql.Timestamp;
import java.util.Map;

public class MaterialPublishedFunction extends AbstractJiraFunctionProvider {
    private static final Logger log = Logger.getLogger(MaterialPublishedFunction.class);
    private final PluginData pluginData;

    public MaterialPublishedFunction(PluginData pluginData) {
        this.pluginData = pluginData;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        try {
            MutableIssue issue = getIssue(transientVars);

            String url = CommonUtils.getCustomFieldStringValue(issue, Consts.URL_CF_ID);

            String apiUrl = pluginData.getApiUrl(issue.getProjectObject());
            if (StringUtils.isEmpty(apiUrl))
                return;

            String response = new HttpSender(apiUrl, url).sendGet();
            JSONObject json = new JSONObject(response);
            issue.setSummary(json.getJSONObject("data").getString("title"));
            issue.setDescription(json.getJSONObject("data").getString("lead"));
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.PUBLISHING_DATE_CF_ID), new Timestamp(json.getJSONObject("data").getLong("published") * 1000));
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.CATEGORY_CF_ID), json.getJSONObject("data").getString("category"));
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.ESTIMATED_TIME_CF_ID), json.getJSONObject("data").has("words_count") ? Math.round(json.getJSONObject("data").getLong("words_count") / 140.0 * 100) / 100.0 : null);
        } catch (Exception e) {
            log.error(e);
            throw new WorkflowException(e);
        }
    }
}
