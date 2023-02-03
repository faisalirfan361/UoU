package com.UoU.app.docs;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.UoU.app.ErrorResponse;
import com.UoU.core.Fluent;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.val;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.ModelAndView;

/**
 * Configures OpenAPI for our generated API docs.
 */
@Configuration
@OpenAPIDefinition(
    security = @SecurityRequirement(name = "bearerAuth"),
    info = @Info(
        title = "Calendars API",
        version = "v1",
        description = "See also &rarr; **[Calendars API Overview](/docs/v1.html)**"
    ))
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT bearer token Authorization header",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT")
class OpenApiConfig {
  private static final String PACKAGE_NAME = OpenApiConfig.class.getPackageName();
  private static final String URL_HTTP = "http://";
  private static final String URL_HTTPS = "https://";

  /**
   * Creates an api customizer that customizes schemas and runs any other custom logic.
   */
  @Bean
  public OpenApiCustomiser openApiCustomizer(
      SchemaExt.Processor schemaExtProcessor,
      @Value("${docs.force-https:true}") boolean forceHttps) {
    return openApi -> {
      // Optionally force https on all server urls, which are usually auto-generated urls.
      // This is intended for non-local environments because some of our load balancers do not
      // pass x-forwarded-scheme/proto headers properly as https, so generated urls are wrong.
      // Devops says it's an issue with Azure Application Gateway, and they don't know how to fix.
      if (forceHttps && openApi.getServers() != null) {
        openApi.getServers().forEach(server -> {
          val url = server.getUrl();
          if (url != null && url.startsWith(URL_HTTP)) {
            server.setUrl(URL_HTTPS + url.substring(URL_HTTP.length()));
          }
        });
      }

      schemaExtProcessor.process(openApi.getComponents().getSchemas());
    };
  }

  /**
   * Creates an operation customizer that adds 401 and 403 to responses.
   */
  @Bean
  public OperationCustomizer operationCustomizer() {
    val errorResponseSchema = "#/components/schemas/" + ErrorResponse.class.getSimpleName();
    val errorContent = new Content().addMediaType(
        APPLICATION_JSON_VALUE,
        new MediaType().schema(new Schema().$ref(errorResponseSchema)));

    val htmlContent = new Content().addMediaType(
        TEXT_HTML_VALUE,
        new MediaType());

    return (operation, handlerMethod) -> {
      // Only customize our stuff in this package so things like actuator are untouched.
      val packageName = handlerMethod.getBeanType().getPackageName();
      if (!packageName.startsWith(PACKAGE_NAME)) {
        return operation;
      }

      // For html controller methods that return ModelAndView, use text/html defaults.
      if (handlerMethod.getReturnType().getParameterType() == ModelAndView.class) {
        return operation
            .responses(Fluent.of(operation
                    .getResponses()
                    .addApiResponse("400", new ApiResponse()
                        .description("Bad Request")
                        .content(htmlContent))
                    .addApiResponse("404", new ApiResponse()
                        .description("Not Found")
                        .content(htmlContent))
                    .addApiResponse("500", new ApiResponse()
                        .description("Server Error")
                        .content(htmlContent)))
                .also(x -> x.remove("403"))
                .get());
      }

      // Else assume the default JSON api behavior:
      return operation
          .responses(operation
              .getResponses()
              .addApiResponse("401", new ApiResponse()
                  .description("Unauthenticated")
                  .content(errorContent))
              .addApiResponse("403", new ApiResponse()
                  .description("Unauthorized")
                  .content(errorContent)));
    };
  }

  /**
   * Configures a ModelResolver with an ObjectMapper that uses our Spring-configured settings.
   *
   * <p>The ObjectMapper instance is a separate instance for the ModelResolver, but we configure
   * it to match Spring's ObjectMapper. The ModelResolver tweaks the ObjectMapper a bit, so it
   * needs to have its own instance. For example, it uses @Schema annotations for deserialization,
   * so if we were to use the global ObjectMapper, things like @Schema(required = true) would be
   * enforced for DTO deserialization (when instead we want that validation in the core package).
   */
  @Bean
  public ModelResolver modelResolver(Jackson2ObjectMapperBuilder objectMapperBuilder) {
    return Fluent
        .of(new ObjectMapper())
        .also(objectMapperBuilder::configure)
        .map(ModelResolver::new)
        .get();
  }

  /**
   * Optionally move all public operations into a group (usually for local only).
   *
   * <p>This is done here instead of application.yml because you can't set a customizer in the yml.
   */
  @Bean
  @ConditionalOnProperty("docs.enable-public-private-groups")
  public GroupedOpenApi publicOpenApi(
      @Value("${docs.private-paths}") String[] privatePaths,
      OpenApiCustomiser openApiCustomizer,
      OperationCustomizer operationCustomizer) {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToExclude(privatePaths)
        .addOpenApiCustomiser(openApiCustomizer)
        .addOperationCustomizer(operationCustomizer)
        .build();
  }

  /**
   * Optionally move all private operations into a group (usually for local only).
   *
   * <p>This is done here instead of application.yml because you can't set a customizer in the yml.
   */
  @Bean
  @ConditionalOnProperty("docs.enable-public-private-groups")
  public GroupedOpenApi privateOpenApi(
      @Value("${docs.private-paths}") String[] privatePaths,
      OpenApiCustomiser openApiCustomizer,
      OperationCustomizer operationCustomizer) {
    return GroupedOpenApi.builder()
        .group("private")
        .pathsToMatch(privatePaths)
        .addOpenApiCustomiser(openApiCustomizer)
        .addOperationCustomizer(operationCustomizer)
        .build();
  }
}
