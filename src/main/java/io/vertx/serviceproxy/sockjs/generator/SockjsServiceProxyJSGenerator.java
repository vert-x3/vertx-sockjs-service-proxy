package io.vertx.serviceproxy.sockjs.generator;

import io.vertx.codegen.Helper;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.*;
import io.vertx.codegen.writer.CodeWriter;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.vertx.codegen.type.ClassKind.*;

public class SockjsServiceProxyJSGenerator extends AbstractSockjsServiceProxyGenerator {
  SockjsServiceProxyJSGenerator() {
    this.name = "sockjs_service_proxies";
    this.kinds = Collections.singleton("proxy");
  }

  @Override
  public String filename(ProxyModel model) {
    ClassTypeInfo type = model.getType();
    return "resources/" + type.getModuleName() + "-js/" + Helper.convertCamelCaseToUnderscores(type.getRaw().getSimpleName()) + "-proxy.js";
  }

  @Override
  public String render(ProxyModel model, int index, int size, Map<String, Object> session) {
    StringWriter sw = new StringWriter();
    CodeWriter writer = new CodeWriter(sw);
    ClassTypeInfo type = model.getType();
    String simpleName = type.getSimpleName();
    genLicenses(writer);
    writer.println();
    writer.format("/** @module %s */", getModuleName(type)).println();

    //generate the module loader shim
    writer.println("!function (factory) {");
    writer.indent().println("if (typeof require === 'function' && typeof module !== 'undefined') {");
    writer.indent().println("factory();");
    writer.unindent().println("} else if (typeof define === 'function' && define.amd) {");
    writer.indent().println("// AMD loader");
    writer.format("define('%s-proxy', [], factory);", getModuleName(type));
    writer.println();
    writer.unindent().println("} else {");
    writer.indent().println("// plain old include");
    writer.format("%s = factory();", simpleName).println();
    writer.unindent().println("}");
    writer.unindent().println("}(function () {");
    writer.indent();
    //Generate the requires
    for (ApiTypeInfo referencedType : model.getReferencedTypes()) {
      if(referencedType.isProxyGen()) {
        String refedType = referencedType.getSimpleName();
        writer.format("var %s = require('%s-proxy');", refedType, getModuleName(referencedType)).println();
      }
    }
    writer.println();
    genDoc(model, writer);
    //The constructor
    writer.format("var %s = function(eb, address) {", simpleName).println();
    writer.indent().println("var j_eb = eb;");
    writer.println("var j_address = address;");
    writer.println("var closed = false;");
    writer.println("var that = this;");
    writer.println("var convCharCollection = function(coll) {");
    writer.indent().println("var ret = [];");
    writer.println("for (var i = 0;i < coll.length;i++) {");
    writer.indent().println("ret.push(String.fromCharCode(coll[i]));");
    writer.unindent().println("}");
    writer.println("return ret;");
    writer.unindent().println("};");

    //Apply any supertypes
    for (TypeInfo superType : model.getSuperTypes()) {
      writer.format("%s.call(this, j_val);", superType.getRaw().getSimpleName()).println();
    }
    writer.println();

    //Now iterate through each unique method
    for (String methodName : model.getMethodMap().keySet()) {
      //Call out to actually generate the method, we only consider non static methods here
      genMethod(model, methodName, false, this::methodFilter, writer);
    }
    writer.unindent().println("};");
    writer.println();

    //Iterate through the methods again, this time only considering the static ones
    for (String methodName : model.getMethodMap().keySet()) {
      //Call out to generate the static method
      genMethod(model, methodName, true, this::methodFilter, writer);
    }
    writer.println("if (typeof exports !== 'undefined') {");
    writer.indent().println("if (typeof module !== 'undefined' && module.exports) {");
    writer.indent().format("exports = module.exports = %s;", simpleName).println();
    writer.unindent().println("} else {");
    writer.indent().format("exports.%s = %s;", simpleName, simpleName).println();
    writer.unindent().println("}");
    writer.unindent().println("} else {");
    writer.indent().format("return %s;", simpleName).println();
    writer.unindent().println("}");
    writer.unindent().print("});");
    return sw.toString();
  }

  @Override
  protected String convReturn(ProxyModel model, MethodInfo method, TypeInfo returnType, String templ) {
    return templ;
  }

  @Override
  protected void genMethodAdapter(ProxyModel model, MethodInfo m, CodeWriter writer) {
    ProxyMethodInfo method = (ProxyMethodInfo) m;
    writer.println("if (closed) {");
    writer.indent().println("throw new Error('Proxy is closed');");
    writer.unindent().println("}");
    genMethodCall(method, writer);
    writer.println(";");
    if (method.isProxyClose()) {
      writer.println("closed = true;");
    }
    if (method.isFluent()) {
      writer.println("return that;");
    } else {
      writer.println("return;");
    }
  }
  private void genMethodCall(MethodInfo method, PrintWriter writer){
    List<ParamInfo> params = method.getParams();
    int psize = params.size();
    ParamInfo lastParam = psize > 0 ? params.get(psize - 1) : null;
    boolean hasResultHandler = lastParam != null && lastParam.getType().getKind() == HANDLER && ((ParameterizedTypeInfo) lastParam.getType()).getArg(0).getKind() == ASYNC_RESULT;
    if (hasResultHandler) {
      psize--;
    }
    writer.print("j_eb.send(j_address, {");
    boolean first = true;
    for (int pcnt = 0; pcnt < psize; pcnt++) {
      if (first) {
        first = false;
      } else {
        writer.print(", ");
      }
      ParamInfo param = params.get(pcnt);
      String paramTypeName = param.getType().getName();
      String paramName = param.getName();
      writer.format("\"%s\":", paramName);
      if ("java.lang.Character".equals(paramTypeName) || "char".equals(paramTypeName)) {
        writer.format("__args[%d].charCodeAt(0)", pcnt);
      } else {
        writer.format("__args[%d]", pcnt);
      }
    }
    writer.format("}, {\"action\":\"%s\"}", method.getName());
    if (hasResultHandler) {
      ParameterizedTypeInfo handlerType = (ParameterizedTypeInfo) lastParam.getType();
      ParameterizedTypeInfo asyncResultType = (ParameterizedTypeInfo) handlerType.getArg(0);
      TypeInfo resultType = asyncResultType.getArg(0);
      writer.format(", function(err, result) { __args[%d](err, result && ", psize);
      ClassKind resultKind = resultType.getKind();
      if (resultType.getKind() == API) {
        writer.format("new %s(j_eb, result.headers.proxyaddr)", resultType.getSimpleName());
      } else if ((resultKind == LIST || resultKind == SET) && "java.lang.Character".equals(((ParameterizedTypeInfo)resultType).getArg(0).getName())) {
        writer.print("convCharCollection(result.body)");
      } else {
        writer.print("result.body");
      }
      writer.print("); }");
    }
    writer.print(")");
  }
}
