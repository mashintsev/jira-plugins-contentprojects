package ru.mail.jira.plugins.contentprojects.authors;

public abstract class Author implements Comparable<Author> {
    public abstract String getDbValue();
    public abstract Object getSearchValue();
    public abstract String getShortCaption();
    public abstract String getLongCaption();
    @SuppressWarnings("UnusedDeclaration")
    public abstract String getAvatarUrl(boolean small);

    @Override
    public boolean equals(Object that) {
        return (that instanceof Author) && this.getDbValue().equals(((Author) that).getDbValue());
    }

    @Override
    public int hashCode() {
        return getDbValue().hashCode();
    }

    @Override
    public String toString() {
        return getShortCaption();
    }

    @Override
    public int compareTo(Author that) {
        if (that == null)
            throw new NullPointerException();
        if ((this instanceof FreelancerAuthor) && (that instanceof UserAuthor))
            return 1;
        if ((this instanceof UserAuthor) && (that instanceof FreelancerAuthor))
            return -1;
        if ((this instanceof FreelancerAuthor) && (that instanceof FreelancerAuthor) || (this instanceof UserAuthor) && (that instanceof UserAuthor))
            return getShortCaption().compareTo(that.getShortCaption());
        throw new ClassCastException();
    }
}
