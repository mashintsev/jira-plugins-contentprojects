package ru.mail.jira.plugins.contentprojects.gadgets;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.permission.ProjectPermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.velocity.NumberTool;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.RestExecutor;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import ru.mail.jira.plugins.contentprojects.configuration.PluginData;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@Produces({ MediaType.APPLICATION_JSON })
@Path("/remainingBudget")
public class RemainingBudgetResource {
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PermissionManager permissionManager;
    private final PluginData pluginData;
    private final ProjectManager projectManager;
    private final SearchProvider searchProvider;
    private final SearchService searchService;

    public RemainingBudgetResource(JiraAuthenticationContext jiraAuthenticationContext, PermissionManager permissionManager, PluginData pluginData, ProjectManager projectManager, SearchProvider searchProvider, SearchService searchService) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.permissionManager = permissionManager;
        this.pluginData = pluginData;
        this.projectManager = projectManager;
        this.searchProvider = searchProvider;
        this.searchService = searchService;
    }

    public static List<Option> getActivePaymentMonthOptions() {
        FieldConfig fieldConfig = CommonUtils.getCustomField(Consts.PAYMENT_MONTH_CF_ID).getRelevantConfig(IssueContext.GLOBAL);
        Options options = ComponentAccessor.getOptionsManager().getOptions(fieldConfig);

        List<Option> result = new ArrayList<Option>();
        for (Option option : options.subList(1, options.size()))
            if (!option.getDisabled())
                result.add(option);
        return result;
    }

    private Query getQuery(Project project, List<Option> paymentMonths) {
        String[] values = new String[paymentMonths.size()];
        for (int i = 0; i < paymentMonths.size(); i++)
            values[i] = paymentMonths.get(i).getValue();
        return JqlQueryBuilder.newClauseBuilder().project(project.getId()).and().customField(Consts.PAYMENT_MONTH_CF_ID).in(values).buildQuery();
    }

    public String getIssueNavigatorUrl(Project project, Option paymentMonth) {
        Query query = JqlQueryBuilder.newClauseBuilder().project(project.getId()).and().customField(Consts.PAYMENT_MONTH_CF_ID).eq(paymentMonth.getValue()).buildQuery();
        return "/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(jiraAuthenticationContext.getUser().getDirectoryUser(), query);
    }

    @GET
    public Response getData(@QueryParam("projectId") final String projectId) {
        return new RestExecutor<Output>() {
            @Override
            protected Output doAction() throws SearchException {
                CustomField paymentMonthCf = CommonUtils.getCustomField(Consts.PAYMENT_MONTH_CF_ID);
                CustomField costCf = CommonUtils.getCustomField(Consts.COST_CF_ID);

                Project project = projectManager.getProjectObj(Long.valueOf(projectId.substring("project-".length())));
                if (!permissionManager.hasPermission(ProjectPermissions.ADMINISTER_PROJECTS, project, jiraAuthenticationContext.getUser()))
                    return new Output(project.getName(), paymentMonthCf.getName(), costCf.getName(), Collections.<Month>emptyList());

                class Value {
                    int issueCount;
                    double spent;
                    double planned;
                }

                List<Option> paymentMonths = getActivePaymentMonthOptions();
                List<Issue> issues = searchProvider.search(getQuery(project, paymentMonths), jiraAuthenticationContext.getUser(), PagerFilter.getUnlimitedFilter()).getIssues();

                Map<Option, Value> values = new HashMap<Option, Value>();
                for (Issue issue : issues) {
                    String statusId = issue.getStatusObject().getId();
                    Option paymentMonth = (Option) issue.getCustomFieldValue(paymentMonthCf);
                    Double cost = (Double) issue.getCustomFieldValue(costCf);
                    if (cost != null) {
                        Value value = values.get(paymentMonth);
                        if (value == null) {
                            value = new Value();
                            values.put(paymentMonth, value);
                        }

                        value.issueCount++;
                        if (Consts.STATUS_SPENT_IDS.contains(statusId))
                            value.spent += cost;
                        if (Consts.STATUS_PLANNED_IDS.contains(statusId))
                            value.planned += cost;
                    }
                }

                NumberTool numberTool = new NumberTool(jiraAuthenticationContext.getLocale());
                List<Month> months = new ArrayList<Month>();
                for (Option paymentMonth : paymentMonths) {
                    Integer budget = pluginData.getBudgetValue(project, paymentMonth);
                    int additionalCost = pluginData.getBudgetCost(project, paymentMonth);
                    Value value = values.get(paymentMonth);
                    if (budget == null || value == null)
                        continue;
                    months.add(new Month(paymentMonth.getValue(), getIssueNavigatorUrl(project, paymentMonth), value.issueCount, budget, value.spent + additionalCost, value.planned, numberTool));
                }
                return new Output(project.getName(), paymentMonthCf.getName(), costCf.getName(), months);
            }
        }.getResponse();
    }

    @SuppressWarnings("UnusedDeclaration")
    @XmlRootElement
    public static class Month {
        @XmlElement
        private String title;
        @XmlElement
        private String url;
        @XmlElement
        private int issueCount;
        @XmlElement
        private String budget;
        @XmlElement
        private String spent;
        @XmlElement
        private String planned;
        @XmlElement
        private String left;
        @XmlElement
        private double budgetWidth;
        @XmlElement
        private double spentWidth;
        @XmlElement
        private double plannedWidth;
        @XmlElement
        private double leftWidth;

        private Month() {
        }

        public Month(String title, String url, int issueCount, double budget, double spent, double planned, NumberTool numberTool) {
            this.title = title;
            this.url = url;
            this.issueCount = issueCount;

            this.budget = numberTool.format("0.00", budget);
            this.spent = numberTool.format("0.00", spent);
            this.planned = numberTool.format("0.00", planned);
            this.left = numberTool.format("0.00", budget - spent - planned);

            double max = Math.max(budget, spent + planned) / 100;
            this.budgetWidth = budget / max;
            this.spentWidth = spent / max;
            this.plannedWidth = planned / max;
            this.leftWidth = Math.abs(budget - spent - planned) / max;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @XmlRootElement
    public static class Output {
        @XmlElement
        private String projectName;
        @XmlElement
        private String paymentMonthCaption;
        @XmlElement
        private String costCaption;
        @XmlElement
        private List<Month> months;

        private Output() {
        }

        public Output(String projectName, String paymentMonthCaption, String costCaption, List<Month> months) {
            this.projectName = projectName;
            this.paymentMonthCaption = paymentMonthCaption;
            this.costCaption = costCaption;
            this.months = months;
        }
    }
}
