package ru.mail.jira.plugins.contentprojects.issue;

public class SocialMedia extends Number implements Comparable<SocialMedia> {
    private final double total;
    private final double facebook;
    private final double mymail;
    private final double odnoklassniki;
    private final double twitter;
    private final double vkontakte;

    public SocialMedia(double facebook, double mymail, double odnoklassniki, double twitter, double vkontakte) {
        this(facebook + mymail + odnoklassniki + twitter + vkontakte, facebook, mymail, odnoklassniki, twitter, vkontakte);
    }

    public SocialMedia(double total, double facebook, double mymail, double odnoklassniki, double twitter, double vkontakte) {
        this.total = total;
        this.facebook = facebook;
        this.mymail = mymail;
        this.odnoklassniki = odnoklassniki;
        this.twitter = twitter;
        this.vkontakte = vkontakte;
    }

    public double getTotal() {
        return total;
    }

    public double getFacebook() {
        return facebook;
    }

    public double getMymail() {
        return mymail;
    }

    public double getOdnoklassniki() {
        return odnoklassniki;
    }

    public double getTwitter() {
        return twitter;
    }

    public double getVkontakte() {
        return vkontakte;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(facebook).append("|");
        sb.append(mymail).append("|");
        sb.append(odnoklassniki).append("|");
        sb.append(twitter).append("|");
        sb.append(vkontakte);
        return sb.toString();
    }

    public static SocialMedia parseSocialMedia(String s) {
        String[] tokens = s.split("\\|");
        double facebook = Double.parseDouble(tokens[0]);
        double mymail = Double.parseDouble(tokens[1]);
        double odnoklassniki = Double.parseDouble(tokens[2]);
        double twitter = Double.parseDouble(tokens[3]);
        double vkontakte = Double.parseDouble(tokens[4]);
        return new SocialMedia(facebook, mymail, odnoklassniki, twitter, vkontakte);
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
    public int compareTo(SocialMedia socialMedia) {
        return Double.compare(getTotal(), socialMedia.getTotal());
    }
}
