package com.UoU._integration;

import com.UoU.core.Noop;
import org.junit.jupiter.api.Test;

class ApplicationTests extends BaseAppIntegrationTest {
  @Test
  void app_shouldLoadWithoutExceptions() {
    Noop.because("if the app loads, it worked!");
  }
}
