package com.sbt.kuberpunk;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Slf4j
@SuppressWarnings("ALL")
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "spring.cloud.gateway.discovery.locator.routeToLocalService")
public class RouteToLocalServicesFilter implements GatewayFilter, Ordered {

  private static final Logger LOGGER = LoggerFactory.getLogger(RouteToLocalServicesFilter.class);

  
  @Autowired
  private ClientConfig config;

  //private static final String PING_CONTEXT = "/actuator/info";
  public static final int ROUTE_TO_LOCAL_FILTER_ORDER = 20_000;

  private final DiscoveryClient discoveryClient;
  private final Map<String, URI> cache = new ConcurrentHashMap<>();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String serviceName = getServiceName(exchange);

    String userIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    String cacheKey = serviceName + ":" + userIp;


    Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);

    //route.getUri().getHost().toLowerCase();
    LOGGER.info("route.getUri" +route.getUri());
    LOGGER.info("route.port" +route.getUri().getPort());
    LOGGER.info("route.getUri" +route.getUri().getHost());



     LOGGER.info("userIp" + userIp);
     LOGGER.info("getAddress" + exchange.getRequest().getRemoteAddress().getAddress());
    try {
       LOGGER.info("getLocalHost" + exchange.getRequest().getRemoteAddress().getAddress().getLocalHost());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
     LOGGER.info("getHostAddress" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
     LOGGER.info("getCanonicalHostName" + exchange.getRequest().getRemoteAddress().getAddress().getCanonicalHostName());
     LOGGER.info("getURI" + exchange.getRequest().getURI());
     LOGGER.info("getLoopbackAddress" + exchange.getRequest().getRemoteAddress().getAddress().getLoopbackAddress());
     LOGGER.info("getLoopbackAddress" + exchange.getRequest().getRemoteAddress().getAddress().getLoopbackAddress());


    log.info("getURI" + exchange.getRequest().getURI());


    String localRout  = System.getenv("ROUTETOREDIRECT");
    LOGGER.info("redirected to" + localRout + "by KuberPunk");

    ///URI localRoutUri = URI.create(localRout);

    if (  true
          //  localRout.equals(userIp)
    )
    {

      UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
      UriComponents modifiedUriString = builder.scheme("http").host(localRout).port("7776").path("").build();
      URI modifiedUri = modifiedUriString.toUri();

      LOGGER.info("modifiedUri Port="  + modifiedUri);
    /// направить на локал  routeTo(localUri, exchange);
      routeTo(modifiedUri, exchange);

      if (isAlive(modifiedUri)) {
        routeTo(modifiedUri, exchange);
      }
    } else {
      ///направить на бывший
      discoveryClient.getInstances(serviceName+"kuberpunk-old")
              .stream()
              .filter(i -> i.getHost().equals(userIp))
              .findFirst()
              .ifPresent(localInstance ->
                      cache.put(cacheKey, localInstance.getUri())
              );
      URI oldServiceUri = cache.get(cacheKey);
      if (isAlive(oldServiceUri)) {
        routeTo(oldServiceUri, exchange);
      }
    }


    return chain.filter(exchange);
  }

  private String getServiceName(ServerWebExchange exchange) {
    Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
    return route == null ? null : route.getUri().getHost().toLowerCase();
  }

  protected boolean isAlive(URI uri) {
    if (uri == null) {
      LOGGER.info("uri is null");
      return false;
    }

    try {
      LOGGER.info("Try to connect" + uri + "by KuberPunk");
      URLConnection urlConnection = new URL(uri.toString()).openConnection();
      urlConnection.setConnectTimeout(100);
      urlConnection.setReadTimeout(100);
      urlConnection.connect();
      return true;
    } catch (Exception e) {
      log.info("Failed to ping {}", uri);
      return false;
    }
  }

  @SneakyThrows
  private void routeTo(URI uri, ServerWebExchange exchange) {
    LOGGER.info("Try to connect in routto" + uri + "by KuberPunk");
    URLConnection urlConnection = new URL(uri.toString()).openConnection();
    urlConnection.setConnectTimeout(100);
    urlConnection.setReadTimeout(100);
    urlConnection.connect();

    URI destination = new URI(uri.toString() + exchange.getRequest().getPath());
    log.info("Routing {} to {}", exchange.getRequest().getURI(), destination);
    exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, destination);
  }

  @Override
  public int getOrder() {
    return ROUTE_TO_LOCAL_FILTER_ORDER;
  }
}