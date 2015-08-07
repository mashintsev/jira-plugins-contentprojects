package ru.mail.jira.plugins.contentprojects.issue.functions;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.HttpSender;
import ru.mail.jira.plugins.contentprojects.configuration.Counter;
import ru.mail.jira.plugins.contentprojects.configuration.CounterConfig;
import ru.mail.jira.plugins.contentprojects.configuration.CounterManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class HitsFunction extends AbstractJiraFunctionProvider {
    private final CounterManager counterManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public HitsFunction(CounterManager counterManager, JiraAuthenticationContext jiraAuthenticationContext) {
        this.counterManager = counterManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    private String getFilter(String url) throws MalformedURLException {
        return new URL(url).getPath().replaceAll("/$", "");
    }

    private int getHits(String filter, Date publishingDate, int numberOfDays, int counterId, String counterPassword) throws Exception {
        int result = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(publishingDate);
        for (int day = 0; day < numberOfDays; day++) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);

            String response = new HttpSender("http://top.mail.ru/json/pages?id=%s&password=%s&period=0&date=%s&filter_type=0&filter=%s", counterId, counterPassword, date, filter).sendGet();
            JSONObject json = new JSONObject(response);
            JSONArray elements = json.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                if (getFilter(element.getString("url")).equals(filter))
                    result += element.getInt("value");
            }
        }

        return result;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);

        CustomField resultCf = CommonUtils.getCustomField((String) args.get(HitsFunctionFactory.RESULT_FIELD));
        Counter counter = counterManager.getCounter(Integer.parseInt((String) args.get(HitsFunctionFactory.COUNTER)));
        CustomField urlCf = CommonUtils.getCustomField((String) args.get(HitsFunctionFactory.URL_FIELD));
        CustomField dateCf = CommonUtils.getCustomField((String) args.get(HitsFunctionFactory.DATE_FIELD));
        int numberOfDays = Integer.parseInt((String) args.get(HitsFunctionFactory.NUMBER_OF_DAYS));

        String url = (String) issue.getCustomFieldValue(urlCf);
        Date publishingDate = (Date) issue.getCustomFieldValue(dateCf);

        try {
            CounterConfig counterConfig = counterManager.getCounterConfig(counter, issue.getProjectObject());
            if (counterConfig == null || counterConfig.getRatingId() == null)
                throw new Exception("Counter is not configured");

            double hits = getHits(getFilter(url), publishingDate, numberOfDays, counterConfig.getRatingId(), StringUtils.trimToEmpty(counterConfig.getRatingPassword()));
            issue.setCustomFieldValue(resultCf, hits);
        } catch (Exception e) {
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.hits.error", counter.getName()), e);
        }
    }
}
