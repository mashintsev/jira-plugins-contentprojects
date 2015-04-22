package ru.mail.jira.plugins.contentprojects.statistics;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.fields.CustomField;
import org.apache.lucene.document.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Sample {
    private final IssueFactory issueFactory = ComponentAccessor.getIssueFactory();
    private final List<CustomField> customFields;
    private int issueCount;
    private List<List<Double>> customFieldSamples;

    public Sample(List<CustomField> customFields) {
        this.customFields = customFields;
        this.issueCount = 0;
        this.customFieldSamples = new ArrayList<List<Double>>(customFields.size());
        for (CustomField ignored : customFields)
            this.customFieldSamples.add(new ArrayList<Double>());
    }

    public void processDocument(Document document) {
        Issue issue = issueFactory.getIssue(document);
        issueCount++;

        Iterator<CustomField> customFieldIterator = customFields.iterator();
        Iterator<List<Double>> customFieldSampleIterator = customFieldSamples.iterator();
        while (customFieldIterator.hasNext() && customFieldSampleIterator.hasNext()) {
            CustomField customField = customFieldIterator.next();
            List<Double> customFieldSample = customFieldSampleIterator.next();

            Object value = issue.getCustomFieldValue(customField);
            if (value instanceof Double)
                customFieldSample.add((Double) value);
        }
    }

    public int getIssueCount() {
        return issueCount;
    }

    public List<Statistic> calculateStatistics() {
        List<Statistic> result = new ArrayList<Statistic>(customFieldSamples.size());
        for (List<Double> customFieldSample : customFieldSamples)
            if (customFieldSample.isEmpty()) {
                result.add(null);
            } else {
                double sum = 0;
                for (Double value : customFieldSample)
                    sum += value;

                double median;
                Collections.sort(customFieldSample);
                if (customFieldSample.size() % 2 == 1)
                    median = customFieldSample.get(customFieldSample.size() / 2);
                else
                    median = (customFieldSample.get(customFieldSample.size() / 2 - 1) + customFieldSample.get(customFieldSample.size() / 2)) / 2;

                result.add(new Statistic(median, sum / customFieldSample.size(), sum));
            }
        return result;
    }
}
