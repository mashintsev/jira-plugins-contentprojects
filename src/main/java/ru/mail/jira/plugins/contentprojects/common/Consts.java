package ru.mail.jira.plugins.contentprojects.common;

import java.util.*;

public class Consts {
    public static final Collection<String> ACCOUNTANTS_GROUPS = Arrays.asList("cp-accountants", "cp-leads", "jira-administrators");
    public static final String AUTHORS_ROLE_NAME = "Developers";
    public static final Collection<Long> PROJECT_IDS = Arrays.asList(19070L, 19170L, 19270L, 19271L, 19272L, 19273L, 19274L);
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
    public static final long EXCESSIVE_TIME_CF_ID = 27701;
    public static final long COMMENTS_CF_ID = 25401;
    public static final long SHARE_RATIO_CF_ID = 27109;
    public static final long SHARE_RATIO_FACEBOOK_CF_ID = 27110;
    public static final long SHARE_RATIO_MYMAIL_CF_ID = 27111;
    public static final long SHARE_RATIO_ODNOKLASSNIKI_CF_ID = 27112;
    public static final long SHARE_RATIO_TWITTER_CF_ID = 27113;
    public static final long SHARE_RATIO_VKONTAKTE_CF_ID = 27114;
    public static final long SOCIAL_ENGAGEMENT_CF_ID = 27103;
    public static final long SOCIAL_ENGAGEMENT_FACEBOOK_CF_ID = 27104;
    public static final long SOCIAL_ENGAGEMENT_MYMAIL_CF_ID = 27105;
    public static final long SOCIAL_ENGAGEMENT_ODNOKLASSNIKI_CF_ID = 27106;
    public static final long SOCIAL_ENGAGEMENT_TWITTER_CF_ID = 27107;
    public static final long SOCIAL_ENGAGEMENT_VKONTAKTE_CF_ID = 27108;

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
    public static final long PAYMENT_ACT_TYPICAL_CONTRACTS_TEMPLATE_ID = 47;
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

//    public static final Collection<String> ACCOUNTANTS_GROUPS = Arrays.asList("jira-developers");
//    public static final String AUTHORS_ROLE_NAME = "Users";
//    public static final Collection<Long> PROJECT_IDS = Arrays.asList(10000L, 10001L, 10100L, 10200L);
//    public static final Collection<String> STATUS_SPENT_IDS = Arrays.asList("1");
//    public static final Collection<String> STATUS_PLANNED_IDS = Arrays.asList("3");
//
//    public static final long PUBLISHING_DATE_CF_ID = 10000;
//    public static final long PAYMENT_MONTH_CF_ID = 10001;
//    public static final long COST_CF_ID = 10002;
//    public static final long TEXT_AUTHOR_CF_ID = 10036;
//    public static final long URL_CF_ID = 10003;
//    public static final long CATEGORY_CF_ID = 10004;
//
//    public static final long HIT_COST_CF_ID = 10005;
//    public static final long SHARE_COST_CF_ID = 10006;
//    public static final long HITS_CF_ID = 10007;
//    public static final long HITS_SEARCH_ENGINES_CF_ID = 10039;
//    public static final long HITS_SEARCH_ENGINES_GOOGLE_CF_ID = 10018;
//    public static final long HITS_SEARCH_ENGINES_YANDEX_CF_ID = 10019;
//    public static final long HITS_SEARCH_ENGINES_OTHERS_CF_ID = 10020;
//    public static final long HITS_SOCIAL_MEDIA_CF_ID = 10038;
//    public static final long HITS_SOCIAL_MEDIA_FACEBOOK_CF_ID = 10013;
//    public static final long HITS_SOCIAL_MEDIA_MYMAIL_CF_ID = 10014;
//    public static final long HITS_SOCIAL_MEDIA_ODNOKLASSNIKI_CF_ID = 10015;
//    public static final long HITS_SOCIAL_MEDIA_TWITTER_CF_ID = 10016;
//    public static final long HITS_SOCIAL_MEDIA_VKONTAKTE_CF_ID = 10017;
//    public static final List<Long> SCROLL_CF_IDS = Arrays.asList(10021L, 10022L, 10100L, 10101L, 10102L);
//    public static final long TOTAL_TIME_CF_ID = 10103;
//    public static final List<Long> TIME_INTERVAL_CF_IDS = Arrays.asList(10104L, 10105L, 10106L);
//    public static final long ESTIMATED_TIME_CF_ID = 10107;
//    public static final long EXCESSIVE_TIME_CF_ID = 10108;
//    public static final long SHARES_CF_ID = 10037;
//    public static final long SHARES_FACEBOOK_CF_ID = 10008;
//    public static final long SHARES_MYMAIL_CF_ID = 10009;
//    public static final long SHARES_ODNOKLASSNIKI_CF_ID = 10010;
//    public static final long SHARES_TWITTER_CF_ID = 10011;
//    public static final long SHARES_VKONTAKTE_CF_ID = 10012;
//    public static final long COMMENTS_CF_ID = 10023;
//    public static final long SHARE_RATIO_CF_ID = 10024;
//    public static final long SHARE_RATIO_FACEBOOK_CF_ID = 10025;
//    public static final long SHARE_RATIO_MYMAIL_CF_ID = 10026;
//    public static final long SHARE_RATIO_ODNOKLASSNIKI_CF_ID = 10027;
//    public static final long SHARE_RATIO_TWITTER_CF_ID = 10028;
//    public static final long SHARE_RATIO_VKONTAKTE_CF_ID = 10029;
//    public static final long SOCIAL_ENGAGEMENT_CF_ID = 10030;
//    public static final long SOCIAL_ENGAGEMENT_FACEBOOK_CF_ID = 10031;
//    public static final long SOCIAL_ENGAGEMENT_MYMAIL_CF_ID = 10032;
//    public static final long SOCIAL_ENGAGEMENT_ODNOKLASSNIKI_CF_ID = 10033;
//    public static final long SOCIAL_ENGAGEMENT_TWITTER_CF_ID = 10034;
//    public static final long SOCIAL_ENGAGEMENT_VKONTAKTE_CF_ID = 10035;
//
//    public static final long PAYMENT_ACT_PROJECT_ID = 10200;
//    public static final long PAYMENT_ACT_ISSUE_TYPE_ID = 1;
//    public static final long PAYMENT_ACT_COMPONENT_VALUE = 0;
//    public static final long PAYMENT_ACT_LEGAL_ENTITY_CF_ID = 0;
//    public static final String PAYMENT_ACT_LEGAL_ENTITY_VALUE = "0";
//    public static final long PAYMENT_ACT_CONTRAGENT_CF_ID = 0;
//    public static final String PAYMENT_ACT_CONTRAGENT_VALUE = "0";
//    public static final long PAYMENT_ACT_TYPICAL_CONTRACTS_CF_ID = 10600;
//    public static final long PAYMENT_ACT_TYPICAL_CONTRACTS_TEMPLATE_ID = 2;
//    public static final String PAYMENT_ACT_LINK_TYPE = "blocks";
}
