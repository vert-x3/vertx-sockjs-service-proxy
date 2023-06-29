package io.vertx.serviceproxy.sockjs.generator;

import io.vertx.codegen.*;
import io.vertx.codegen.doc.Token;
import io.vertx.codegen.format.CamelCase;
import io.vertx.codegen.format.SnakeCase;
import io.vertx.codegen.type.*;
import io.vertx.codegen.writer.CodeWriter;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

  /**
   * Generate the module name of a type
   */
  private String getModuleName(ClassTypeInfo type) {
    return type.getModuleName() + "-js/" + CamelCase.INSTANCE.to(SnakeCase.INSTANCE, type.getSimpleName());
  }

  private void genMethod(ProxyModel model, String methodName, boolean genStatic, @SuppressWarnings("SameParameterValue") Predicate<MethodInfo> methodFilter, CodeWriter writer) {
    ClassTypeInfo type = model.getType();
    String simpleName = type.getSimpleName();
    Map<String, List<MethodInfo>> methodsByName = model.getMethodMap();
    List<MethodInfo> methodList = methodsByName.get(methodName);
    if (methodFilter != null) {
      List<MethodInfo> methodTmpl = methodList;
      methodList = new ArrayList<>();
      for (MethodInfo method : methodTmpl) {
        if (methodFilter.test(method)) {
          methodList.add(method);
        }
      }
    }
    if (methodList.size() > 0) {
      boolean overloaded = methodList.size() > 1;
      MethodInfo method = methodList.get(methodList.size() - 1);
      if (genStatic == method.isStaticMethod()) {
        writer.println("/**");
        if (method.getDoc() != null) {
          Token.toHtml(method.getDoc().getTokens(), "", this::renderLinkToHtml, "\n", writer);
        }
        writer.println();
        writer.print(" ");
        if (genStatic) {
          writer.format("@memberof module:%s", getModuleName(type)).println();
        } else {
          writer.println("@public");
        }
        boolean first = true;
        for (ParamInfo param : method.getParams()) {
          if (first) {
            first = false;
          } else {
            writer.println();
          }
          writer.format(" @param %s {%s} ", param.getName(), getJSDocType(param.getType()));
          if (param.getDescription() != null) {
            Token.toHtml(param.getDescription().getTokens(), "", this::renderLinkToHtml, "", writer);
            writer.print(" ");
          }
        }
        writer.println();

        if (method.getReturnType().getKind() != VOID) {
          writer.format(" @return {%s}", getJSDocType(method.getReturnType()));
          if (method.getReturnDescription() != null) {
            writer.print(" ");
            Token.toHtml(method.getReturnDescription().getTokens(), "", this::renderLinkToHtml, "", writer);
          }
          writer.println();
        }
        writer.println(" */");

        writer.format("%s.%s = ", genStatic ? simpleName : "this", methodName);
        if (overloaded) {
          writer.println(" function() {");
        } else {
          writer.format(" function(%s) {\n", (method.getParams().stream().map(ParamInfo::getName).collect(Collectors.joining(", "))));
        }
        int mcnt = 0;
        writer.indent();
        writer.println("var __args = arguments;");
        for (MethodInfo m : methodList) {
          writer.print(mcnt++ == 0 ? "if" : "else if");
          int paramSize = m.getParams().size();
          if (m.getKind() == MethodKind.FUTURE) {
            writer.format(" (__args.length === %s", paramSize + 1);
          } else {
            writer.format(" (__args.length === %s", paramSize);
          }
          int cnt = 0;
          if (paramSize > 0) {
            writer.print(" && ");
          }
          first = true;
          for (ParamInfo param : m.getParams()) {
            if (first) {
              first = false;
            } else {
              writer.print(" && ");
            }
            switch (param.getType().getKind()) {
              case PRIMITIVE:
              case BOXED_PRIMITIVE:
                if (param.isNullable()) {
                  writer.print("(");
                }
                writer.format("typeof __args[%s] ===", cnt);
                String paramSimpleName = param.getType().getSimpleName();
                if ("boolean".equalsIgnoreCase(paramSimpleName)) {
                  writer.print("'boolean'");
                } else if ("char".equals(paramSimpleName) || "Character".equals(paramSimpleName)) {
                  writer.print("'string'");
                } else {
                  writer.print("'number'");
                }
                if (param.isNullable()) {
                  writer.format(" || __args[%s] == null)", cnt);
                }
                break;
              case STRING:
              case ENUM:
                if (param.isNullable()) {
                  writer.print("(");
                }
                writer.format("typeof __args[%s] === 'string'", cnt);
                if (param.isNullable()) {
                  writer.format(" || __args[%s] == null)", cnt);
                }
                break;
              case CLASS_TYPE:
                writer.format("typeof __args[%s] === 'function'", cnt);
                break;
              case API:
                writer.format("typeof __args[%s] === 'object' && ", cnt);
                if (param.isNullable()) {
                  writer.format("(__args[%s] == null || ", cnt);
                }
                writer.format("__args[%s]._jdel", cnt);
                if (param.isNullable()) {
                  writer.print(")");
                }
                break;
              case JSON_ARRAY:
              case LIST:
              case SET:
                writer.format("typeof __args[%s] === 'object' && ", cnt);
                if (param.isNullable()) {
                  writer.print("(");
                }
                writer.format("__args[%s] instanceof Array", cnt);
                if (param.isNullable()) {
                  writer.format(" || __args[%s] == null)", cnt);
                }
                break;
              case HANDLER:
                if (param.isNullable()) {
                  writer.print("(");
                }
                writer.format("typeof __args[%s] === 'function'", cnt);
                if (param.isNullable()) {
                  writer.format(" || __args[%s] == null)", cnt);
                }
                break;
              case OBJECT:
                if (param.getType().isVariable() && ((TypeVariableInfo) param.getType()).isClassParam()) {
                  writer.format("j_%s.accept(__args[%s])", param.getType().getName(), cnt);
                } else {
                  writer.format("typeof __args[%s] !== 'function'", cnt);
                }
                break;
              case FUNCTION:
                writer.format("typeof __args[%s] === 'function'", cnt);
                break;
              case THROWABLE:
                writer.format("typeof __args[%s] === 'object'", cnt);
                break;
              default:
                if (!param.isNullable()) {
                  writer.print("(");
                }
                writer.format("typeof __args[%s] === 'object'", cnt);
                if (!param.isNullable()) {
                  writer.format(" && __args[%s] != null)", cnt);
                }
            }
            cnt++;
          }
          writer.println(") {");
          writer.indent();
          genMethodAdapter(model, m, writer);
          writer.unindent();
          writer.print("}");
        }
        writer.unindent();
        writer.println(" else throw new TypeError('function invoked with invalid arguments');");
        writer.println("};");
        writer.println();
      }
    }
  }

  @Override
  public String render(ProxyModel model, int index, int size, Map<String, Object> session) {
    StringWriter sw = new StringWriter();
    CodeWriter writer = new CodeWriter(sw);
    ClassTypeInfo type = model.getType();
    String simpleName = type.getSimpleName();
    genLicenses(writer);
    writer.println();
    writer.format("/// <reference path=\"./%s-proxy.d.ts\" />", Helper.convertCamelCaseToUnderscores(type.getRaw().getSimpleName())).println();
    writer.println();
    writer.format("/** @module %s */", getModuleName(type)).println();

    List<String> imports = new ArrayList<>();

    //generate the module loader shim
    writer.println("!function (factory) {");
    writer.indent().println("if (typeof require === 'function' && typeof module !== 'undefined') {");
    // write imports as commonJS
    writer.indent();
    imports.clear();
    for (ApiTypeInfo referencedType : model.getReferencedTypes()) {
      if(referencedType.isProxyGen()) {
        String refedType = referencedType.getSimpleName();
        imports.add(refedType);
        writer.format("var %s = require('./%s-proxy');", refedType, getModuleName(referencedType)).println();
      }
    }
    writer.format("factory(%s);", String.join(", ", imports)).println();
    writer.unindent().println("} else if (typeof define === 'function' && define.amd) {");
    writer.indent().println("// AMD loader");
    imports.clear();
    for (ApiTypeInfo referencedType : model.getReferencedTypes()) {
      if(referencedType.isProxyGen()) {
        imports.add("'" + getModuleName(referencedType) + "-proxy'");
      }
    }
    writer.format("define('%s-proxy', [%s], factory);", getModuleName(type), String.join(", ", imports));
    writer.println();
    writer.unindent().println("} else {");
    writer.indent().println("// plain old include");
    imports.clear();
    for (ApiTypeInfo referencedType : model.getReferencedTypes()) {
      if(referencedType.isProxyGen()) {
        String refedType = referencedType.getSimpleName();
        imports.add("this." + refedType);
      }
    }
    writer.format("%s = factory(%s);", simpleName, String.join(", ", imports)).println();
    writer.unindent().println("}");
    imports.clear();
    for (ApiTypeInfo referencedType : model.getReferencedTypes()) {
      if(referencedType.isProxyGen()) {
        String refedType = referencedType.getSimpleName();
        imports.add(refedType);
      }
    }
    writer.unindent().format("}(function (%s) {", String.join(", ", imports)).println();
    writer.indent();
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

  private void genMethodAdapter(ProxyModel model, MethodInfo m, CodeWriter writer) {
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
    boolean hasResultHandler = method.getKind() == MethodKind.FUTURE;
    writer.print("j_eb.send(j_address, {");
    boolean first = true;
    for (int pcnt = 0; pcnt < params.size(); pcnt++) {
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
      ParameterizedTypeInfo futureType = (ParameterizedTypeInfo) method.getReturnType();
      TypeInfo resultType = futureType.getArg(0);
      writer.format(", function(err, result) { __args[%d](err, result && ", params.size());
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

  void genDoc(ProxyModel model, CodeWriter writer) {
    writer.println("/**");
    if (model.getIfaceComment() != null) {
      writer.println(Helper.removeTags(model.getIfaceComment()));
    }
    writer.println(" @class");
    writer.println("*/");
  }
}
