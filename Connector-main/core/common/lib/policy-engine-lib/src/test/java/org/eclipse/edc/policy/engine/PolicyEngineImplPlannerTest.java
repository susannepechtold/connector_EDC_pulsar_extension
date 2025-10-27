/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.policy.engine;

import org.eclipse.edc.policy.engine.spi.DynamicAtomicConstraintRuleFunction;
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyContextImpl;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.PolicyRuleFunction;
import org.eclipse.edc.policy.engine.spi.PolicyValidatorRule;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.engine.spi.plan.PolicyEvaluationPlan;
import org.eclipse.edc.policy.engine.spi.plan.step.AtomicConstraintStep;
import org.eclipse.edc.policy.engine.spi.plan.step.MultiplicityConstraintStep;
import org.eclipse.edc.policy.engine.spi.plan.step.RuleStep;
import org.eclipse.edc.policy.engine.spi.plan.step.ValidatorStep;
import org.eclipse.edc.policy.engine.validation.RuleValidator;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AndConstraint;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.Duty;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.OrConstraint;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.Prohibition;
import org.eclipse.edc.policy.model.Rule;
import org.eclipse.edc.policy.model.XoneConstraint;
import org.eclipse.edc.spi.EdcException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.policy.model.Operator.EQ;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class PolicyEngineImplPlannerTest {

    private static final String TEST_SCOPE = "test";

    private final RuleBindingRegistry bindingRegistry = new RuleBindingRegistryImpl();
    private PolicyEngine policyEngine;

    private static AtomicConstraint atomicConstraint(String key, String value) {
        var left = new LiteralExpression(key);
        var right = new LiteralExpression(value);
        return AtomicConstraint.Builder.newInstance()
                .leftExpression(left)
                .operator(EQ)
                .rightExpression(right)
                .build();
    }

    @BeforeEach
    void setUp() {
        policyEngine = new PolicyEngineImpl(new ScopeFilter(bindingRegistry), new RuleValidator(bindingRegistry));
        policyEngine.registerScope("test", TestContext.class);
    }

    @Nested
    class EvaluationPlan {

        @ParameterizedTest
        @ArgumentsSource(SimplePolicyProvider.class)
        void withRule(Policy policy, Class<Rule> ruleClass, String action, String key, Function<PolicyEvaluationPlan, List<RuleStep<? extends Rule>>> stepsProvider) {

            bindingRegistry.bind(action, TEST_SCOPE);
            bindingRegistry.bind(key, TEST_SCOPE);

            policyEngine.registerFunction(TestContext.class, ruleClass, key, (op, rv, r, ctx) -> true);

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(stepsProvider.apply(plan)).hasSize(1)
                    .first()
                    .satisfies(ruleStep -> {
                        assertThat(ruleStep.isFiltered()).isFalse();
                        assertThat(ruleStep.getRuleFunctions()).hasSize(0);
                        assertThat(ruleStep.getConstraintSteps()).hasSize(1)
                                .first()
                                .isInstanceOfSatisfying(AtomicConstraintStep.class, (constraintStep) -> {
                                    assertThat(constraintStep.isFiltered()).isFalse();
                                    assertThat(constraintStep.functionName()).isNotNull();
                                    assertThat(constraintStep.constraint()).isNotNull();
                                    assertThat(constraintStep.rule()).isInstanceOf(ruleClass);
                                });
                    });
        }

        @ParameterizedTest
        @ArgumentsSource(SimplePolicyProvider.class)
        void withRuleAndDynFunction(Policy policy, Class<Rule> ruleClass, String action, String key, Function<PolicyEvaluationPlan, List<RuleStep<? extends Rule>>> stepsProvider) {

            var function = new DynamicAtomicConstraintRuleFunction<Rule, TestContext>() {

                @Override
                public boolean evaluate(Object leftValue, Operator operator, Object rightValue, Rule rule, TestContext context) {
                    throw new EdcException("should not pass here");
                }

                @Override
                public boolean canHandle(Object leftValue) {
                    return true;
                }
            };

            bindingRegistry.bind(action, TEST_SCOPE);
            bindingRegistry.bind(key, TEST_SCOPE);

            policyEngine.registerFunction(TestContext.class, ruleClass, function);

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(stepsProvider.apply(plan)).hasSize(1)
                    .first()
                    .satisfies(ruleStep -> {
                        assertThat(ruleStep.isFiltered()).isFalse();
                        assertThat(ruleStep.getRuleFunctions()).hasSize(0);
                        assertThat(ruleStep.getConstraintSteps()).hasSize(1)
                                .first()
                                .isInstanceOfSatisfying(AtomicConstraintStep.class, (constraintStep) -> {
                                    assertThat(constraintStep.isFiltered()).isFalse();
                                    assertThat(constraintStep.functionName()).isNotNull();
                                    assertThat(constraintStep.constraint()).isNotNull();
                                    assertThat(constraintStep.rule()).isInstanceOf(ruleClass);
                                });
                    });
        }


        @ParameterizedTest
        @ArgumentsSource(SimplePolicyProvider.class)
        void withRuleAndRuleFunction(Policy policy, Class<Rule> ruleClass, String action, String key, Function<PolicyEvaluationPlan, List<RuleStep<? extends Rule>>> stepsProvider) {

            PolicyRuleFunction<Rule, TestContext> function = mock();

            bindingRegistry.bind(action, TEST_SCOPE);
            bindingRegistry.bind(key, TEST_SCOPE);

            policyEngine.registerFunction(TestContext.class, ruleClass, function);
            policyEngine.registerFunction(TestContext.class, ruleClass, function);

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(stepsProvider.apply(plan)).hasSize(1)
                    .first()
                    .satisfies(ruleStep -> {
                        assertThat(ruleStep.isFiltered()).isFalse();
                        assertThat(ruleStep.getRuleFunctions()).hasSize(2);
                        assertThat(ruleStep.getConstraintSteps()).hasSize(1)
                                .first()
                                .isInstanceOfSatisfying(AtomicConstraintStep.class, (constraintStep) -> {
                                    assertThat(constraintStep.isFiltered()).isTrue();
                                    assertThat(constraintStep.functionName()).isNull();
                                    assertThat(constraintStep.constraint()).isNotNull();
                                    assertThat(constraintStep.rule()).isInstanceOf(ruleClass);
                                });
                    });
        }

        @ParameterizedTest
        @ArgumentsSource(SimplePolicyProvider.class)
        void withRuleAndRuleFunctionNotBound(Policy policy, Class<Rule> ruleClass, String action, String key, Function<PolicyEvaluationPlan, List<RuleStep<? extends Rule>>> stepsProvider) {

            bindingRegistry.bind(action, TEST_SCOPE);
            bindingRegistry.bind(key, TEST_SCOPE);

            PolicyRuleFunction<Rule, UnboundedContext> function = mock();
            policyEngine.registerFunction(UnboundedContext.class, ruleClass, function);

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(stepsProvider.apply(plan)).hasSize(1)
                    .first()
                    .satisfies(ruleStep -> {
                        assertThat(ruleStep.isFiltered()).isFalse();
                        assertThat(ruleStep.getRuleFunctions()).hasSize(0);
                    });
        }

        @Test
        void withPermissionContainingDuty() {

            var key = "foo";
            var actionType = "action";
            var constraint = atomicConstraint(key, "bar");
            var action = Action.Builder.newInstance().type(actionType).build();
            var duty = Duty.Builder.newInstance().constraint(constraint).action(action).build();
            var permission = Permission.Builder.newInstance().constraint(constraint).duty(duty).action(action).build();
            var policy = Policy.Builder.newInstance().permission(permission).build();

            bindingRegistry.bind(actionType, TEST_SCOPE);
            bindingRegistry.bind(key, TEST_SCOPE);

            policyEngine.registerFunction(TestContext.class, Duty.class, key, (op, rv, r, ctx) -> true);

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(plan.getPermissionSteps()).hasSize(1)
                    .first()
                    .satisfies(ruleStep -> {
                        assertThat(ruleStep.isFiltered()).isFalse();
                        assertThat(ruleStep.getDutySteps()).hasSize(1);
                        assertThat(ruleStep.getRuleFunctions()).hasSize(0);
                        assertThat(ruleStep.getConstraintSteps()).hasSize(1)
                                .first()
                                .isInstanceOfSatisfying(AtomicConstraintStep.class, (constraintStep) -> {
                                    assertThat(constraintStep.isFiltered()).isTrue();
                                    assertThat(constraintStep.functionName()).isNull();
                                    assertThat(constraintStep.constraint()).isNotNull();
                                    assertThat(constraintStep.rule()).isInstanceOf(Permission.class);
                                });
                    });
        }


        private static class SimplePolicyProvider implements ArgumentsProvider {
            @Override
            public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

                var leftOperand = "foo";
                var actionType = "action";

                var action = Action.Builder.newInstance().type(actionType).build();
                var constraint = atomicConstraint(leftOperand, "bar");

                var prohibition = Prohibition.Builder.newInstance().constraint(constraint).action(action).build();

                Function<PolicyEvaluationPlan, List<? extends RuleStep<? extends Rule>>> permissionSteps = PolicyEvaluationPlan::getPermissionSteps;
                Function<PolicyEvaluationPlan, List<? extends RuleStep<? extends Rule>>> dutySteps = PolicyEvaluationPlan::getObligationSteps;
                Function<PolicyEvaluationPlan, List<? extends RuleStep<? extends Rule>>> prohibitionSteps = PolicyEvaluationPlan::getProhibitionSteps;

                var permission = Permission.Builder.newInstance().constraint(constraint).action(action).build();
                var duty = Duty.Builder.newInstance().constraint(constraint).action(action).build();

                return Stream.of(
                        of(Policy.Builder.newInstance().permission(permission).build(), Permission.class, actionType, leftOperand, permissionSteps),
                        of(Policy.Builder.newInstance().duty(duty).build(), Duty.class, actionType, leftOperand, dutySteps),
                        of(Policy.Builder.newInstance().prohibition(prohibition).build(), Prohibition.class, actionType, leftOperand, prohibitionSteps)
                );
            }
        }
    }

    @Nested
    class IgnoredStep {

        @Test
        void shouldIgnorePermissionStep_whenActionNotBound() {

            bindingRegistry.bind("foo", TEST_SCOPE);

            var constraint = atomicConstraint("foo", "bar");

            var permission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("action").build()).constraint(constraint).build();
            var policy = Policy.Builder.newInstance().permission(permission).build();
            policyEngine.registerFunction(TestContext.class, Permission.class, "foo", (op, rv, r, ctx) -> true);

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(plan.getPermissionSteps()).hasSize(1)
                    .first()
                    .satisfies(permissionStep -> {
                        assertThat(permissionStep.isFiltered()).isTrue();
                        assertThat(permissionStep.getFilteringReasons()).hasSize(1);
                        assertThat(permissionStep.getConstraintSteps()).hasSize(1)
                                .first()
                                .isInstanceOfSatisfying(AtomicConstraintStep.class, constraintStep -> {
                                    assertThat(constraintStep.isFiltered()).isFalse();
                                });
                    });
        }

        @Test
        void shouldIgnoreAtomicConstraintStep_whenLeftExpressionNotScopeBound() {

            bindingRegistry.bind("action", TEST_SCOPE);

            var constraint = atomicConstraint("foo", "bar");
            var permission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("action").build()).constraint(constraint).build();
            var policy = Policy.Builder.newInstance().permission(permission).build();

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(plan.getPermissionSteps()).hasSize(1)
                    .first()
                    .satisfies(permissionStep -> {
                        assertThat(permissionStep.isFiltered()).isFalse();
                        assertThat(permissionStep.getConstraintSteps()).hasSize(1)
                                .first()
                                .isInstanceOfSatisfying(AtomicConstraintStep.class, constraintStep -> {
                                    assertThat(constraintStep.isFiltered()).isTrue();
                                });
                    });
        }

        @Test
        void shouldIgnoreAtomicConstraintStep_whenLeftExpressionNotFunctionBound() {

            bindingRegistry.bind("action", TEST_SCOPE);
            bindingRegistry.bind("foo", TEST_SCOPE);

            var constraint = atomicConstraint("foo", "bar");
            var permission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("action").build()).constraint(constraint).build();
            var policy = Policy.Builder.newInstance().permission(permission).build();

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(plan.getPermissionSteps()).hasSize(1)
                    .first()
                    .satisfies(permissionStep -> {
                        assertThat(permissionStep.isFiltered()).isFalse();
                        assertThat(permissionStep.getConstraintSteps()).hasSize(1)
                                .first()
                                .isInstanceOfSatisfying(AtomicConstraintStep.class, constraintStep -> {
                                    assertThat(constraintStep.isFiltered()).isTrue();
                                });
                    });
        }

        @Test
        void shouldIgnoreAtomicConstraintStep_whenLeftExpressionNotDynFunctionBound() {


            DynamicAtomicConstraintRuleFunction<Duty, TestContext> function = mock();

            when(function.canHandle(any())).thenReturn(true);

            bindingRegistry.bind("action", TEST_SCOPE);
            bindingRegistry.bind("foo", TEST_SCOPE);

            var constraint = atomicConstraint("foo", "bar");
            var permission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("action").build()).constraint(constraint).build();
            var policy = Policy.Builder.newInstance().permission(permission).build();
            policyEngine.registerFunction(TestContext.class, Duty.class, function);

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(plan.getPermissionSteps()).hasSize(1)
                    .first()
                    .satisfies(permissionStep -> {
                        assertThat(permissionStep.isFiltered()).isFalse();
                        assertThat(permissionStep.getConstraintSteps()).hasSize(1)
                                .first()
                                .isInstanceOfSatisfying(AtomicConstraintStep.class, constraintStep -> {
                                    assertThat(constraintStep.isFiltered()).isTrue();
                                    assertThat(constraintStep.functionName()).isNull();
                                });
                    });
        }

    }

    @Nested
    class MultiplicityConstraints {

        @ParameterizedTest
        @ArgumentsSource(MultiplicityPolicyProvider.class)
        void shouldEvaluate_withMultiplicityConstraint(Policy policy, Class<Rule> ruleClass, String action, String key, Function<PolicyEvaluationPlan, List<RuleStep<? extends Rule>>> stepsProvider) {
            bindingRegistry.bind(key, TEST_SCOPE);
            bindingRegistry.bind(action, TEST_SCOPE);

            policyEngine.registerFunction(TestContext.class, ruleClass, key, (op, rv, r, ctx) -> true);

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, policy);

            assertThat(plan.getPreValidators()).isEmpty();
            assertThat(plan.getPostValidators()).isEmpty();


            assertThat(stepsProvider.apply(plan)).hasSize(1)
                    .first()
                    .satisfies((ruleStep -> {
                        assertThat(ruleStep.isFiltered()).isFalse();
                        assertThat(ruleStep.getConstraintSteps()).hasSize(1)
                                .first()
                                .isInstanceOfSatisfying(MultiplicityConstraintStep.class, constraintStep -> {
                                    assertThat(constraintStep.getConstraintSteps()).hasSize(2);
                                    assertThat(constraintStep.getConstraint()).isNotNull();
                                });
                    }));

        }

        private static class MultiplicityPolicyProvider implements ArgumentsProvider {
            @Override
            public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

                var leftOperand = "foo";
                var actionType = "action";

                var firstConstraint = atomicConstraint("foo", "bar");
                var secondConstraint = atomicConstraint("baz", "bar");

                var orConstraints = OrConstraint.Builder.newInstance().constraint(firstConstraint).constraint(secondConstraint).build();
                var andConstraints = AndConstraint.Builder.newInstance().constraint(firstConstraint).constraint(secondConstraint).build();
                var xoneConstraint = XoneConstraint.Builder.newInstance().constraint(firstConstraint).constraint(secondConstraint).build();

                var permission = Permission.Builder.newInstance().constraint(andConstraints).build();
                var prohibition = Prohibition.Builder.newInstance().constraint(orConstraints).build();
                var duty = Duty.Builder.newInstance().constraint(xoneConstraint).build();

                Function<PolicyEvaluationPlan, List<? extends RuleStep<? extends Rule>>> permissionSteps = PolicyEvaluationPlan::getPermissionSteps;
                Function<PolicyEvaluationPlan, List<? extends RuleStep<? extends Rule>>> dutySteps = PolicyEvaluationPlan::getObligationSteps;
                Function<PolicyEvaluationPlan, List<? extends RuleStep<? extends Rule>>> prohibitionSteps = PolicyEvaluationPlan::getProhibitionSteps;

                return Stream.of(
                        of(Policy.Builder.newInstance().permission(permission).build(), Permission.class, actionType, leftOperand, permissionSteps),
                        of(Policy.Builder.newInstance().duty(duty).build(), Duty.class, actionType, leftOperand, dutySteps),
                        of(Policy.Builder.newInstance().prohibition(prohibition).build(), Prohibition.class, actionType, leftOperand, prohibitionSteps)
                );
            }
        }
    }

    @Nested
    class Validator {

        @Test
        void shouldEvaluate_withNoValidators() {
            policyEngine.registerScope("another.scope", PolicyContext.class);
            var emptyPolicy = Policy.Builder.newInstance().build();
            policyEngine.registerPreValidator(TestContext.class, (policy, policyContext) -> true);

            var plan = policyEngine.createEvaluationPlan("another.scope", emptyPolicy);

            assertThat(plan.getPostValidators()).isEmpty();
            assertThat(plan.getPreValidators()).isEmpty();
        }

        @Test
        void shouldEvaluate_withFunctionalValidators() {
            var emptyPolicy = Policy.Builder.newInstance().build();

            PolicyValidatorRule<TestContext> function = (policy, policyContext) -> true;
            policyEngine.registerPreValidator(TestContext.class, function);
            policyEngine.registerPostValidator(TestContext.class, function);

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, emptyPolicy);

            assertThat(plan.getPreValidators()).hasSize(1)
                    .extracting(ValidatorStep::name)
                    .allMatch(s -> s.contains(PolicyEngineImplPlannerTest.class.getSimpleName()));

            assertThat(plan.getPostValidators()).hasSize(1)
                    .extracting(ValidatorStep::name)
                    .allMatch(s -> s.contains(PolicyEngineImplPlannerTest.class.getSimpleName()));

        }

        @Test
        void shouldEvaluate_withValidators() {
            var emptyPolicy = Policy.Builder.newInstance().build();
            policyEngine.registerPreValidator(TestContext.class, new MyValidatorFunction());
            policyEngine.registerPostValidator(TestContext.class, new MyValidatorFunction());

            var plan = policyEngine.createEvaluationPlan(TEST_SCOPE, emptyPolicy);

            assertThat(plan.getPreValidators()).hasSize(1)
                    .extracting(ValidatorStep::name)
                    .contains("MyCustomValidator");
            assertThat(plan.getPostValidators()).hasSize(1)
                    .extracting(ValidatorStep::name)
                    .contains("MyCustomValidator");

        }

        static class MyValidatorFunction implements PolicyValidatorRule<TestContext> {

            @Override
            public Boolean apply(Policy policy, TestContext policyContext) {
                return true;
            }

            @Override
            public String name() {
                return "MyCustomValidator";
            }
        }
    }

    static class TestContext extends PolicyContextImpl {

        @Override
        public String scope() {
            return TEST_SCOPE;
        }
    }

    private static class UnboundedContext extends PolicyContextImpl {

        @Override
        public String scope() {
            return "unbounded";
        }
    }

}
