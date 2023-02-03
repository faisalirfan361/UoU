package com.UoU.core;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import java.util.List;
import java.util.stream.Collectors;
import lombok.val;
import org.junit.jupiter.api.Test;

class ChecksumTests {

  @Test
  void ctor_shouldRequireValues() {
    assertThatCode(() -> new Checksum()).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void value_shouldAllowNulls() {
    val checksum = new Checksum(null, null);
    assertThat(checksum.getValue()).isNotBlank();
  }

  @Test
  void value_shouldMatchForSameInputValues() {
    val checksum1 = new Checksum("hi", "there").getValue();
    val checksum2 = new Checksum("hi", "there").getValue();

    assertThat(checksum1).isEqualTo(checksum2);
  }

  @Test
  void value_shouldNotMatchForDifferentInputValues() {
    val checksums = List.of(
        new Checksum("hi", "there"),
        new Checksum("hithe", "re"),
        new Checksum("hi", "there", "test "),
        new Checksum("hitheretest"));
    val valueSet = checksums.stream().map(x -> x.getValue()).collect(Collectors.toSet());

    assertThat(valueSet.size()).isEqualTo(checksums.size());
  }
}
