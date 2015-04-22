package ru.mail.jira.plugins.contentprojects.authors;

import ru.mail.jira.plugins.contentprojects.authors.freelancers.Freelancer;

public class FreelancerAuthor extends Author {
    private final Freelancer freelancer;

    public FreelancerAuthor(Freelancer freelancer) {
        this.freelancer = freelancer;
    }

    @Override
    public String getDbValue() {
        return "&" + freelancer.getID();
    }

    @Override
    public Long getSearchValue() {
        return (long) freelancer.getID();
    }

    @Override
    public String getShortCaption() {
        return freelancer.getFullName();
    }

    @Override
    public String getLongCaption() {
        return freelancer.getFullName();
    }

    @Override
    public String getAvatarUrl(boolean small) {
        return null;
    }

    public Freelancer getFreelancer() {
        return freelancer;
    }
}
