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

public class HitSourcesFunction extends AbstractJiraFunctionProvider {
    private final CounterManager counterManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public HitSourcesFunction(CounterManager counterManager, JiraAuthenticationContext jiraAuthenticationContext) {
        this.counterManager = counterManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    private int[] getSocialMediaHits(String filter, Date publishingDate, int numberOfDays, int counterId, String counterPassword) throws Exception {
        int[] result = {0, 0, 0, 0, 0};

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(publishingDate);
        for (int day = 0; day < numberOfDays; day++) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);

            String response = new HttpSender("http://top.mail.ru/json/soclanding?rettype=all&id=%s&password=%s&period=0&date=%s&filter_type=0&filter=%s", counterId, counterPassword, date, filter).sendGet();
            JSONObject json = new JSONObject(response);
            JSONArray elements = json.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                if (AbstractFunctionFactory.getFilter(element.getString("url")).equals(filter)) {
                    String sign = element.getString("sign");
                    int value = element.getInt("value");
                    if ("facebook".equals(sign))
                        result[0] += value;
                    else if ("mymail".equals(sign))
                        result[1] += value;
                    else if ("odnoklassniki".equals(sign))
                        result[2] += value;
                    else if ("twitter".equals(sign))
                        result[3] += value;
                    else if ("vkontakte".equals(sign))
                        result[4] += value;
                }
            }
        }

        return result;
    }

    private int[] getSearchEngineHits(String filter, Date publishingDate, int numberOfDays, int counterId, String counterPassword) throws Exception {
        int[] result = {0, 0, 0};

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(publishingDate);
        for (int day = 0; day < numberOfDays; day++) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);

            String response = new HttpSender("http://top.mail.ru/json/srchlanding?rettype=all&id=%s&password=%s&period=0&date=%s&filter_type=0&filter=%s", counterId, counterPassword, date, filter).sendGet();
            JSONObject json = new JSONObject(response);
            JSONArray elements = json.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                if (AbstractFunctionFactory.getFilter(element.getString("url")).equals(filter)) {
                    String sign = element.getString("sign");
                    int value = element.getInt("value");
                    if ("google".equals(sign))
                        result[0] += value;
                    else if ("yandex".equals(sign))
                        result[1] += value;
                    else
                        result[2] += value;
                }
            }
        }

        return result;
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);

        CustomField publishingDateCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.PUBLISHING_DATE_FIELD));
        CustomField facebookCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.FACEBOOK_FIELD));
        CustomField myMailCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.MY_MAIL_FIELD));
        CustomField odnoklassnikiCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.ODNOKLASSNIKI_FIELD));
        CustomField twitterCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.TWITTER_FIELD));
        CustomField vkontakteCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.VKONTAKTE_FIELD));
        CustomField googleCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.GOOGLE_FIELD));
        CustomField yandexCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.YANDEX_FIELD));
        CustomField otherSearchEnginesCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.OTHER_SEARCH_ENGINES_FIELD));
        CustomField urlCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.URL_FIELD));
        Counter counter = counterManager.getCounter(Integer.parseInt((String) args.get(AbstractFunctionFactory.COUNTER)));
        int numberOfDays = Integer.parseInt((String) args.get(AbstractFunctionFactory.NUMBER_OF_DAYS));

        CounterConfig counterConfig = counterManager.getCounterConfig(counter, issue.getProjectObject());
        if (counterConfig == null || counterConfig.getRatingId() == null)
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.notConfiguredCounterError", counter.getName()));

        Date publishingDate = (Date) issue.getCustomFieldValue(publishingDateCf);
        String url = (String) issue.getCustomFieldValue(urlCf);
        if (publishingDate == null || StringUtils.isEmpty(url))
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.emptyFieldsError"));

        try {
            int[] socialMediaHits = getSocialMediaHits(AbstractFunctionFactory.getFilter(url), publishingDate, numberOfDays, counterConfig.getRatingId(), StringUtils.trimToEmpty(counterConfig.getRatingPassword()));
            int[] searchEngineHits = getSearchEngineHits(AbstractFunctionFactory.getFilter(url), publishingDate, numberOfDays, counterConfig.getRatingId(), StringUtils.trimToEmpty(counterConfig.getRatingPassword()));
            issue.setCustomFieldValue(facebookCf, (double) socialMediaHits[0]);
            issue.setCustomFieldValue(myMailCf, (double) socialMediaHits[1]);
            issue.setCustomFieldValue(odnoklassnikiCf, (double) socialMediaHits[2]);
            issue.setCustomFieldValue(twitterCf, (double) socialMediaHits[3]);
            issue.setCustomFieldValue(vkontakteCf, (double) socialMediaHits[4]);
            issue.setCustomFieldValue(googleCf, (double) searchEngineHits[0]);
            issue.setCustomFieldValue(yandexCf, (double) searchEngineHits[1]);
            issue.setCustomFieldValue(otherSearchEnginesCf, (double) searchEngineHits[2]);
        } catch (Exception e) {
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.counterError", counter.getName()), e);
        }
    }
}
