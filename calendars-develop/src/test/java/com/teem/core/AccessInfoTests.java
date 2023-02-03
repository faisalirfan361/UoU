package com.UoU.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.TestData;
import com.UoU.core.exceptions.NotFoundException;
import com.UoU.core.exceptions.ReadOnlyException;
import org.junit.jupiter.api.Test;

class AccessInfoTests {

  @Test
  void methodChaining_works() {
    var orgId = TestData.orgId();
    var info = new TestAccessInfo(orgId, false);

    assertThatCode(
        () -> info
            .requireOrgOrThrowNotFound(orgId)
            .requireOrgOrThrow(orgId, () -> new RuntimeException())
            .requireWritable())
        .doesNotThrowAnyException();
  }

  @Test
  void isOrg_works() {
    var info = new TestAccessInfo(new OrgId("x"), false);
    assertThat(info.isOrg(new OrgId("x"))).isTrue();
    assertThat(info.isOrg(new OrgId("y"))).isFalse();
  }

  @Test
  void requireOrgOrThrow_shouldNotThrowWhenOrgValueMatches() {
    var info = new TestAccessInfo(new OrgId("x"), false);

    assertThatCode(
        () -> info.requireOrgOrThrow(new OrgId("x"), () -> new RuntimeException()))
        .doesNotThrowAnyException();
  }

  @Test
  void requireOrgOrThrow_shouldThrowSuppliedException() {
    var exception = new RuntimeException("test");
    var info = new TestAccessInfo(TestData.orgId(), false);

    assertThatCode(
        () -> info.requireOrgOrThrow(TestData.orgId(), () -> exception))
        .isSameAs(exception);
  }

  @Test
  void requireOrgOrThrowNotFound_shouldThrowWithName() {
    var info = new TestAccessInfo(TestData.orgId(), false);

    assertThatCode(
        () -> info.requireOrgOrThrowNotFound(TestData.orgId()))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining(info.name());
  }

  @Test
  void requireWritable_shouldNotThrowWhenNotReadOnly() {
    var info = new TestAccessInfo(TestData.orgId(), false);

    assertThatCode(() -> info.requireWritable()).doesNotThrowAnyException();
  }

  @Test
  void requireWritable_shouldThrowWithNameWhenReadOnly() {
    var info = new TestAccessInfo(TestData.orgId(), true);

    assertThatCode(() -> info.requireWritable())
        .isInstanceOf(ReadOnlyException.class)
        .hasMessageContaining(info.name());
  }

  record TestAccessInfo(OrgId orgId, boolean isReadOnly) implements AccessInfo<TestAccessInfo> {
    public static final String NAME = "Test Thing";

    @Override
    public String name() {
      return NAME;
    }
  }
}
