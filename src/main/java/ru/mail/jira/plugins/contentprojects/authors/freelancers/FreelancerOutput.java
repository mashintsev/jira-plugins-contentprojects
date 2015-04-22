package ru.mail.jira.plugins.contentprojects.authors.freelancers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.SimpleDateFormat;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
@XmlRootElement
public class FreelancerOutput {
    @XmlElement
    private String id;
    @XmlElement
    private String fullName;
    @XmlElement
    private String payeeName;
    @XmlElement
    private String contractDate;
    @XmlElement
    private String contractType;
    @XmlElement
    private String inn;
    @XmlElement
    private String snils;

    public FreelancerOutput(Freelancer freelancer) {
        this.id = String.valueOf(freelancer.getID());
        this.fullName = freelancer.getFullName();
        this.payeeName = freelancer.getPayeeName();
        this.contractDate = new SimpleDateFormat(ContentProjectsFreelancersAction.DATE_FORMAT).format(freelancer.getContractDate());
        this.contractType = freelancer.getContractType().toString();
        this.inn = freelancer.getInn();
        this.snils = freelancer.getSnils();
    }
}
