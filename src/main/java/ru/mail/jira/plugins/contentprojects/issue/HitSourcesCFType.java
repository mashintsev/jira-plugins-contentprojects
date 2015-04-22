package ru.mail.jira.plugins.contentprojects.issue;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.converters.DoubleConverter;
import com.atlassian.jira.issue.customfields.impl.CalculatedCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.util.velocity.NumberTool;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.CommonUtils;
import ru.mail.jira.plugins.contentprojects.common.Consts;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class HitSourcesCFType extends CalculatedCFType<Double, Double> {
    private final DoubleConverter doubleConverter;

    public HitSourcesCFType(DoubleConverter doubleConverter) {
        this.doubleConverter = doubleConverter;
    }

    @Override
    public String getStringFromSingularObject(Double value) {
        return doubleConverter.getString(value);
    }

    @Override
    public Double getSingularObjectFromString(String string) throws FieldValidationException {
        return doubleConverter.getDouble(string);
    }

    @Override
    public String getChangelogValue(CustomField field, Double value) {
        return doubleConverter.getStringForChangelog(value);
    }

    public Map<String, Double> getHitSources(CustomField field, Issue issue) {
        Map<String, Double> result = new HashMap<String, Double>();
        if (field.getIdAsLong() == Consts.SHARES_CF_ID) {
            result.put("facebook", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_FACEBOOK_CF_ID)));
            result.put("mymail", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_MYMAIL_CF_ID)));
            result.put("odnoklassniki", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_ODNOKLASSNIKI_CF_ID)));
            result.put("twitter", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_TWITTER_CF_ID)));
            result.put("vkontakte", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.SHARES_VKONTAKTE_CF_ID)));
        } else if (field.getIdAsLong() == Consts.HITS_SOCIAL_MEDIA_CF_ID) {
            result.put("facebook", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_FACEBOOK_CF_ID)));
            result.put("mymail", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_MYMAIL_CF_ID)));
            result.put("odnoklassniki", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_ODNOKLASSNIKI_CF_ID)));
            result.put("twitter", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_TWITTER_CF_ID)));
            result.put("vkontakte", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SOCIAL_MEDIA_VKONTAKTE_CF_ID)));
        } else if (field.getIdAsLong() == Consts.HITS_SEARCH_ENGINES_CF_ID) {
            result.put("google", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_GOOGLE_CF_ID)));
            result.put("yandex", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_YANDEX_CF_ID)));
            result.put("others", (Double) issue.getCustomFieldValue(CommonUtils.getCustomField(Consts.HITS_SEARCH_ENGINES_OTHERS_CF_ID)));
        }
        return result;
    }

    @Nullable
    @Override
    public Double getValueFromIssue(CustomField field, Issue issue) {
        double result = 0;
        for (Double hitSource : getHitSources(field, issue).values()) {
            if (hitSource == null)
                return null;
            result += hitSource;
        }
        return result;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {
        Map<String, Object> params = super.getVelocityParameters(issue, field, fieldLayoutItem);
        params.put("numberTool", new NumberTool(getI18nBean().getLocale()));
        params.putAll(getHitSources(field, issue));
        return params;
    }
}
