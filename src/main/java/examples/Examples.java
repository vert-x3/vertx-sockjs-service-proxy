package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.serviceproxy.ServiceBinder;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {

  public void serviceAndSockJS(Vertx vertx) {
    SomeDatabaseService service = new SomeDatabaseServiceImpl();
    new ServiceBinder(vertx)
      .setAddress("database-service-address")
      .register(SomeDatabaseService.class, service);

    Router router = Router.router(vertx);
    // Allow events for the designated addresses in/out of the event bus bridge
    SockJSBridgeOptions opts = new SockJSBridgeOptions()
        .addInboundPermitted(new PermittedOptions()
            .setAddress("database-service-address"))
        .addOutboundPermitted(new PermittedOptions()
            .setAddress("database-service-address"));

    // Create the event bus bridge and add it to the router.
    router.route("/eventbus/*").subRouter(SockJSHandler.create(vertx).bridge(opts));

    vertx.createHttpServer().requestHandler(router).listen(8080);
  }
}
