package ru.mail.jira.plugins.contentprojects.authors.freelancers;

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.RestExecutor;
import ru.mail.jira.plugins.commons.RestFieldException;
import ru.mail.jira.plugins.contentprojects.common.Consts;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Path("/freelancer")
@Produces({MediaType.APPLICATION_JSON})
public class ContentProjectsFreelancersAction extends JiraWebActionSupport {
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    private final FreelancerManager freelancerManager;

    public ContentProjectsFreelancersAction(FreelancerManager freelancerManager) {
        this.freelancerManager = freelancerManager;
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
    protected String doExecute() throws Exception {
        if (!isUserAllowed())
            return sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return SUCCESS;
    }

    @SuppressWarnings("unused")
    public Freelancer[] getFreelancers() {
        return freelancerManager.getFreelancers();
    }

    @SuppressWarnings("unused")
    public String formatDate(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    @GET
    @Path("{id}")
    public Response getFreelancer(@PathParam("id") final int id) {
        return new RestExecutor<FreelancerOutput>() {
            @Override
            protected FreelancerOutput doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                return new FreelancerOutput(freelancerManager.getFreelancer(id));
            }
        }.getResponse();
    }

    private Date parseContractDate(String contractDate) {
        try {
            if (StringUtils.isBlank(contractDate))
                return null;
            return new SimpleDateFormat(DATE_FORMAT).parse(contractDate.trim());
        } catch (ParseException e) {
            throw new RestFieldException(getText("ru.mail.jira.plugins.contentprojects.authors.freelancers.error.invalidContractDateFormat"), "contract-date");
        }
    }

    private ContractType parseContractType(String contractType) {
        try {
            return ContractType.valueOf(contractType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @RequiresXsrfCheck
    @POST
    public Response createFreelancer(@FormParam("fullName") final String fullName,
                                     @FormParam("payeeName") final String payeeName,
                                     @FormParam("contractDate") final String contractDate,
                                     @FormParam("type") final String contractType,
                                     @FormParam("inn") final String inn,
                                     @FormParam("snils") final String snils) {
        return new RestExecutor<Integer>() {
            @Override
            protected Integer doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                return freelancerManager.createFreelancer(StringUtils.trimToNull(fullName), StringUtils.trimToNull(payeeName), parseContractDate(contractDate), parseContractType(contractType), StringUtils.trimToNull(inn), StringUtils.trimToNull(snils));
            }
        }.getResponse();
    }

    @RequiresXsrfCheck
    @PUT
    @Path("{id}")
    public Response updateFreelancer(@PathParam("id") final int id,
                                     @FormParam("fullName") final String fullName,
                                     @FormParam("payeeName") final String payeeName,
                                     @FormParam("contractDate") final String contractDate,
                                     @FormParam("type") final String contractType,
                                     @FormParam("inn") final String inn,
                                     @FormParam("snils") final String snils) {
        return new RestExecutor<Void>() {
            @Override
            protected Void doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                freelancerManager.updateFreelancer(id, StringUtils.trimToNull(fullName), StringUtils.trimToNull(payeeName), parseContractDate(contractDate), parseContractType(contractType), StringUtils.trimToNull(inn), StringUtils.trimToNull(snils));
                return null;
            }
        }.getResponse();
    }

    @RequiresXsrfCheck
    @DELETE
    @Path("{id}")
    public Response deleteFreelancer(@PathParam("id") final int id) {
        return new RestExecutor<Void>() {
            @Override
            protected Void doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                freelancerManager.deleteFreelancer(id);
                return null;
            }
        }.getResponse();
    }
}
