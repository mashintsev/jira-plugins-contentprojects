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
import ru.mail.jira.plugins.contentprojects.configuration.Counter;
import ru.mail.jira.plugins.contentprojects.configuration.CounterConfig;
import ru.mail.jira.plugins.contentprojects.configuration.CounterManager;

import java.text.SimpleDateFormat;
import java.util.*;

public class TimeFunction extends AbstractJiraFunctionProvider {
    private final CounterManager counterManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public TimeFunction(CounterManager counterManager, JiraAuthenticationContext jiraAuthenticationContext) {
        this.counterManager = counterManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    private Double getTime(String filter, Date publishingDate, int numberOfDays, int counterId, String counterPassword) throws Exception {
        long t = 0, v = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(publishingDate);
        for (int day = 0; day < numberOfDays; day++) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);

            String responseT = new HttpSender("http://top.mail.ru/json/goals?id=%s&password=%s&period=0&date=%s&goal=%s", counterId, counterPassword, date, "jse:t_" + filter).sendGet();
            JSONObject jsonT = new JSONObject(responseT);
            if (jsonT.has("total2") && !jsonT.isNull("total2"))
                t += jsonT.getLong("total2");

            String responseV = new HttpSender("http://top.mail.ru/json/goals?id=%s&password=%s&period=0&date=%s&goal=%s", counterId, counterPassword, date, "jse:v_" + filter).sendGet();
            JSONObject jsonV = new JSONObject(responseV);
            if (jsonV.has("total2") && !jsonV.isNull("total2"))
                v += jsonV.getLong("total2");
        }

        if (v == 0)
            return null;
        return AbstractFunctionFactory.round((double) t / v / 60);
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);

        CustomField publishingDateCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.PUBLISHING_DATE_FIELD));
        CustomField timeCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.TIME_FIELD));
        CustomField urlCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.URL_FIELD));
        Counter counter = counterManager.getCounter(Integer.parseInt((String) args.get(AbstractFunctionFactory.COUNTER)));
        int numberOfDays = Integer.parseInt((String) args.get(AbstractFunctionFactory.NUMBER_OF_DAYS));

        CounterConfig counterConfig = counterManager.getCounterConfig(counter, issue.getProjectObject());
        if (counterConfig == null || counterConfig.getRatingId() == null)
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.notConfiguredCounterError", counter.getName()));
        if (counterConfig.getRatingId() == 0)
            return;

        Date publishingDate = (Date) issue.getCustomFieldValue(publishingDateCf);
        String url = (String) issue.getCustomFieldValue(urlCf);
        if (publishingDate == null || StringUtils.isEmpty(url))
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.emptyFieldsError"));

        try {
            Double time = getTime(AbstractFunctionFactory.getFilter(url), publishingDate, numberOfDays, counterConfig.getRatingId(), StringUtils.trimToEmpty(counterConfig.getRatingPassword()));
            issue.setCustomFieldValue(timeCf, time);
        } catch (Exception e) {
            AbstractFunctionFactory.sendErrorEmail("ru.mail.jira.plugins.contentprojects.issue.functions.counterError", counter.getName(), issue, Arrays.asList(urlCf, publishingDateCf));
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.counterError", counter.getName()), e);
        }
    }
}
