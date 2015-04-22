package ru.mail.jira.plugins.contentprojects.issue;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.commons.HttpSender;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Produces({ MediaType.APPLICATION_JSON })
@Path("/collectStatistics")
public class CollectStatisticsFunction extends AbstractJiraFunctionProvider {
    private static final int DAYS_COUNT = 7;

    private static final Logger log = Logger.getLogger(CollectStatisticsFunction.class);
    private final PluginData pluginData;

    public CollectStatisticsFunction(PluginData pluginData) {
        this.pluginData = pluginData;
    }

    private String getFilter(String url) throws MalformedURLException {
        return new URL(url).getPath().replaceAll("/$", "");
    }

    private int getHits(String filter, Date publishingDate, int counterId, String counterPassword) throws Exception {
        int result = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(publishingDate);
        for (int day = 0; day < DAYS_COUNT; day++) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);

            String response = new HttpSender("http://top.mail.ru/json/pages?id=%s&password=%s&period=0&date=%s&filter_type=0&filter=%s", counterId, counterPassword, date, filter).sendGet();
            JSONObject json = new JSONObject(response);
            JSONArray elements = json.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                if (getFilter(element.getString("url")).equals(filter))
                    result += element.getInt("value");
            }
        }

        return result;
    }

    private SearchEngines getHitsSearchEngines(String filter, Date publishingDate, int counterId, String counterPassword) throws Exception {
        int[] result = {0, 0, 0};

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(publishingDate);
        for (int day = 0; day < DAYS_COUNT; day++) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);

            String response = new HttpSender("http://top.mail.ru/json/srchlanding?rettype=all&id=%s&password=%s&period=0&date=%s&filter_type=0&filter=%s", counterId, counterPassword, date, filter).sendGet();
            JSONObject json = new JSONObject(response);
            JSONArray elements = json.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                if (getFilter(element.getString("url")).equals(filter)) {
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

        return new SearchEngines(result[0], result[1], result[2]);
    }

    private SocialMedia getHitsSocialMedia(String filter, Date publishingDate, int counterId, String counterPassword) throws Exception {
        int[] result = {0, 0, 0, 0, 0};

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(publishingDate);
        for (int day = 0; day < DAYS_COUNT; day++) {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            calendar.add(Calendar.DATE, 1);

            String response = new HttpSender("http://top.mail.ru/json/soclanding?rettype=all&id=%s&password=%s&period=0&date=%s&filter_type=0&filter=%s", counterId, counterPassword, date, filter).sendGet();
            JSONObject json = new JSONObject(response);
            JSONArray elements = json.getJSONArray("elements");
            for (int i = 0; i < elements.length(); i++) {
                JSONObject element = elements.getJSONObject(i);
                if (getFilter(element.getString("url")).equals(filter)) {
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

        return new SocialMedia(result[0], result[1], result[2], result[3], result[4]);
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

    private SocialMedia getShares(String url) throws Exception {
        String separator = url.contains("?") ? "&" : "?";
        int facebook = getSharesFacebook(url);
        int mymail = getSharesMymail(url, url + separator + "social=my");
        int odnoklassniki = getSharesOdnoklassniki(url) + getSharesOdnoklassniki(url + separator + "social=ok");
        int twitter = getSharesTwitter(url) + getSharesTwitter(url + separator + "social=tw");
        int vkontakte = getSharesVkontakte(url) + getSharesVkontakte(url + separator + "social=vk");
        return new SocialMedia(facebook, mymail, odnoklassniki, twitter, vkontakte);
    }

    private Integer[] getScrolls(String filter, Date publishingDate, Integer[] scrollCounterIds, String scrollCountersPassword) throws Exception {
        Integer[] result = new Integer[scrollCounterIds.length];
        for (int i = 0; i < result.length; i++)
            if (scrollCounterIds[i] != null && StringUtils.isNotEmpty(scrollCountersPassword))
                result[i] = getHits(filter, publishingDate, scrollCounterIds[i], scrollCountersPassword);
        return result;
    }

    private Integer getComments(String url, String apiUrl) throws Exception {
        if (StringUtils.isEmpty(apiUrl))
            return null;
        String response = new HttpSender(apiUrl, url).sendGet();
        JSONObject json = new JSONObject(response);
        return json.getJSONObject("data").getInt("comments_count");
    }

    @Override
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        try {
            MutableIssue issue = getIssue(transientVars);

            String url = CommonUtils.getCustomFieldStringValue(issue, Consts.URL_CF_ID);
            String filter = getFilter(url);
            double cost = (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.COST_CF_ID));
            Date publishingDate = (Date) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.PUBLISHING_DATE_CF_ID));

            Integer counterId = pluginData.getCounterId(issue.getProjectObject());
            String counterPassword = pluginData.getCounterPassword(issue.getProjectObject());
            Integer[] scrollCounterIds = pluginData.getScrollCounterIds(issue.getProjectObject());
            String scrollCountersPassword = pluginData.getScrollCountersPassword(issue.getProjectObject());
            String apiUrl = pluginData.getApiUrl(issue.getProjectObject());
            if (counterId == null || StringUtils.isEmpty(counterPassword))
                throw new IllegalStateException("Content Project Settings are not specified");

            int hits = getHits(filter, publishingDate, counterId, counterPassword);
            SocialMedia shares = getShares(url);
            SocialMedia hitsSocialMedia = getHitsSocialMedia(filter, publishingDate, counterId, counterPassword);
            SearchEngines hitsSearchEngines = getHitsSearchEngines(filter, publishingDate, counterId, counterPassword);
            Integer[] scrolls = getScrolls(filter, publishingDate, scrollCounterIds, scrollCountersPassword);
            Integer comments = getComments(url, apiUrl);

            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HIT_COST_CF_ID), hits != 0 ? (cost / hits) : null);
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.SHARE_COST_CF_ID), shares.getTotal() != 0 ? (cost / shares.getTotal()) : null);
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_CF_ID), (double) hits);
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_FACEBOOK_CF_ID), shares.getFacebook());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_MYMAIL_CF_ID), shares.getMymail());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_ODNOKLASSNIKI_CF_ID), shares.getOdnoklassniki());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_TWITTER_CF_ID), shares.getTwitter());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_VKONTAKTE_CF_ID), shares.getVkontakte());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_FACEBOOK_CF_ID), hitsSocialMedia.getFacebook());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_MYMAIL_CF_ID), hitsSocialMedia.getMymail());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_ODNOKLASSNIKI_CF_ID), hitsSocialMedia.getOdnoklassniki());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_TWITTER_CF_ID), hitsSocialMedia.getTwitter());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_VKONTAKTE_CF_ID), hitsSocialMedia.getVkontakte());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_GOOGLE_CF_ID), hitsSearchEngines.getGoogle());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_YANDEX_CF_ID), hitsSearchEngines.getYandex());
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_OTHERS_CF_ID), hitsSearchEngines.getOthers());
            for (int i = 0; i < Consts.SCROLL_CF_IDS.size(); i++)
                issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.SCROLL_CF_IDS.get(i)), scrolls[i] != null ? scrolls[i].doubleValue() : null);
            issue.setCustomFieldValue(CommonUtils.getCustomField(Consts.COMMENTS_CF_ID), comments != null ? comments.doubleValue() : null);
        } catch (Exception e) {
            log.error(e);
            throw new WorkflowException(e);
        }
    }

    @GET
    @Path("/shares")
    public Response getSharesJson(@QueryParam("url") final String url) {
        return new RestExecutor<SocialMediaOutput>() {
            @Override
            protected SocialMediaOutput doAction() throws Exception {
                return new SocialMediaOutput(getShares(url));
            }
        }.getResponse();
    }

    @SuppressWarnings("UnusedDeclaration")
    @XmlRootElement
    public static class SocialMediaOutput {
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

        private SocialMediaOutput() {
        }

        public SocialMediaOutput(SocialMedia socialMedia) {
            this.total = socialMedia.getTotal();
            this.facebook = socialMedia.getFacebook();
            this.mymail = socialMedia.getMymail();
            this.odnoklassniki = socialMedia.getOdnoklassniki();
            this.twitter = socialMedia.getTwitter();
            this.vkontakte = socialMedia.getVkontakte();
        }
    }
}
