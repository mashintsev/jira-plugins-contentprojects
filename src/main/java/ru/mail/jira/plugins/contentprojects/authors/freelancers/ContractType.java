package ru.mail.jira.plugins.contentprojects.authors.freelancers;

public enum ContractType {
    SOLE_PROPRIETORSHIP("ru.mail.jira.plugins.contentprojects.authors.freelancers.contractType.soleProprietorship"),
    CIVIL_LAW_CONTRACT("ru.mail.jira.plugins.contentprojects.authors.freelancers.contractType.civilLawContract"),
    NON_RESIDENT("ru.mail.jira.plugins.contentprojects.authors.freelancers.contractType.nonResident");

    private final String labelKey;

    ContractType(String labelKey) {
        this.labelKey = labelKey;
    }

    @SuppressWarnings("unused")
    public String getLabelKey() {
        return labelKey;
    }
}
