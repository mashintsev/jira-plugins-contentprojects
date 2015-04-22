package ru.mail.jira.plugins.contentprojects.statistics;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.util.velocity.NumberTool;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@XmlRootElement
public class Statistic {
    private double medianValue;
    private double meanValue;
    private double sumValue;

    @XmlElement
    private String median;
    @XmlElement
    private String mean;
    @XmlElement
    private String sum;

    private Statistic() {
    }

    public Statistic(double median, double mean, double sum) {
        this.medianValue = median;
        this.meanValue = mean;
        this.sumValue = sum;

        NumberTool numberTool = new NumberTool(ComponentAccessor.getJiraAuthenticationContext().getLocale());
        this.median = numberTool.format(median);
        this.mean = numberTool.format(mean);
        this.sum = numberTool.format(sum);
    }

    public double getMedian() {
        return medianValue;
    }

    public double getMean() {
        return meanValue;
    }

    public double getSum() {
        return sumValue;
    }
}
