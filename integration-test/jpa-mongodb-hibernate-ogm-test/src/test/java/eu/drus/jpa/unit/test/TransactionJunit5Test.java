package eu.drus.jpa.unit.test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.JpaUnit;

@ExtendWith(JpaUnit.class)
@RunWith(JUnitPlatform.class)
public class TransactionJunit5Test extends AbstractTransactionJunit5Test {}
