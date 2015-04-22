package ru.mail.jira.plugins.contentprojects.statistics;

import com.atlassian.jira.issue.fields.CustomField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import java.io.IOException;
import java.util.List;

class SampleCollector extends Collector {
    private IndexReader indexReader;
    private Sample sample;

    public SampleCollector(List<CustomField> customFields) {
        this.sample = new Sample(customFields);
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
    }

    @Override
    public void collect(int i) throws IOException {
        sample.processDocument(indexReader.document(i));
    }

    @Override
    public void setNextReader(IndexReader indexReader, int i) throws IOException {
        this.indexReader = indexReader;
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
        return true;
    }

    public Sample getSample() {
        return sample;
    }
}
