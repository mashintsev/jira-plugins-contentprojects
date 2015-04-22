package ru.mail.jira.plugins.contentprojects.extras;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.LocalUtils;
import ru.mail.jira.plugins.contentprojects.authors.Author;
import ru.mail.jira.plugins.contentprojects.authors.FreelancerAuthor;
import ru.mail.jira.plugins.contentprojects.authors.freelancers.Freelancer;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import ru.mail.jira.plugins.contentprojects.configuration.PluginData;
import ru.mail.jira.plugins.contentprojects.gadgets.RemainingBudgetResource;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ContentProjectsCreateActsAction extends JiraWebActionSupport {
    private static final String PAYMENT_ACT = "\u0410\u043A\u0442 \u043E\u043F\u043B\u0430\u0442\u044B";
    private final DateFormat OPTION_FORMAT = LocalUtils.updateMonthNames(new SimpleDateFormat("MMMM yyyy"), LocalUtils.MONTH_NAMES_NOMINATIVE);
    private final DateFormat DAY_OF_THE_MONTH_FORMAT = new SimpleDateFormat("dd");
    private final DateFormat MONTH_FORMAT = LocalUtils.updateMonthNames(new SimpleDateFormat("MMMM"), LocalUtils.MONTH_NAMES_GENITIVE);
    private final DateFormat YEAR_FORMAT = new SimpleDateFormat("yy");

    private final IssueLinkService issueLinkService;
    private final IssueService issueService;
    private final OptionsManager optionsManager;
    private final PluginData pluginData;
    private final ProjectManager projectManager;
    private final SearchProvider searchProvider;
    private final SearchService searchService;

    private long optionId;
    private long[] projectIds;

    private Option option;
    private Set<Project> projects;

    public ContentProjectsCreateActsAction(IssueLinkService issueLinkService, IssueService issueService, OptionsManager optionsManager, PluginData pluginData, ProjectManager projectManager, SearchProvider searchProvider, SearchService searchService) {
        this.issueLinkService = issueLinkService;
        this.issueService = issueService;
        this.optionsManager = optionsManager;
        this.pluginData = pluginData;
        this.projectManager = projectManager;
        this.searchProvider = searchProvider;
        this.searchService = searchService;
    }

    private boolean isUserAllowed() {
        return CommonUtils.isUserInGroups(getLoggedInApplicationUser(), Consts.ACCOUNTANTS_GROUPS);
    }

    private String sendError(int code) throws IOException {
        if (ServletActionContext.getResponse() != null)
            ServletActionContext.getResponse().sendError(code);
        return NONE;
    }

    @Override
    public String doDefault() throws Exception {
        if (!isUserAllowed())
            return sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return INPUT;
    }

    @Override
    protected void doValidation() {
        option = optionsManager.findByOptionId(optionId);
        if (option == null)
            addError("optionId", getText("issue.field.required", getPaymentMonthFieldName()));

        projects = new HashSet<Project>();
        if (projectIds != null)
            for (long projectId : projectIds)
                if (Consts.PROJECT_IDS.contains(projectId))
                    projects.add(projectManager.getProjectObj(projectId));
        if (projects.isEmpty())
            addError("projectIds", getText("issue.field.required", getText("common.concepts.projects")));
    }

    private Collection<Date> getPossibleDates() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(OPTION_FORMAT.parse(option.getValue()));

        Collection<Date> result = new ArrayList<Date>();
        for (int day = 15; day <= 31; day++) {
            calendar.set(Calendar.DAY_OF_MONTH, day);
            if (calendar.get(Calendar.DAY_OF_MONTH) != day)
                break;
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                continue;
            if (calendar.get(Calendar.MONTH) == Calendar.FEBRUARY && calendar.get(Calendar.DAY_OF_MONTH) == 23)
                continue;
            result.add(calendar.getTime());
        }
        return result;
    }

    private Collection<Pair<IssueService.CreateValidationResult, Collection<String>>> prepareIssues() throws Exception {
        CustomField paymentMonthCf = CommonUtils.getCustomField(Consts.PAYMENT_MONTH_CF_ID);
        CustomField costCf = CommonUtils.getCustomField(Consts.COST_CF_ID);
        CustomField textAuthorCf = CommonUtils.getCustomField(Consts.TEXT_AUTHOR_CF_ID);

        Collection<Date> possibleDates = getPossibleDates();
        Iterator<Date> possibleDatesIterator = possibleDates.iterator();

        Collection<Pair<IssueService.CreateValidationResult, Collection<String>>> result = new ArrayList<Pair<IssueService.CreateValidationResult, Collection<String>>>();
        for (Project project : projects) {
            Query query = JqlQueryBuilder.newClauseBuilder().project(project.getId()).buildQuery();
            SearchResults searchResults = searchProvider.search(query, getLoggedInApplicationUser(), PagerFilter.getUnlimitedFilter());

            class FreelancerData {
                Collection<String> issueKeys = new ArrayList<String>();
                Collection<String> issueSummaries = new ArrayList<String>();
                double totalCost = 0;
            }
            Map<Freelancer, FreelancerData> freelancerDataMap = new HashMap<Freelancer, FreelancerData>();
            for (Issue issue : searchResults.getIssues()) {
                if (!option.equals(issue.getCustomFieldValue(paymentMonthCf)))
                    continue;

                if (!Consts.STATUS_SPENT_IDS.contains(issue.getStatusObject().getId())) {
                    addErrorMessage(getText("ru.mail.jira.plugins.contentprojects.extras.createActs.error.illegalStatus", issue.getKey()));
                    continue;
                }

                Author author = (Author) issue.getCustomFieldValue(textAuthorCf);
                if (!(author instanceof FreelancerAuthor)) {
                    addErrorMessage(getText("ru.mail.jira.plugins.contentprojects.extras.createActs.error.illegalTextAuthor", issue.getKey()));
                    continue;
                }

                Freelancer freelancer = ((FreelancerAuthor) author).getFreelancer();
                FreelancerData freelancerData = freelancerDataMap.get(freelancer);
                if (freelancerData == null) {
                    freelancerData = new FreelancerData();
                    freelancerDataMap.put(freelancer, freelancerData);
                }
                freelancerData.issueKeys.add(issue.getKey());
                freelancerData.issueSummaries.add(issue.getSummary());
                freelancerData.totalCost += (Double) issue.getCustomFieldValue(costCf);
            }

            for (Map.Entry<Freelancer, FreelancerData> e : freelancerDataMap.entrySet()) {
                Date paymentActDate = possibleDatesIterator.next();
                if (!possibleDatesIterator.hasNext())
                    possibleDatesIterator = possibleDates.iterator();

                JSONObject json = new JSONObject();
                json.put("templateIds", Collections.<Object>singleton(Consts.PAYMENT_ACT_TYPICAL_CONTRACTS_TEMPLATE_ID));
                json.put("variableValues", Arrays.<Object>asList(
                        DAY_OF_THE_MONTH_FORMAT.format(e.getKey().getContractDate()),
                        MONTH_FORMAT.format(e.getKey().getContractDate()),
                        YEAR_FORMAT.format(e.getKey().getContractDate()),
                        DAY_OF_THE_MONTH_FORMAT.format(paymentActDate.getTime()),
                        MONTH_FORMAT.format(paymentActDate.getTime()),
                        YEAR_FORMAT.format(paymentActDate.getTime()),
                        e.getValue().issueSummaries.size(),
                        StringUtils.join(e.getValue().issueSummaries, "\n"),
                        String.format(new Locale("ru"), "%,d", (int) e.getValue().totalCost),
                        LocalUtils.numberToCaption((int) e.getValue().totalCost),
                        String.format(new Locale("ru"), "%02d", (int) (e.getValue().totalCost * 100 % 100)),
                        StringUtils.isNotEmpty(e.getKey().getPayeeName()) ? e.getKey().getPayeeName() : e.getKey().getFullName(),
                        project.getName().substring(4)
                ));

                IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
                issueInputParameters.setProjectId(Consts.PAYMENT_ACT_PROJECT_ID);
                issueInputParameters.setIssueTypeId(String.valueOf(Consts.PAYMENT_ACT_ISSUE_TYPE_ID));
                issueInputParameters.setReporterId(getLoggedInApplicationUser().getName());
                issueInputParameters.setAssigneeId(getLoggedInApplicationUser().getName());
                issueInputParameters.setSummary(String.format("%s, %s, %s", PAYMENT_ACT, e.getKey().getFullName(), option.getValue()));
                issueInputParameters.setDescription(PAYMENT_ACT);
                issueInputParameters.setComponentIds(Consts.PAYMENT_ACT_COMPONENT_VALUE);
                issueInputParameters.addCustomFieldValue(Consts.PAYMENT_ACT_LEGAL_ENTITY_CF_ID, Consts.PAYMENT_ACT_LEGAL_ENTITY_VALUE);
                issueInputParameters.addCustomFieldValue(Consts.PAYMENT_ACT_CONTRAGENT_CF_ID, Consts.PAYMENT_ACT_CONTRAGENT_VALUE);
                issueInputParameters.addCustomFieldValue(Consts.PAYMENT_ACT_TYPICAL_CONTRACTS_CF_ID, json.toString());
                issueInputParameters.addCustomFieldValue(Consts.PAYMENT_ACT_PROJECT_CF_ID, Consts.PAYMENT_ACT_PROJECT_VALUE_MAP.get(project.getId()));
                IssueService.CreateValidationResult createValidationResult = issueService.validateCreate(getLoggedInApplicationUser().getDirectoryUser(), issueInputParameters);

                if (createValidationResult.isValid()) {
                    result.add(Pair.of(createValidationResult, e.getValue().issueKeys));
                } else {
                    addErrorMessages(createValidationResult.getErrorCollection().getErrorMessages());
                    addErrorMessages(createValidationResult.getErrorCollection().getErrors().values());
                }
            }
        }
        return result;
    }

    @RequiresXsrfCheck
    @Override
    public String doExecute() throws Exception {
        if (!isUserAllowed())
            return sendError(HttpServletResponse.SC_UNAUTHORIZED);

        for (Project project : projects)
            if (pluginData.isActCreated(project, option))
                addError("project-" + project.getId(), getText("ru.mail.jira.plugins.contentprojects.extras.createActs.error.alreadyCreated", option.getValue(), project.getName()));
        if (hasAnyErrors())
            return INPUT;

        Collection<Pair<IssueService.CreateValidationResult, Collection<String>>> preparedIssues = prepareIssues();
        if (!hasAnyErrors() && preparedIssues.isEmpty())
            addErrorMessage(getText("issuenav.results.none.found"));
        if (hasAnyErrors())
            return INPUT;

        Collection<String> issueKeys = new ArrayList<String>(preparedIssues.size());
        for (Pair<IssueService.CreateValidationResult, Collection<String>> preparationResult : preparedIssues) {
            IssueService.IssueResult issueResult = issueService.create(getLoggedInApplicationUser().getDirectoryUser(), preparationResult.getLeft());
            if (issueResult.isValid()) {
                IssueLinkService.AddIssueLinkValidationResult addIssueLinkValidationResult = issueLinkService.validateAddIssueLinks(getLoggedInApplicationUser().getDirectoryUser(), issueResult.getIssue(), Consts.PAYMENT_ACT_LINK_TYPE, preparationResult.getRight());
                if (addIssueLinkValidationResult.isValid()) {
                    issueLinkService.addIssueLinks(getLoggedInApplicationUser().getDirectoryUser(), addIssueLinkValidationResult);
                } else {
                    addErrorMessages(addIssueLinkValidationResult.getErrorCollection().getErrorMessages());
                    addErrorMessages(addIssueLinkValidationResult.getErrorCollection().getErrors().values());
                }
                issueKeys.add(issueResult.getIssue().getKey());
            } else {
                addErrorMessages(issueResult.getErrorCollection().getErrorMessages());
                addErrorMessages(issueResult.getErrorCollection().getErrors().values());
            }
        }

        for (Project project : projects)
            pluginData.setActCreated(project, option, true);
        boolean found = false;
        for (Project project : getContentProjects())
            if (!pluginData.isActCreated(project, option)) {
                found = true;
                break;
            }
        if (!found)
            optionsManager.disableOption(option);

        if (hasAnyErrors())
            return INPUT;
        Query query = JqlQueryBuilder.newClauseBuilder().issue(issueKeys.toArray(new String[issueKeys.size()])).buildQuery();
        return forceRedirect("/secure/IssueNavigator.jspa?reset=true" + searchService.getQueryString(getLoggedInApplicationUser().getDirectoryUser(), query));
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getPaymentMonthFieldName() {
        return CommonUtils.getCustomField(Consts.PAYMENT_MONTH_CF_ID).getName();
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<Option> getPaymentMonthOptions() {
        return RemainingBudgetResource.getActivePaymentMonthOptions();
    }

    public Collection<Project> getContentProjects() {
        Collection<Project> result = new ArrayList<Project>(Consts.PROJECT_IDS.size());
        for (Long projectId : Consts.PROJECT_IDS)
            result.add(projectManager.getProjectObj(projectId));
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    public long getOptionId() {
        return optionId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setOptionId(long optionId) {
        this.optionId = optionId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public long[] getProjectIds() {
        return projectIds;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setProjectIds(long[] projectIds) {
        this.projectIds = projectIds;
    }
}
