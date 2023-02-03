package com.UoU.app.v1.dtos;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.UoU.core.Fluent;
import com.UoU.infra.json.ObjectMapperBuilderCustomizer;
import java.util.function.BiConsumer;
import lombok.SneakyThrows;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Helpers for testing JSON related things.
 */
public class JsonTestHelper {

  /**
   * Mapper instance for testing json (de)serialization.
   *
   * <p>This should mostly match spring boot, but we won't load the spring application
   * to inject the actual mapper since this should be good enough for unit tests.
   */
  public static final ObjectMapper MAPPER = Fluent
      .of(Jackson2ObjectMapperBuilder.json())
      .also(x -> new ObjectMapperBuilderCustomizer().customize(x))
      .get()
      .build();

  /**
   * Tests that inputJson can be read into a cls instance and then written back to json.
   */
  public static <T> void shouldReadAndWriteJson(String inputJson, Class<T> cls) {
    shouldReadAndWriteJson(inputJson, cls, (json, dto) -> { /* noop */ });
  }

  /**
   * Tests that inputJson can be read into a cls instance and then written back to json, and
   * passes output json and object to outputHandler for further handling/assertions.
   */
  @SneakyThrows
  public static <T> void shouldReadAndWriteJson(
      String inputJson, Class<T> cls, BiConsumer<String, T> outputHandler) {

    var obj = MAPPER.readValue(inputJson, cls);
    var outputJson = MAPPER.writeValueAsString(obj);

    assertThat(outputJson)
        .describedAs("Output json should match input json")
        .isEqualTo(inputJson);

    // Pass output back for additional handling/assertions:
    outputHandler.accept(outputJson, obj);
  }

  @SneakyThrows
  public static <T> void shouldReadAndWriteJson(String inputJson, String expectedJson,
                                                Class<T> cls) {
    var obj = MAPPER.readValue(inputJson, cls);
    var outputJson = MAPPER.writeValueAsString(obj);

    assertThat(outputJson)
        .describedAs("Output json should match expected json")
        .isEqualTo(expectedJson);
  }
}
