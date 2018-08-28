package io.vertx.serviceproxy.sockjs.generator;

import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.lang.js.generator.AbstractJSClassGenerator;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.util.Collections;

abstract class AbstractSockjsServiceProxyGenerator extends AbstractJSClassGenerator<ProxyModel> {
  AbstractSockjsServiceProxyGenerator(){
    this.name = "sockjs_service_proxies";
    this.kinds = Collections.singleton("proxy");
  }

  boolean methodFilter(MethodInfo m){
    ProxyMethodInfo method = (ProxyMethodInfo) m;
    return !method.isProxyIgnore();
  }
}
