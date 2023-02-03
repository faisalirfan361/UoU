package com.UoU.core.validation;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.UoU._helpers.TestData;
import com.UoU.core.Fluent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidatorContext;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidatorCheckerTests {

  private String failMessage;
  private ConstraintValidatorContext contextMock;
  private Map<String, List<String>> violationResults;

  @BeforeEach
  void setUp() {
    failMessage = "fail-" + TestData.uuidString();
    violationResults = new HashMap<>();

    // Configure a validation context mock that adds calls to addConstraintViolation() to results.
    contextMock = mock(ConstraintValidatorContext.class);
    when(contextMock.buildConstraintViolationWithTemplate(anyString())).then(templateInv -> Fluent
        .of(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class))
        .also(builder -> when(builder.addPropertyNode(anyString())).then(propInv -> Fluent
            .of(mock(ConstraintValidatorContext.ConstraintViolationBuilder
                .NodeBuilderCustomizableContext.class))
            .also(customizer -> when(customizer.addConstraintViolation()).then(addInv -> {
              val template = (String) templateInv.getArgument(0);
              val prop = (String) propInv.getArgument(0);
              if (!violationResults.containsKey(prop)) {
                violationResults.put(prop, new ArrayList<>());
              }
              violationResults.get(prop).add(template);
              return null;
            }))
            .get()))
        .get());
  }

  @Test
  void isValid_shouldAddViolationsToConstraintValidatorContext() {
    val checker = new ValidatorChecker<>()
        .add((val, ctx) -> false, "property", failMessage)
        .add((val, ctx) -> false, "property", val -> failMessage)
        .add((val, ctx) -> true, "pass", "pass");

    val result = checker.isValid(new Object(), contextMock);

    assertThat(result).isFalse();
    assertThat(violationResults).containsOnlyKeys("property");
    assertThat(violationResults.get("property")).containsExactly(failMessage, failMessage);
  }

  @Test
  void isValid_shouldAddSingleFailureToMultipleProperties() {
    val checker = new ValidatorChecker<>()
        .add(
            (val, ctx) -> false,
            Pair.of("prop1", val -> failMessage), Pair.of("prop2", val -> failMessage));

    val result = checker.isValid(new Object(), contextMock);

    assertThat(result).isFalse();
    assertThat(violationResults).containsOnlyKeys("prop1", "prop2");
    assertThat(violationResults.get("prop1")).containsExactly(failMessage);
    assertThat(violationResults.get("prop2")).containsExactly(failMessage);
  }

  @Test
  void isValid_failIsValidCheck_shouldAddCustomMessage() {
    val checker = new ValidatorChecker<>()
        .add((val, ctx) -> ValidatorChecker.failIsValidCheck(failMessage),
            "property",
            "default message, not expected to be used");

    val result = checker.isValid(new Object(), contextMock);

    assertThat(result).isFalse();
    assertThat(violationResults).containsOnlyKeys("property");
    assertThat(violationResults.get("property")).containsExactly(failMessage);
  }

  @Test
  void isValid_failIsValidCheck_withBlankMessage_shouldFallbackToDefaultMessage() {
    val checker = new ValidatorChecker<>()
        .add((val, ctx) -> ValidatorChecker.failIsValidCheck(" "), // blank message
            "property",
            failMessage);

    val result = checker.isValid(new Object(), contextMock);

    assertThat(result).isFalse();
    assertThat(violationResults).containsOnlyKeys("property");
    assertThat(violationResults.get("property")).containsExactly(failMessage);
  }

  @Test
  void isValid_failIsValidCheck_shouldAddCustomMessageToMulitpleProperties() {
    val checker = new ValidatorChecker<>()
        .add((val, ctx) -> ValidatorChecker.failIsValidCheck(failMessage),
            Pair.of("prop1", val -> "default msg"), Pair.of("prop2", val -> "default msg"));

    val result = checker.isValid(new Object(), contextMock);

    assertThat(result).isFalse();
    assertThat(violationResults).containsOnlyKeys("prop1", "prop2");
    assertThat(violationResults.get("prop1")).containsExactly(failMessage);
    assertThat(violationResults.get("prop2")).containsExactly(failMessage);
  }
}
