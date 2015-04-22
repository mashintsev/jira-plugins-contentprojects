package ru.mail.jira.plugins.contentprojects.authors.freelancers;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.sal.api.transaction.TransactionCallback;
import net.java.ao.Query;
import org.apache.commons.lang3.StringUtils;
import ru.mail.jira.plugins.commons.ContractUtils;
import ru.mail.jira.plugins.commons.RestFieldException;

import java.util.Date;

public class FreelancerManager {
    private final ActiveObjects ao;
    private final I18nHelper i18nHelper;

    public FreelancerManager(ActiveObjects ao, I18nHelper i18nHelper) {
        this.ao = ao;
        this.i18nHelper = i18nHelper;
    }

    public Freelancer getFreelancer(final int id) {
        return ao.executeInTransaction(new TransactionCallback<Freelancer>() {
            @Override
            public Freelancer doInTransaction() {
                Freelancer freelancer = ao.get(Freelancer.class, id);
                if (freelancer == null)
                    throw new IllegalArgumentException(String.format("Freelancer is not found by id %s", id));
                return freelancer;
            }
        });
    }

    public Freelancer[] getFreelancers() {
        return ao.executeInTransaction(new TransactionCallback<Freelancer[]>() {
            @Override
            public Freelancer[] doInTransaction() {
                return ao.find(Freelancer.class, Query.select().where("DELETED = false").order("FULL_NAME"));
            }
        });
    }

    public Freelancer[] searchFreelancers(final String fullName) {
        return ao.executeInTransaction(new TransactionCallback<Freelancer[]>() {
            @Override
            public Freelancer[] doInTransaction() {
                return ao.find(Freelancer.class, Query.select().where("LOWER(FULL_NAME) LIKE LOWER(?) AND DELETED = false", "%" + fullName + "%").order("FULL_NAME"));
            }
        });
    }

    private void validateFreelancerParams(int id, String fullName, Date contractDate, ContractType contractType, String inn, String snils) {
        if (StringUtils.isEmpty(fullName))
            throw new RestFieldException(i18nHelper.getText("issue.field.required", i18nHelper.getText("ru.mail.jira.plugins.contentprojects.authors.freelancers.fullName")), "full-name");

        Freelancer[] freelancers = searchFreelancers(fullName);
        if (freelancers.length == 1 && freelancers[0].getID() != id || freelancers.length > 1)
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.contentprojects.authors.freelancers.error.fullNameAlreadyUsed"), "full-name");

        if (contractDate == null)
            throw new RestFieldException(i18nHelper.getText("issue.field.required", i18nHelper.getText("ru.mail.jira.plugins.contentprojects.authors.freelancers.contractDate")), "contract-date");

        if (contractType == null)
            throw new RestFieldException(i18nHelper.getText("issue.field.required", i18nHelper.getText("ru.mail.jira.plugins.contentprojects.authors.freelancers.contractType")), "contract-type");

        if (contractType == ContractType.SOLE_PROPRIETORSHIP && StringUtils.isEmpty(inn))
            throw new RestFieldException(i18nHelper.getText("issue.field.required", i18nHelper.getText("ru.mail.jira.plugins.contentprojects.authors.freelancers.inn")), "inn");

        if (contractType == ContractType.CIVIL_LAW_CONTRACT && StringUtils.isEmpty(snils))
            throw new RestFieldException(i18nHelper.getText("issue.field.required", i18nHelper.getText("ru.mail.jira.plugins.contentprojects.authors.freelancers.snils")), "snils");

        if (StringUtils.isNotEmpty(inn) && !ContractUtils.isValidInn(inn))
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.contentprojects.authors.freelancers.error.invalidInn"), "inn");

        if (StringUtils.isNotEmpty(snils) && !ContractUtils.isValidSnils(snils))
            throw new RestFieldException(i18nHelper.getText("ru.mail.jira.plugins.contentprojects.authors.freelancers.error.invalidSnils"), "snils");
    }

    public int createFreelancer(final String fullName, final String payeeName, final Date contractDate, final ContractType contractType, final String inn, final String snils) {
        validateFreelancerParams(-1, fullName, contractDate, contractType, inn, snils);
        return ao.executeInTransaction(new TransactionCallback<Integer>() {
            @Override
            public Integer doInTransaction() {
                Freelancer freelancer = ao.create(Freelancer.class);
                freelancer.setFullName(fullName);
                freelancer.setPayeeName(payeeName);
                freelancer.setContractDate(contractDate);
                freelancer.setContractType(contractType);
                freelancer.setInn(inn);
                freelancer.setSnils(snils);
                freelancer.setDeleted(false);
                freelancer.save();
                return freelancer.getID();
            }
        });
    }

    public void updateFreelancer(final int id, final String fullName, final String payeeName, final Date contractDate, final ContractType contractType, final String inn, final String snils) {
        validateFreelancerParams(id, fullName, contractDate, contractType, inn, snils);
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                Freelancer freelancer = getFreelancer(id);
                freelancer.setFullName(fullName);
                freelancer.setPayeeName(payeeName);
                freelancer.setContractDate(contractDate);
                freelancer.setContractType(contractType);
                freelancer.setInn(inn);
                freelancer.setSnils(snils);
                freelancer.save();
                return null;
            }
        });
    }

    public void deleteFreelancer(final int id) {
        ao.executeInTransaction(new TransactionCallback<Void>() {
            @Override
            public Void doInTransaction() {
                Freelancer freelancer = getFreelancer(id);
                freelancer.setDeleted(true);
                freelancer.save();
                return null;
            }
        });
    }
}