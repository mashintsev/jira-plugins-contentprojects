package ru.mail.jira.plugins.contentprojects.common;

import java.util.*;

public class Consts {
    public static final Collection<String> ACCOUNTANTS_GROUPS = Arrays.asList("cp-accountants", "cp-leads", "jira-administrators");
    public static final String AUTHORS_ROLE_NAME = "Developers";
    public static final Collection<Long> PROJECT_IDS = Arrays.asList(19070L, 19170L, 19270L, 19271L, 19272L, 19273L, 19274L, 21670L, 21671L);
    public static final Collection<String> STATUS_SPENT_IDS = Arrays.asList("11593", "11594", "11595");
    public static final Collection<String> STATUS_PLANNED_IDS = Arrays.asList("11395", "11592");

    public static final long PUBLISHING_DATE_CF_ID = 25102;
    public static final long PAYMENT_MONTH_CF_ID = 25625;
    public static final long COST_CF_ID = 25114;
    public static final long TEXT_AUTHOR_CF_ID = 26100;
    public static final long URL_CF_ID = 25103;
    public static final long CATEGORY_CF_ID = 25510;

    public static final long HIT_COST_CF_ID = 25203;
    public static final long SHARE_COST_CF_ID = 25204;
    public static final long HITS_CF_ID = 25202;
    public static final long SHARES_CF_ID = 25200;
    public static final long SHARES_FACEBOOK_CF_ID = 25505;
    public static final long SHARES_MYMAIL_CF_ID = 25506;
    public static final long SHARES_ODNOKLASSNIKI_CF_ID = 25507;
    public static final long SHARES_TWITTER_CF_ID = 25508;
    public static final long SHARES_VKONTAKTE_CF_ID = 25509;
    public static final long HITS_SOCIAL_MEDIA_CF_ID = 25201;
    public static final long HITS_SOCIAL_MEDIA_FACEBOOK_CF_ID = 25500;
    public static final long HITS_SOCIAL_MEDIA_MYMAIL_CF_ID = 25501;
    public static final long HITS_SOCIAL_MEDIA_ODNOKLASSNIKI_CF_ID = 25502;
    public static final long HITS_SOCIAL_MEDIA_TWITTER_CF_ID = 25503;
    public static final long HITS_SOCIAL_MEDIA_VKONTAKTE_CF_ID = 25504;
    public static final long HITS_SEARCH_ENGINES_CF_ID = 26208;
    public static final long HITS_SEARCH_ENGINES_GOOGLE_CF_ID = 26205;
    public static final long HITS_SEARCH_ENGINES_YANDEX_CF_ID = 26206;
    public static final long HITS_SEARCH_ENGINES_OTHERS_CF_ID = 26207;
    public static final List<Long> SCROLL_CF_IDS = Arrays.asList(25700L, 25701L, 25702L, 25703L, 25704L);
    public static final long TOTAL_TIME_CF_ID = 27602;
    public static final List<Long> TIME_INTERVAL_CF_IDS = Arrays.asList(27603L, 27604L, 27605L);
    public static final long ESTIMATED_TIME_CF_ID = 27700;
    public static final long COMMENTS_CF_ID = 25401;
    public static final long SHARE_RATIO_CF_ID = 27109;
    public static final long SOCIAL_ENGAGEMENT_CF_ID = 27103;
    public static final long HITS_TOUCH_CF_ID = 27903;
    public static final List<Long> SCROLL_TOUCH_CF_IDS = Arrays.asList(28000L, 28001L, 28002L, 28003L, 28004L);
    public static final long ENGAGEMENT_RATE = 29901;
    public static final long ENGAGEMENT_RATE_TOUCH = 29902;

    public static final String NEWS_USER_NAME = "s.paranjko@mail.msk";
    public static final long NEWS_ISSUE_TYPE_ID = 13904;

    public static final long PAYMENT_ACT_PROJECT_ID = 12471;
    public static final long PAYMENT_ACT_ISSUE_TYPE_ID = 13400;
    public static final long PAYMENT_ACT_COMPONENT_VALUE = 18025;
    public static final long PAYMENT_ACT_LEGAL_ENTITY_CF_ID = 16521;
    public static final String PAYMENT_ACT_LEGAL_ENTITY_VALUE = "14660";
    public static final long PAYMENT_ACT_CONTRAGENT_CF_ID = 24601;
    public static final String PAYMENT_ACT_CONTRAGENT_VALUE = "26964";
    public static final long PAYMENT_ACT_PROJECT_CF_ID = 11542;
    public static final Map<Long, String> PAYMENT_ACT_PROJECT_VALUE_MAP = new HashMap<Long, String>();
    public static final long PAYMENT_ACT_TYPICAL_CONTRACTS_CF_ID = 26000;
    public static final long PAYMENT_ACT_TYPICAL_CONTRACTS_TEMPLATE_ID = 52;
    public static final String PAYMENT_ACT_LINK_TYPE = "depends on";

    static {
        PAYMENT_ACT_PROJECT_VALUE_MAP.put(19070L, "23810");
        PAYMENT_ACT_PROJECT_VALUE_MAP.put(19170L, "23814");
        PAYMENT_ACT_PROJECT_VALUE_MAP.put(19270L, "23815");
        PAYMENT_ACT_PROJECT_VALUE_MAP.put(19271L, "23813");
        PAYMENT_ACT_PROJECT_VALUE_MAP.put(19272L, "23816");
        PAYMENT_ACT_PROJECT_VALUE_MAP.put(19273L, "23811");
        PAYMENT_ACT_PROJECT_VALUE_MAP.put(19274L, "23812");
    }

    public final static List<String> NOTIFICATION_USER_KEYS = Arrays.asList("ol.sidorova@mail.msk","a.molchanov@mail.msk");
}
