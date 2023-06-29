package examples;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@ProxyGen
@VertxGen
public interface SomeDatabaseService {

  Future<Void> save(String collection, JsonObject document);

  Future<Void> foo(String collection, JsonObject document);

  static SomeDatabaseService createProxy(Vertx vertx, String address) {
    return null;
  }
}
