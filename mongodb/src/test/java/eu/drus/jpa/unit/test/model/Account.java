package eu.drus.jpa.unit.test.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "ACCOUNT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE")
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    @Version
    @Column(name = "VERSION")
    private Integer version;

    @ManyToOne(optional = false, cascade = {
            CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "DEPOSITOR_ID", updatable = false)
    private Depositor depositor;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ACCOUNT_ID")
    private List<AccountEntry> entries = new ArrayList<>();

    protected Account() {
        // for JPA
    }

    public Account(final Depositor depositor) {
        this.depositor = depositor;
        depositor.addAccount(this);
    }

    public Depositor getDepositor() {
        return depositor;
    }

    protected void setDepositor(final Depositor depositor) {
        this.depositor = depositor;
    }

    protected List<AccountEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    protected void addEntry(final AccountEntry entry) {
        entries.add(entry);
    }

    public float getBalance() {
        float balance = 0;
        final Iterator<AccountEntry> it = entries.iterator();
        while (it.hasNext()) {
            final AccountEntry entry = it.next();
            balance += entry.getAmount();
        }
        return balance;
    }

    public abstract float withdraw(float amount) throws OperationNotSupportedException;

    public abstract float transfer(float amount, Account toAccount);

    public abstract float deposit(float amount) throws OperationNotSupportedException;
}
