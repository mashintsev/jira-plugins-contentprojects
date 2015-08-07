package ru.mail.jira.plugins.contentprojects.configuration;

import com.atlassian.jira.permission.GlobalPermissionKey;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.RestExecutor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Path("/counter")
@Produces({MediaType.APPLICATION_JSON})
public class CounterRest {
    private final CounterManager counterManager;
    private final GlobalPermissionManager globalPermissionManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public CounterRest(CounterManager counterManager, GlobalPermissionManager globalPermissionManager, JiraAuthenticationContext jiraAuthenticationContext) {
        this.counterManager = counterManager;
        this.globalPermissionManager = globalPermissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    private boolean isUserAllowed() {
        return globalPermissionManager.hasPermission(GlobalPermissionKey.ADMINISTER, jiraAuthenticationContext.getUser());
    }

    @GET
    public Response getCounters() {
        return new RestExecutor<CountersOutput>() {
            @Override
            protected CountersOutput doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                return new CountersOutput(counterManager.getCounters());
            }
        }.getResponse();
    }

    @POST
    public Response createCounter(@FormParam("name") final String name) {
        return new RestExecutor<Integer>() {
            @Override
            protected Integer doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                return counterManager.createCounter(StringUtils.trimToNull(name));
            }
        }.getResponse();
    }

    @PUT
    @Path("{id}")
    public Response updateCounter(@PathParam("id") final int id,
                                  @FormParam("name") final String name) {
        return new RestExecutor<Void>() {
            @Override
            protected Void doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                counterManager.updateCounter(id, StringUtils.trimToNull(name));
                return null;
            }
        }.getResponse();
    }

    @DELETE
    @Path("{id}")
    public Response deleteCounter(@PathParam("id") final int id) {
        return new RestExecutor<Void>() {
            @Override
            protected Void doAction() throws Exception {
                if (!isUserAllowed())
                    throw new SecurityException();
                counterManager.deleteCounter(id);
                return null;
            }
        }.getResponse();
    }

    @SuppressWarnings({"UnusedDeclaration", "MismatchedQueryAndUpdateOfCollection"})
    @XmlRootElement
    public static class CountersOutput {
        @XmlElement
        private List<CounterOutput> counters = new ArrayList<CounterOutput>();

        private CountersOutput() {
        }

        public CountersOutput(Counter[] counters) {
            for (Counter counter : counters)
                this.counters.add(new CounterOutput(counter));
        }
    }

    @SuppressWarnings({"UnusedDeclaration", "MismatchedQueryAndUpdateOfCollection"})
    @XmlRootElement
    public static class CounterOutput {
        @XmlElement
        private int id;
        @XmlElement
        private String name;
        @XmlElement
        private List<CounterConfigOutput> counterConfigs = new ArrayList<CounterConfigOutput>();

        private CounterOutput() {
        }

        public CounterOutput(Counter counter) {
            this.id = counter.getID();
            this.name = counter.getName();
            for (CounterConfig counterConfig : counter.getCounterConfigs())
                this.counterConfigs.add(new CounterConfigOutput(counterConfig));
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @XmlRootElement
    public static class CounterConfigOutput {
        @XmlElement
        private long projectId;
        @XmlElement
        private int ratingId;
        @XmlElement
        private String ratingPassword;

        private CounterConfigOutput() {
        }

        public CounterConfigOutput(CounterConfig counterConfig) {
            this.projectId = counterConfig.getProjectId();
            this.ratingId = counterConfig.getRatingId();
            this.ratingPassword = counterConfig.getRatingPassword();
        }
    }
}
