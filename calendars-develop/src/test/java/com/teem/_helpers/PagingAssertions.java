package com.UoU._helpers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.UoU.core.PagedItems;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;

public class PagingAssertions {

  @SafeVarargs
  public static <T, ValueT> void assertPagesContainValues(
      Function<T, ValueT> valueExtractor, Pair<PagedItems<T>, List<ValueT>>... pages) {

    for (var i = 0; i < pages.length; i++) {
      var page = pages[i].getLeft();
      var expectedValues = pages[i].getRight().toArray();
      var isLastPage = i == pages.length - 1;

      assertThat(page.hasNextPage())
          .as("Should have a next page: " + !isLastPage)
          .isEqualTo(!isLastPage);
      assertThat(page.items().stream().map(valueExtractor).toList()).asList()
          .as("Should contain the expected values in order")
          .containsExactly(expectedValues);
    }
  }
}
