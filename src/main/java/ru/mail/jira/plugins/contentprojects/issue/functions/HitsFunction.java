package ru.mail.jira.plugins.contentprojects.issue.functions;

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
                if (AbstractFunctionFactory.getFilter(element.getString("url")).equals(filter))
                    result += element.getInt("value");
            }
        }

        return result;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);

        CustomField publishingDateCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.PUBLISHING_DATE_FIELD));
        CustomField hitsCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.HITS_FIELD));
        CustomField urlCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.URL_FIELD));
        Counter counter = counterManager.getCounter(Integer.parseInt((String) args.get(AbstractFunctionFactory.COUNTER)));
        int numberOfDays = Integer.parseInt((String) args.get(AbstractFunctionFactory.NUMBER_OF_DAYS));

        Date publishingDate = (Date) issue.getCustomFieldValue(publishingDateCf);
        String url = (String) issue.getCustomFieldValue(urlCf);
        if (publishingDate == null || StringUtils.isEmpty(url))
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.emptyFieldsError"));

        CounterConfig counterConfig = counterManager.getCounterConfig(counter, issue.getProjectObject());
        if (counterConfig == null || counterConfig.getRatingId() == null)
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.notConfiguredCounterError", counter.getName()));
        if (counterConfig.getRatingId() == 0)
            return;

        try {
            int hits = getHits(AbstractFunctionFactory.getFilter(url), publishingDate, numberOfDays, counterConfig.getRatingId(), StringUtils.trimToEmpty(counterConfig.getRatingPassword()));
            issue.setCustomFieldValue(hitsCf, (double) hits);
        } catch (Exception e) {
            AbstractFunctionFactory.sendErrorEmail("ru.mail.jira.plugins.contentprojects.issue.functions.counterError", counter.getName(), issue, url);
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.counterError", counter.getName()), e);
        }
    }
}
