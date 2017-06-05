package eu.drus.jpa.unit.test.model;

import java.sql.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "INSTANT_ACCESS_ACCOUNT")
public class InstantAccessAccount extends Account {

    protected InstantAccessAccount() {
        super();
        // for JPA
    }

    public InstantAccessAccount(final Depositor depositor) {
        super(depositor);
    }

    @Override
    public float withdraw(final float amount) throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Instant access account does not support money withdraw");
    }

    @Override
    public float transfer(final float amount, final Account toAccount) {
        final float balance = getBalance();
        final float balanceAfterTransfer = balance - amount;
        if (balanceAfterTransfer < 0.0f) {
            return 0;
        }

        final Date date = new Date(System.currentTimeMillis());

        addEntry(new AccountEntry(date, "ACC", "money transfer", amount, AccountEntryType.CREDIT));
        toAccount.addEntry(new AccountEntry(date, "ACC", "money transfer", amount, AccountEntryType.DEBIT));
        return amount;
    }

    @Override
    public float deposit(final float amount) throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Instant access account does not support money deposit");
    }

}
