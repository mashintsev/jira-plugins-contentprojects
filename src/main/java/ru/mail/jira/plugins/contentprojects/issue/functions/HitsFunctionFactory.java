package ru.mail.jira.plugins.contentprojects.issue.functions;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import ru.mail.jira.plugins.contentprojects.configuration.CounterManager;

import java.util.HashMap;
import java.util.Map;

public class HitsFunctionFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {
    public static final String COUNTERS = "counters";
    public static final String CUSTOM_FIELDS = "customFields";
    public static final String RESULT_FIELD = "resultField";
    public static final String COUNTER = "counter";
    public static final String URL_FIELD = "urlField";
    public static final String DATE_FIELD = "dateField";
    public static final String NUMBER_OF_DAYS = "numberOfDays";

    private final CounterManager counterManager;
    private final CustomFieldManager customFieldManager;

    public HitsFunctionFactory(CounterManager counterManager, CustomFieldManager customFieldManager) {
        this.counterManager = counterManager;
        this.customFieldManager = customFieldManager;
    }

    @Override
    protected void getVelocityParamsForInput(Map<String, Object> velocityParams) {
        velocityParams.put(COUNTERS, counterManager.getCounters());
        velocityParams.put(CUSTOM_FIELDS, customFieldManager.getCustomFieldObjects());
    }

    private void getVelocityParamsFromDescriptor(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        if (!(descriptor instanceof FunctionDescriptor))
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        velocityParams.put(RESULT_FIELD, functionDescriptor.getArgs().get(RESULT_FIELD));
        velocityParams.put(COUNTER, functionDescriptor.getArgs().get(COUNTER));
        velocityParams.put(URL_FIELD, functionDescriptor.getArgs().get(URL_FIELD));
        velocityParams.put(DATE_FIELD, functionDescriptor.getArgs().get(DATE_FIELD));
        velocityParams.put(NUMBER_OF_DAYS, functionDescriptor.getArgs().get(NUMBER_OF_DAYS));
    }

    @Override
    protected void getVelocityParamsForEdit(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsForInput(velocityParams);
        getVelocityParamsFromDescriptor(velocityParams, descriptor);
    }

    private String getCounterName(String id) {
        try {
            return counterManager.getCounter(Integer.parseInt(id)).getName();
        } catch (IllegalArgumentException e) {
            return id;
        }
    }

    private String getCustomFieldName(String id) {
        CustomField customField = customFieldManager.getCustomFieldObject(id);
        return customField != null ? customField.getName() : id;
    }

    @Override
    protected void getVelocityParamsForView(Map<String, Object> velocityParams, AbstractDescriptor descriptor) {
        getVelocityParamsFromDescriptor(velocityParams, descriptor);
        velocityParams.put(RESULT_FIELD, getCustomFieldName((String) velocityParams.get(RESULT_FIELD)));
        velocityParams.put(COUNTER, getCounterName((String) velocityParams.get(COUNTER)));
        velocityParams.put(URL_FIELD, getCustomFieldName((String) velocityParams.get(URL_FIELD)));
        velocityParams.put(DATE_FIELD, getCustomFieldName((String) velocityParams.get(DATE_FIELD)));
    }

    @Override
    public Map<String, ?> getDescriptorParams(Map<String, Object> formParams) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(RESULT_FIELD, extractSingleParam(formParams, RESULT_FIELD));
        result.put(COUNTER, extractSingleParam(formParams, COUNTER));
        result.put(URL_FIELD, extractSingleParam(formParams, URL_FIELD));
        result.put(DATE_FIELD, extractSingleParam(formParams, DATE_FIELD));
        result.put(NUMBER_OF_DAYS, Math.max(1, Math.min(30, Integer.parseInt(extractSingleParam(formParams, NUMBER_OF_DAYS)))));
        return result;
    }
}
