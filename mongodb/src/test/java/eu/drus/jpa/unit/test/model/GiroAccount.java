package eu.drus.jpa.unit.test.model;

import java.sql.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "GIRO_ACCOUNT")
public class GiroAccount extends Account {

    @Column(name = "CREDIT_LIMIT")
    @Basic(optional = false)
    private Float creditLimit = 0.0f;

    protected GiroAccount() {
        super();
        // for JPA
    }

    public GiroAccount(final Depositor depositor) throws OperationNotSupportedException {
        this(depositor, 0.0f);
    }

    public GiroAccount(final Depositor depositor, final float initialDeposit) throws OperationNotSupportedException {
        super(depositor);
        if (initialDeposit != 0) {
            deposit(initialDeposit);
        }
    }

    public float getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(final float creditLimit) {
        this.creditLimit = creditLimit;
    }

    @Override
    public float withdraw(final float amount) throws OperationNotSupportedException {
        final float balance = getBalance();
        final float balanceAfterWithdraw = balance - amount;
        if (balanceAfterWithdraw < 0.0f) {
            // if (-balanceAfterWithdraw > creditLimit) {
            return 0;
            // }
        }

        addEntry(new AccountEntry(new Date(System.currentTimeMillis()), "ACC", "cash withdraw", amount, AccountEntryType.CREDIT));
        return amount;
    }

    @Override
    public float transfer(final float amount, final Account toAccount) {
        // final float balance = getBalance();
        // final float balanceAfterTransfer = balance - amount;
        // if (balanceAfterTransfer < 0.0f) {
        // if (-balanceAfterTransfer > creditLimit) {
        // return 0;
        // }
        // }

        final Date date = new Date(System.currentTimeMillis());

        addEntry(new AccountEntry(date, "ACC", "money transfer", amount, AccountEntryType.CREDIT));
        toAccount.addEntry(new AccountEntry(date, "ACC", "money transfer", amount, AccountEntryType.DEBIT));
        return amount;
    }

    @Override
    public float deposit(final float amount) throws OperationNotSupportedException {
        addEntry(new AccountEntry(new Date(System.currentTimeMillis()), "ACC", "deposit", amount, AccountEntryType.DEBIT));
        return amount;
    }

}
