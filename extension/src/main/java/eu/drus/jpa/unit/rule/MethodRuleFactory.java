package eu.drus.jpa.unit.rule;

import org.junit.rules.MethodRule;

public interface MethodRuleFactory {
    ExecutionPhase getPhase();

    int getPriority();

    MethodRule createRule(ExecutionContext factory);
}
