package ru.mail.jira.plugins.contentprojects.issue;

public class SearchEngines extends Number implements Comparable<SearchEngines> {
    private final double total;
    private final double google;
    private final double yandex;
    private final double others;

    public SearchEngines(double google, double yandex, double others) {
        this(google + yandex + others, google, yandex, others);
    }

    public SearchEngines(double total, double google, double yandex, double others) {
        this.total = total;
        this.google = google;
        this.yandex = yandex;
        this.others = others;
    }

    public double getTotal() {
        return total;
    }

    public double getGoogle() {
        return google;
    }

    public double getYandex() {
        return yandex;
    }

    public double getOthers() {
        return others;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(google).append("|");
        sb.append(yandex).append("|");
        sb.append(others);
        return sb.toString();
    }

    public static SearchEngines parseSearchEngines(String s) {
        String[] tokens = s.split("\\|");
        double google = Double.parseDouble(tokens[0]);
        double yandex = Double.parseDouble(tokens[1]);
        double others = Double.parseDouble(tokens[2]);
        return new SearchEngines(google, yandex, others);
    }

    @Override
    public int intValue() {
        return (int) getTotal();
    }

    @Override
    public long longValue() {
        return (long) getTotal();
    }

    @Override
    public float floatValue() {
        return (float) getTotal();
    }

    @Override
    public double doubleValue() {
        return getTotal();
    }

    @Override
    public int compareTo(SearchEngines searchEngines) {
        return Double.compare(getTotal(), searchEngines.getTotal());
    }
}
