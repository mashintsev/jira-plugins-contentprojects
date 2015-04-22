package ru.mail.jira.plugins.contentprojects.statistics;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.util.*;

class GroupedSamplesCollector<T> extends Collector {
    private List<CustomField> customFields;
    private SortedMap<T, Sample> groupedSamples;
    private IndexReader indexReader;
    private StatisticsMapper<T> statisticsMapper;

    public GroupedSamplesCollector(List<CustomField> customFields, final StatisticsMapper<T> statisticsMapper) {
        this.customFields = customFields;
        this.groupedSamples = new TreeMap<T, Sample>(new Comparator<T>() {
            private final Comparator<T> comparator = statisticsMapper.getComparator();

            @Override
            public int compare(T t1, T t2) {
                if (t1 == null && t2 == null)
                    return 0;
                if (t1 == null)
                    return 1;
                if (t2 == null)
                    return -1;
                return comparator.compare(t1, t2);
            }
        });
        this.statisticsMapper = statisticsMapper;
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
    }

    @Override
    public void collect(int i) throws IOException {
        Document document = indexReader.document(i);
        T key = statisticsMapper.getValueFromLuceneField(document.get(statisticsMapper.getDocumentConstant()));
        Sample sample = groupedSamples.get(key);
        if (sample == null) {
            sample = new Sample(customFields);
            groupedSamples.put(key, sample);
        }
        sample.processDocument(document);
    }

    @Override
    public void setNextReader(IndexReader indexReader, int i) throws IOException {
        this.indexReader = indexReader;
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    public SortedMap<T, Sample> getGroupedSamples() {
        return groupedSamples;
    }
}
