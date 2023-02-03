package com.UoU._fakes;

import com.microsoft.graph.http.CoreHttpProvider;
import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.httpcore.RedirectHandler;
import com.microsoft.graph.httpcore.RetryHandler;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.serializer.DefaultSerializer;
import com.UoU.core.conferencing.teams.AuthHttpInterceptor;
import lombok.val;
import okhttp3.Request;

public class FakeGraphServiceClient extends GraphServiceClient<Request> {

  public FakeGraphServiceClient(AuthHttpInterceptor authHttpInterceptor) {
    // Default to localhost so graph api url won't be used.
    this(authHttpInterceptor, "http://localhost");
  }

  public FakeGraphServiceClient(AuthHttpInterceptor authHttpInterceptor, String baseUrl) {
    val logger = new DefaultLogger();
    val serializer = new DefaultSerializer(logger);
    val http = HttpClients.custom()
        .addInterceptor(authHttpInterceptor)
        .addInterceptor(new RetryHandler())
        .addInterceptor(new RedirectHandler())
        .build();

    setServiceRoot(baseUrl);
    setLogger(logger);
    setSerializer(serializer);
    setHttpProvider(new CoreHttpProvider(serializer, logger, http));
  }
}
