package com.UoU.core.conferencing.teams;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.httpcore.RedirectHandler;
import com.microsoft.graph.httpcore.RetryHandler;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
class TeamsConfig {

  /**
   * Creates a graph client that should be the single client used for the entire app.
   *
   * <p>We only want one graph client and underlying OkHttp client because that's how these things
   * were designed for best performance. Mainly, OkHttp should be a single instance to reuse
   * resources like connection pools.
   * <a href="https://square.github.io/okhttp/4.x/okhttp/okhttp3/-ok-http-client/#okhttpclients-should-be-shared">See the docs.</a>
   */
  @Bean
  @Scope(SCOPE_SINGLETON) // same as default, but explicit here because it's very important
  public GraphServiceClient<Request> graphServiceClient(AuthHttpInterceptor authHttpInterceptor) {
    return GraphServiceClient.builder()
        .httpClient(HttpClients.custom()
            .addInterceptor(authHttpInterceptor) // replaces default graph AuthenticationHandler
            .addInterceptor(new RetryHandler()) // same as default client
            .addInterceptor(new RedirectHandler()) // same as default client
            .build())
        .buildClient();
  }
}
