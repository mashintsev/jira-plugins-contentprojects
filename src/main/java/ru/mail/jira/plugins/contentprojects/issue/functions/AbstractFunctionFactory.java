package ru.mail.jira.plugins.contentprojects.issue.functions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import ru.mail.jira.plugins.contentprojects.configuration.CounterManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class AbstractFunctionFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {
    public static final String CUSTOM_FIELDS = "customFields";
    public static final String COUNTERS = "counters";

    public static final String CATEGORY_FIELD = "categoryField";
    public static final String PUBLISHING_DATE_FIELD = "publishingDateField";
    public static final String ESTIMATED_TIME_FIELD = "estimatedTimeField";
    public static final String COMMENTS_FIELD = "commentsField";
    public static final String HITS_FIELD = "hitsField";
    public static final String FACEBOOK_FIELD = "facebookField";
    public static final String MY_MAIL_FIELD = "myMailField";
    public static final String ODNOKLASSNIKI_FIELD = "odnoklassnikiField";
    public static final String TWITTER_FIELD = "twitterField";
    public static final String VKONTAKTE_FIELD = "vkontakteField";
    public static final String GOOGLE_FIELD = "googleField";
    public static final String YANDEX_FIELD = "yandexField";
    public static final String OTHER_SEARCH_ENGINES_FIELD = "otherSearchEnginesField";
    public static final String TIME_FIELD = "timeField";
    public static final String URL_FIELD = "urlField";
    public static final String COUNTER = "counter";
    public static final String NUMBER_OF_DAYS = "numberOfDays";

    private final CounterManager counterManager;
    private final CustomFieldManager customFieldManager;

    public static String getFilter(String url) throws MalformedURLException {
        return new URL(url).getPath().replaceAll("/$", "");
    }

    public static Double round(Double a) {
        if (a == null)
            return null;
        return Math.round(a * 100) / 100.0;
    }

    public static void sendErrorEmail(String problemI18nKey, String counterName, Issue issue, List<CustomField> customFields) {
        ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
        UserManager userManager = ComponentAccessor.getUserManager();

        List<String> recipientKeys = new ArrayList<String>(Consts.NOTIFICATION_USER_KEYS);
        recipientKeys.add(issue.getProjectObject().getProjectLead().getKey());
        String issueUrl = applicationProperties.getString(APKeys.JIRA_BASEURL) + "/browse/" + issue.getKey();

        for (String recipientKey : recipientKeys) {
            ApplicationUser recipient = userManager.getUserByKey(recipientKey);
            if (recipient == null)
                continue;

            I18nHelper i18nHelper = ComponentAccessor.getI18nHelperFactory().getInstance(recipient);
            String problem = counterName == null ? i18nHelper.getText(problemI18nKey) : i18nHelper.getText(problemI18nKey, counterName);
            StringBuilder body = new StringBuilder();
            body.append(problem).append("\n");
            body.append(i18nHelper.getText("common.words.issue")).append(": ").append(issueUrl).append("\n");
            for (CustomField customField : customFields)
                body.append(customField.getFieldName()).append(": ").append(customField.getValue(issue)).append("\n");

            CommonUtils.sendEmail(recipient, i18nHelper.getText("ru.mail.jira.plugins.contentprojects.issue.functions.errorMailSubject"), body.toString());
        }
    }

    public AbstractFunctionFactory(CounterManager counterManager, CustomFieldManager customFieldManager) {
        this.counterManager = counterManager;
        this.customFieldManager = customFieldManager;
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        velocityParams.put(CUSTOM_FIELDS, customFieldManager.getCustomFieldObjects());
        velocityParams.put(COUNTERS, counterManager.getCounters());
    }

    private void getVelocityParamsFromDescriptor(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        if (!(descriptor instanceof FunctionDescriptor))
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        velocityParams.put(CATEGORY_FIELD, functionDescriptor.getArgs().get(CATEGORY_FIELD));
        velocityParams.put(PUBLISHING_DATE_FIELD, functionDescriptor.getArgs().get(PUBLISHING_DATE_FIELD));
        velocityParams.put(ESTIMATED_TIME_FIELD, functionDescriptor.getArgs().get(ESTIMATED_TIME_FIELD));
        velocityParams.put(COMMENTS_FIELD, functionDescriptor.getArgs().get(COMMENTS_FIELD));
        velocityParams.put(HITS_FIELD, functionDescriptor.getArgs().get(HITS_FIELD));
        velocityParams.put(FACEBOOK_FIELD, functionDescriptor.getArgs().get(FACEBOOK_FIELD));
        velocityParams.put(MY_MAIL_FIELD, functionDescriptor.getArgs().get(MY_MAIL_FIELD));
        velocityParams.put(ODNOKLASSNIKI_FIELD, functionDescriptor.getArgs().get(ODNOKLASSNIKI_FIELD));
        velocityParams.put(TWITTER_FIELD, functionDescriptor.getArgs().get(TWITTER_FIELD));
        velocityParams.put(VKONTAKTE_FIELD, functionDescriptor.getArgs().get(VKONTAKTE_FIELD));
        velocityParams.put(GOOGLE_FIELD, functionDescriptor.getArgs().get(GOOGLE_FIELD));
        velocityParams.put(YANDEX_FIELD, functionDescriptor.getArgs().get(YANDEX_FIELD));
        velocityParams.put(OTHER_SEARCH_ENGINES_FIELD, functionDescriptor.getArgs().get(OTHER_SEARCH_ENGINES_FIELD));
        velocityParams.put(TIME_FIELD, functionDescriptor.getArgs().get(TIME_FIELD));
        velocityParams.put(URL_FIELD, functionDescriptor.getArgs().get(URL_FIELD));
        velocityParams.put(COUNTER, functionDescriptor.getArgs().get(COUNTER));
        velocityParams.put(NUMBER_OF_DAYS, functionDescriptor.getArgs().get(NUMBER_OF_DAYS));
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsForInput(velocityParams);
        getVelocityParamsFromDescriptor(velocityParams, descriptor);
    }

    private String getCustomFieldName(String id) {
        CustomField customField = customFieldManager.getCustomFieldObject(id);
        return customField != null ? customField.getName() : id;
    }

    private String getCounterName(String id) {
        try {
            return counterManager.getCounter(Integer.parseInt(id)).getName();
        } catch (IllegalArgumentException e) {
            return id;
        }
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsFromDescriptor(velocityParams, descriptor);
        velocityParams.put(CATEGORY_FIELD, getCustomFieldName((String) velocityParams.get(CATEGORY_FIELD)));
        velocityParams.put(PUBLISHING_DATE_FIELD, getCustomFieldName((String) velocityParams.get(PUBLISHING_DATE_FIELD)));
        velocityParams.put(ESTIMATED_TIME_FIELD, getCustomFieldName((String) velocityParams.get(ESTIMATED_TIME_FIELD)));
        velocityParams.put(COMMENTS_FIELD, getCustomFieldName((String) velocityParams.get(COMMENTS_FIELD)));
        velocityParams.put(HITS_FIELD, getCustomFieldName((String) velocityParams.get(HITS_FIELD)));
        velocityParams.put(FACEBOOK_FIELD, getCustomFieldName((String) velocityParams.get(FACEBOOK_FIELD)));
        velocityParams.put(MY_MAIL_FIELD, getCustomFieldName((String) velocityParams.get(MY_MAIL_FIELD)));
        velocityParams.put(ODNOKLASSNIKI_FIELD, getCustomFieldName((String) velocityParams.get(ODNOKLASSNIKI_FIELD)));
        velocityParams.put(TWITTER_FIELD, getCustomFieldName((String) velocityParams.get(TWITTER_FIELD)));
        velocityParams.put(VKONTAKTE_FIELD, getCustomFieldName((String) velocityParams.get(VKONTAKTE_FIELD)));
        velocityParams.put(GOOGLE_FIELD, getCustomFieldName((String) velocityParams.get(GOOGLE_FIELD)));
        velocityParams.put(YANDEX_FIELD, getCustomFieldName((String) velocityParams.get(YANDEX_FIELD)));
        velocityParams.put(OTHER_SEARCH_ENGINES_FIELD, getCustomFieldName((String) velocityParams.get(OTHER_SEARCH_ENGINES_FIELD)));
        velocityParams.put(TIME_FIELD, getCustomFieldName((String) velocityParams.get(TIME_FIELD)));
        velocityParams.put(URL_FIELD, getCustomFieldName((String) velocityParams.get(URL_FIELD)));
        velocityParams.put(COUNTER, getCounterName((String) velocityParams.get(COUNTER)));
    }

    private void addStringParam(Map<String, Object> descriptorParams, Map<String, Object> formParams, String paramName) {
        try {
            descriptorParams.put(paramName, extractSingleParam(formParams, paramName));
        } catch (Exception ignored) {
        }
    }

    private void addIntegerParam(Map<String, Object> descriptorParams, Map<String, Object> formParams, String paramName, int min, int max) {
        try {
            descriptorParams.put(paramName, Math.max(min, Math.min(max, Integer.parseInt(extractSingleParam(formParams, paramName)))));
        } catch (Exception ignored) {
        }
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
        Map<String, Object> descriptorParams = new HashMap<String, Object>();
        addStringParam(descriptorParams, formParams, CATEGORY_FIELD);
        addStringParam(descriptorParams, formParams, PUBLISHING_DATE_FIELD);
        addStringParam(descriptorParams, formParams, ESTIMATED_TIME_FIELD);
        addStringParam(descriptorParams, formParams, COMMENTS_FIELD);
        addStringParam(descriptorParams, formParams, HITS_FIELD);
        addStringParam(descriptorParams, formParams, FACEBOOK_FIELD);
        addStringParam(descriptorParams, formParams, MY_MAIL_FIELD);
        addStringParam(descriptorParams, formParams, ODNOKLASSNIKI_FIELD);
        addStringParam(descriptorParams, formParams, TWITTER_FIELD);
        addStringParam(descriptorParams, formParams, VKONTAKTE_FIELD);
        addStringParam(descriptorParams, formParams, GOOGLE_FIELD);
        addStringParam(descriptorParams, formParams, YANDEX_FIELD);
        addStringParam(descriptorParams, formParams, OTHER_SEARCH_ENGINES_FIELD);
        addStringParam(descriptorParams, formParams, TIME_FIELD);
        addStringParam(descriptorParams, formParams, URL_FIELD);
        addStringParam(descriptorParams, formParams, COUNTER);
        addIntegerParam(descriptorParams, formParams, NUMBER_OF_DAYS, 1, 30);
        return descriptorParams;
    }
}
