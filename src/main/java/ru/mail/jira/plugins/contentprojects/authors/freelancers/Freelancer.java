package ru.mail.jira.plugins.contentprojects.authors.freelancers;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

import java.util.Date;

@Table("Author")
public interface Freelancer extends Entity {
    String getFullName();
    void setFullName(String fullName);

    String getPayeeName();
    void setPayeeName(String payeeName);

    Date getContractDate();
    void setContractDate(Date contractDate);

    ContractType getContractType();
    void setContractType(ContractType contractType);

    String getInn();
    void setInn(String inn);

    String getSnils();
    void setSnils(String snils);

    boolean isDeleted();
    void setDeleted(boolean deleted);
}
