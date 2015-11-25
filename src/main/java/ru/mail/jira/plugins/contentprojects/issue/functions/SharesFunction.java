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
import ru.mail.jira.plugins.commons.RestExecutor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Produces({ MediaType.APPLICATION_JSON })
@Path("/collectStatistics")
public class SharesFunction extends AbstractJiraFunctionProvider {
    private final JiraAuthenticationContext jiraAuthenticationContext;

    public SharesFunction(JiraAuthenticationContext jiraAuthenticationContext) {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    private int getSharesFacebook(String ... urls) throws Exception {
        String response = new HttpSender("http://api.facebook.com/restserver.php?method=links.getStats&format=json&urls=%s", StringUtils.join(urls, ",")).sendGet();
        JSONArray json = new JSONArray(response);
        int result = 0;
        for (int i = 0; i < json.length(); i++)
            result += json.getJSONObject(i).getInt("share_count");
        return result;
    }

    private int getSharesMymail(String ... urls) throws Exception {
        String response = new HttpSender("https://connect.mail.ru/share_count?url_list=%s", StringUtils.join(urls, ",")).sendGet();
        JSONObject json = new JSONObject(response);
        int result = 0;
        Iterator<String> iterator = json.keys();
        while (iterator.hasNext())
            result += json.getJSONObject(iterator.next()).getInt("shares");
        return result;
    }

    private int getSharesOdnoklassniki(String url) throws Exception {
        String response = new HttpSender("http://connect.ok.ru/dk/?st.cmd=extLike&ref=%s&tp=json", url).sendGet();
        JSONObject json = new JSONObject(response);
        return json.getInt("count");
    }

    private int getSharesTwitter(String url) throws Exception {
        String response = new HttpSender("https://cdn.api.twitter.com/1/urls/count.json?url=%s", url).sendGet();
        JSONObject json = new JSONObject(response);
        return json.getInt("count");
    }

    private int getSharesVkontakte(String url) throws Exception {
        String response = new HttpSender("https://vk.com/share.php?url=%s&act=count", url).sendGet();
        Matcher matcher = Pattern.compile("VK\\.Share\\.count\\((\\d+), (\\d+)\\);").matcher(response);
        if (!matcher.matches())
            throw new IllegalArgumentException("Response doesn't match the pattern");
        return Integer.parseInt(matcher.group(2));
    }

    private int[] getShares(String url) throws Exception {
        String separator = url.contains("?") ? "&" : "?";
        int facebook = getSharesFacebook(url);
        int mymail = getSharesMymail(url, url + separator + "social=my");
        int odnoklassniki = getSharesOdnoklassniki(url) + getSharesOdnoklassniki(url + separator + "social=ok");
        int twitter = 0;
        int vkontakte = 0;
        int countVk = 0;
        while (countVk < 2)
            try {
                countVk++;
                vkontakte = getSharesVkontakte(url) + getSharesVkontakte(url + separator + "social=vk");
                break;
            } catch (ConnectException e) {
                if (countVk < 2)
                    continue;
                throw new Exception(e.getMessage(), e);
            }
        return new int[] { facebook, mymail, odnoklassniki, twitter, vkontakte };
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        MutableIssue issue = getIssue(transientVars);

        CustomField facebookCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.FACEBOOK_FIELD));
        CustomField myMailCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.MY_MAIL_FIELD));
        CustomField odnoklassnikiCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.ODNOKLASSNIKI_FIELD));
        CustomField twitterCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.TWITTER_FIELD));
        CustomField vkontakteCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.VKONTAKTE_FIELD));
        CustomField urlCf = CommonUtils.getCustomField((String) args.get(AbstractFunctionFactory.URL_FIELD));

        String url = (String) issue.getCustomFieldValue(urlCf);
        if (StringUtils.isEmpty(url))
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.emptyFieldsError"));

        try {
            int[] shares = getShares(url);
            issue.setCustomFieldValue(facebookCf, (double) shares[0]);
            issue.setCustomFieldValue(myMailCf, (double) shares[1]);
            issue.setCustomFieldValue(odnoklassnikiCf, (double) shares[2]);
            issue.setCustomFieldValue(twitterCf, null);
            issue.setCustomFieldValue(vkontakteCf, (double) shares[4]);
        } catch (Exception e) {
            throw new WorkflowException(jiraAuthenticationContext.getI18nHelper().getText("ru.mail.jira.plugins.contentprojects.issue.functions.sharesError"), e);
        }
    }

    @GET
    @Path("/shares")
    public Response getSharesJson(@QueryParam("url") final String url) {
        return new RestExecutor<SharesOutput>() {
            @Override
            protected SharesOutput doAction() throws Exception {
                return new SharesOutput(getShares(url));
            }
        }.getResponse();
    }

    @SuppressWarnings("UnusedDeclaration")
    @XmlRootElement
    public static class SharesOutput {
        @XmlElement
        private double total;
        @XmlElement
        private double facebook;
        @XmlElement
        private double mymail;
        @XmlElement
        private double odnoklassniki;
        @XmlElement
        private double twitter;
        @XmlElement
        private double vkontakte;

        private SharesOutput() {
        }

        public SharesOutput(int[] shares) {
            this.total = shares[0] + shares[1] + shares[2] + shares[3] + shares[4];
            this.facebook = shares[0];
            this.mymail = shares[1];
            this.odnoklassniki = shares[2];
            this.twitter = shares[3];
            this.vkontakte = shares[4];
        }
    }
}
