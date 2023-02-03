package com.UoU.core.accounts;

import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationFails;
import static com.UoU.core._helpers.ValidationAssertions.assertThatValidationPasses;

import com.UoU._helpers.TestData;
import com.UoU.core.OrgId;
import com.UoU.core.SecretString;
import com.UoU.core.auth.AuthMethod;
import java.util.Set;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AccountCreateRequestTests {

  @Test
  void validation_shouldPass() {
    val request = buildValid().build();
    assertThatValidationPasses(request);
  }

  @ParameterizedTest
  @MethodSource
  void validation_shouldFail(Set<String> invalidProps, AccountCreateRequest request) {
    assertThatValidationFails(invalidProps, request);
  }

  private static Stream<Arguments> validation_shouldFail() { // test data
    return Stream.of(
        Arguments.of(
            Set.of("id", "orgId", "email", "name", "authMethod", "accessToken"),
            AccountCreateRequest.builder().build()), // everything is null
        Arguments.of(
            Set.of("orgId.value", "email", "name"),
            buildValid()
                .orgId(new OrgId(" "))
                .email("x")
                .name(" ")
                .build()),
        Arguments.of(
            Set.of("email"),
            buildValid()
                .email("")
                .build()),
        Arguments.of(
            Set.of("email"),
            buildValid()
                .email(" ")
                .build()),
        Arguments.of(
            Set.of("name"),
            buildValid()
                .name("x".repeat(AccountConstraints.NAME_MAX + 1))
                .build())
    );
  }

  private static AccountCreateRequest.Builder buildValid() {
    return AccountCreateRequest.builder()
        .id(TestData.accountId())
        .orgId(TestData.orgId())
        .email(TestData.email())
        .authMethod(AuthMethod.GOOGLE_OAUTH)
        .accessToken(new SecretString("test"))
        .name("Test Account");
  }
}
