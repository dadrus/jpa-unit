package eu.drus.jpa.unit.test;

import javax.persistence.TypedQuery;

import org.junit.Rule;

import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRule;
import eu.drus.jpa.unit.api.TransactionMode;
import eu.drus.jpa.unit.api.Transactional;
import eu.drus.jpa.unit.test.model.Account;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.GiroAccount;
import eu.drus.jpa.unit.test.model.InstantAccessAccount;
import eu.drus.jpa.unit.test.model.OperationNotSupportedException;

public class AbstractNewAccountFixture extends AbstractConcordionFixture {

    @Rule
    public JpaUnitRule rule = new JpaUnitRule(getClass());

    @InitialDataSets("datasets/max-payne-data.json")
    public Depositor findCustomer(final String customerName) {
        // the following is just to verify, that the initial data is indeed present

        final String[] nameParts = customerName.split(" ");
        final TypedQuery<Depositor> query = manager.createQuery("SELECT d FROM Depositor d WHERE d.name=:name", Depositor.class);
        query.setParameter("name", nameParts[0]);

        return query.getSingleResult();
    }

    public Depositor applyForNewAccount(final String accountType, final Depositor depositor) throws OperationNotSupportedException {
        createNewAccount(accountType, depositor);

        // we've obtained this object in a previous transaction - so it is in a detached state.
        manager.merge(depositor);
        return depositor;
    }

    private Account createNewAccount(final String accountType, final Depositor depositor) throws OperationNotSupportedException {
        if (accountType.equals("giro account")) {
            return new GiroAccount(depositor);
        } else if (accountType.equals("instant access account")) {
            return new InstantAccessAccount(depositor);
        } else {
            throw new RuntimeException("unsupported account type: " + accountType);
        }
    }

    @Transactional(TransactionMode.DISABLED)
    public int getNumberOfAccounts(final Depositor depositor) {
        return depositor.getAccounts().size();
    }

    @Transactional(TransactionMode.DISABLED)
    public boolean hasAccountOfType(final Depositor depositor, final String accountType) {
        for (final Account account : depositor.getAccounts()) {
            if (account instanceof GiroAccount && accountType.equals("giro account")) {
                return true;
            } else if (account instanceof InstantAccessAccount && accountType.equals("instant access account")) {
                return true;
            }
        }
        return false;
    }

}
