package com.kuberpunk;

import com.kuberpunk.connection.client.ClientHostData;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Slf4j
@SuppressWarnings("ALL")
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(value = "spring.cloud.gateway.discovery.locator.routeToLocalService")
public class RouteToLocalServicesFilter implements GatewayFilter, Ordered {

    @Autowired
    ClientsHolder clientsHolder;
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteToLocalServicesFilter.class);
    public static final int ROUTE_TO_LOCAL_FILTER_ORDER = 20_000;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String serviceName = getServiceName(exchange);
        String requestSourceRoute = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);

        //route.getUri().getHost().toLowerCase();
        LOGGER.info("route.getUri" + route.getUri());
        LOGGER.info("route.port" + route.getUri().getPort());
        LOGGER.info("route.getUri" + route.getUri().getHost());


        LOGGER.info("requestSourceRoute" + requestSourceRoute);
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

        String mainContainerPort = System.getenv("PORT_TO_REDIRECT");


        if (clientsHolder.contains(requestSourceRoute)) {
            sendToSender(exchange, requestSourceRoute, mainContainerPort);
        } else {
            sendToMainContainer(exchange, requestSourceRoute, mainContainerPort);
        }
        return chain.filter(exchange);
    }

    private void sendToSender(ServerWebExchange exchange, String requestSourceRoute, String mainContainerPort) {
        Integer portForRedirect = clientsHolder.getPort(requestSourceRoute);
        ClientHostData senderHostFromHolder = new ClientHostData(requestSourceRoute, portForRedirect);

        LOGGER.info("redirecting to {}:{} by KuberPunk", requestSourceRoute, portForRedirect);
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        UriComponents modifiedUriString = builder.scheme("http").host(requestSourceRoute).port(portForRedirect).path("").build();
        URI modifiedUri = modifiedUriString.toUri();
        LOGGER.debug("Try to send on modified uri: {}", modifiedUri);
        /// направить на локал  routeTo(localUri, exchange);
        if (isAlive(modifiedUri)) {
            routeTo(modifiedUri, exchange);
        } else {
            sendToMainContainer(exchange, requestSourceRoute, mainContainerPort);
        }
    }

    private void sendToMainContainer(ServerWebExchange exchange, String requestSourceRoute, String mainContainerPort) {
        ///направить на бывший
        LOGGER.info("Try to send to main container on port {}", mainContainerPort);
        LOGGER.info("Service with port {} was not available on {}", mainContainerPort, requestSourceRoute);

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        UriComponents mainContainerUriString = builder.scheme("http").host("localhost").port(mainContainerPort).path("").build();
        URI oldServiceUri = mainContainerUriString.toUri();
        if (isAlive(oldServiceUri)) {
            routeTo(oldServiceUri, exchange);
        }
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
            LOGGER.info("Chieck if uri is available, try to ping {}", uri);
            URLConnection urlConnection = new URL(uri.toString()).openConnection();
            urlConnection.setConnectTimeout(100);
            urlConnection.setReadTimeout(100);
            urlConnection.connect();
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to ping {}", uri);
            return false;
        }
    }

    @SneakyThrows
    private void routeTo(URI uri, ServerWebExchange exchange) {
        LOGGER.info("Try to send on uri:{}", uri);
        URLConnection urlConnection = new URL(uri.toString()).openConnection();
        urlConnection.setConnectTimeout(100);
        urlConnection.setReadTimeout(100);
        URI destination = null;
        try {
            urlConnection.connect();
            destination = new URI(uri.toString() + exchange.getRequest().getPath());
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Exception while connecting with uri: {}. {}", uri, e);
        }
        LOGGER.info("Routing {} to {}", exchange.getRequest().getURI(), destination);
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, destination);
    }

    @Override
    public int getOrder() {
        return ROUTE_TO_LOCAL_FILTER_ORDER;
    }
}