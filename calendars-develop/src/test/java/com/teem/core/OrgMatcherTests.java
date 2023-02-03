package com.UoU.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import com.UoU.core.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;

class OrgMatcherTests {

  @Test
  void matchOrThrow_shouldNotThrowWhenOrgValueMatches() {
    assertThatCode(
        () -> OrgMatcher.matchOrThrow(new OrgId("x"), new OrgId("x"), () -> new RuntimeException()))
        .doesNotThrowAnyException();
  }

  @Test
  void matchOrThrow_shouldThrowSuppliedException() {
    var exception = new RuntimeException("test");

    assertThatCode(
        () -> OrgMatcher.matchOrThrow(new OrgId("x"), new OrgId("y"), () -> exception))
        .isSameAs(exception);
  }

  @Test
  void matchOrThrowNotFound_shouldThrowWithClassName() {
    var cls = OrgMatcherTests.class;

    assertThatCode(
        () -> OrgMatcher.matchOrThrowNotFound(new OrgId("x"), new OrgId("y"), cls))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(cls.getSimpleName());
  }
}
