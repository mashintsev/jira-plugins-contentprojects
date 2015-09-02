package ru.mail.jira.plugins.contentprojects.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.validator.AbstractPermissionValidator;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.InvalidInputException;
import com.opensymphony.workflow.WorkflowException;
import org.apache.log4j.Logger;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import ru.mail.jira.plugins.contentprojects.configuration.PluginData;

import java.util.Map;

public class PaymentMonthIsOpenValidator extends AbstractPermissionValidator {
    private static final Logger log = Logger.getLogger(PaymentMonthIsOpenValidator.class);
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PluginData pluginData;

    public PaymentMonthIsOpenValidator(JiraAuthenticationContext jiraAuthenticationContext, PluginData pluginData) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.pluginData = pluginData;
    }

    @Override
    public void validate(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        try {
            Issue issue = (Issue) transientVars.get("issue");
            CustomField customField = CommonUtils.getCustomField(Consts.PAYMENT_MONTH_CF_ID);
            Option option = (Option) issue.getCustomFieldValue(customField);
            if (option != null && pluginData.isActCreated(issue.getProjectObject(), option))
                throw new InvalidInputException(customField.getId(), jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.paymentMonthIsOpen.error"));
        } catch (WorkflowException e) {
            throw e;
        } catch (Exception e) {
            log.error(e);
            throw new WorkflowException(e);
        }
    }
}
