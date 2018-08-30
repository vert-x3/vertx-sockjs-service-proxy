package io.vertx.serviceproxy.sockjs.generator;

import io.vertx.codegen.Case;
import io.vertx.codegen.Helper;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.ParamInfo;
import io.vertx.codegen.type.*;
import io.vertx.codegen.writer.CodeWriter;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.vertx.codegen.type.ClassKind.*;

public class SockjsServiceProxyTSGenerator extends AbstractSockjsServiceProxyGenerator {

  @Override
  public String filename(ProxyModel model) {
    ClassTypeInfo type = model.getType();
    return "resources/" + type.getModuleName() + "-ts/" + Helper.convertCamelCaseToUnderscores(type.getRaw().getSimpleName()) + "-proxy.ts";
  }

  @Override
  public String render(ProxyModel model, int index, int size, Map<String, Object> session) {
    StringWriter sw = new StringWriter();
    CodeWriter writer = new CodeWriter(sw);

    String simpleName = model.getIfaceSimpleName();
    genLicenses(writer);
    writer.println();

    //Generate the requires
    for (ApiTypeInfo referencedType : model.getReferencedTypes()) {
      if (referencedType.isProxyGen()) {
        String refedType = referencedType.getSimpleName();
        writer.format("import { %s } from '%s-proxy';", refedType, getModuleName(referencedType)).println();
      }
    }
    writer.println();

    writer.format("export class %s {", simpleName).println();
    writer.println();

    writer.indent().println("private closed = false;");
    writer.println();
    writer.println("private readonly convCharCollection = coll => {");
    writer.indent().println("const ret = [];");
    writer.println("for (let i = 0; i < coll.length; i++) {");
    writer.indent().println("ret.push(String.fromCharCode(coll[i]));");
    writer.unindent().println("}");
    writer.println("return ret;");
    writer.unindent().println("}");
    writer.println();

    //The constructor
    writer.println("constructor (private eb: any, private address: string) {");
    writer.indent();
    //Apply any supertypes
    for (TypeInfo superType : model.getSuperTypes()) {
      writer.format("%s.call(this, eb, address);", superType.getRaw().getSimpleName()).println();
    }
    writer.unindent();
    writer.println("}");

    //Now iterate through each unique method

    for (String methodName : model.getMethodMap().keySet()) {
      //Call out to actually generate the method, we only consider non static methods here
      genMethod(model, methodName, false, this::methodFilter, writer);
    }
    writer.unindent();
    writer.print("}");
    return sw.toString();
  }

  @Override
  protected String convReturn(ProxyModel model, MethodInfo method, TypeInfo returnType, String templ) {
    //do nothing
    return null;
  }

  /**
   * Generate a TypeScript Method
   */
  protected void genMethod(ProxyModel model, String methodName, boolean genStatic, Predicate<MethodInfo> methodFilter, CodeWriter writer) {
    String simpleName = model.getIfaceSimpleName();
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
      MethodInfo method = methodList.get(methodList.size() - 1);
      if (genStatic == method.isStaticMethod()) {
        writer.println();
        if (genStatic) {
          writer.print("static ");
        }
        writer.format("%s(%s) : ", methodName, method.getParams().stream().map(p -> p.getName() + ": " + getTSDocType(p.getType())).collect(Collectors.joining(", ")));
        if (method.isFluent()) {
          writer.print(simpleName);
        } else {
          writer.print("void");
        }
        writer.println(" {");
        genMethodAdapter(model, method, writer);
        writer.unindent().println("}");
      }
    }
  }

  private void genMethodCall(MethodInfo method, CodeWriter writer) {
    List<ParamInfo> params = method.getParams();
    int psize = params.size();
    ParamInfo lastParam = psize > 0 ? params.get(psize - 1) : null;
    boolean hasResultHandler = lastParam != null && lastParam.getType().getKind() == HANDLER && ((ParameterizedTypeInfo) lastParam.getType()).getArg(0).getKind() == ASYNC_RESULT;
    if (hasResultHandler) {
      psize--;
    }
    writer.print("this.eb.send(this.address, {");
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
      writer.format("\"%s\": %s", paramName, paramName);
      if ("java.lang.Character".equals(paramTypeName) || "char".equals(paramTypeName)) {
        writer.print(".charCodeAt(0)");
      }
    }
    writer.format("}, {\"action\":\"%s\"}", method.getName());
    if (hasResultHandler) {
      ParameterizedTypeInfo handlerType = (ParameterizedTypeInfo) lastParam.getType();
      ParameterizedTypeInfo asyncResultType = (ParameterizedTypeInfo) handlerType.getArg(0);
      TypeInfo resultType = asyncResultType.getArg(0);
      writer.format(", function(err, result) { %s(err, result && ", lastParam.getName());
      ClassKind resultKind = resultType.getKind();
      if (resultType.getKind() == API) {
        writer.format("new %s(this.eb, result.headers.proxyaddr)", resultType.getSimpleName());
      } else if ((resultKind == LIST || resultKind == SET) && "java.lang.Character".equals(((ParameterizedTypeInfo) resultType).getArg(0).getName())) {
        writer.print("this.convCharCollection(result.body)");
      } else {
        writer.print("result.body");
      }
      writer.print("); }");
    }
    writer.print(")");
  }

  @Override
  protected void genMethodAdapter(ProxyModel model, MethodInfo m, CodeWriter writer) {
//  protected void genMethodAdapter(ProxyModel model, MethodInfo m, String ind, PrintWriter writer) {
    ProxyMethodInfo method = (ProxyMethodInfo) m;
    writer.indent().println("if (closed) {");
    writer.indent().println("throw new Error('Proxy is closed');");
    writer.unindent().println("}");
    genMethodCall(method, writer);
    writer.println(";");
    if (method.isProxyClose()) {
      writer.println("closed = true;");
    }
    if (method.isFluent()) {
      writer.println("return this;");
    }
  }

  /**
   * Generate the JSDoc type of a type
   */
  private String getTSDocType(TypeInfo type) {
    switch (type.getKind()) {
      case STRING:
        return "string" + (type.isNullable() ? " | null" : "");
      case PRIMITIVE:
      case BOXED_PRIMITIVE:
        switch (type.getSimpleName()) {
          case "boolean":
          case "Boolean":
            return "boolean" + (type.isNullable() ? " | null" : "");
          case "char":
          case "Character":
            return "string" + (type.isNullable() ? " | null" : "");
          default:
            return "number" + (type.isNullable() ? " | null" : "");
        }
      case JSON_OBJECT:
        return "Object" + (type.isNullable() ? " | null" : "");
      case JSON_ARRAY:
        return "Array" + (type.isNullable() ? " | null" : "");
      case DATA_OBJECT:
        return "any" + (type.isNullable() ? " | null" : "");
      case ENUM:
        return "string" + (type.isNullable() ? " | null" : "");
      case API:
        return type.getRaw().getSimpleName() + (type.isNullable() ? " | null" : "");
      case MAP:
        //`Map` before `collection`, because of MAP.collection is true
        return "Object<string, " + getTSDocType(((ParameterizedTypeInfo) type).getArg(1)) + ">" + (type.isNullable() ? " | null" : "");
      case SET:
      case LIST:
        return "Array<" + getTSDocType(((ParameterizedTypeInfo) type).getArg(0)) + ">" + (type.isNullable() ? " | null" : "");
      case OBJECT:
        return "any";
      case HANDLER:
        ParameterizedTypeInfo handlerType = (ParameterizedTypeInfo) type;
        ParameterizedTypeInfo asyncResultType = (ParameterizedTypeInfo) handlerType.getArg(0);
        TypeInfo resultType = asyncResultType.getArg(0);
        switch (resultType.getKind()) {
          case API:
          case STRING:
          case PRIMITIVE:
          case JSON_OBJECT:
          case JSON_ARRAY:
          case DATA_OBJECT:
          case ENUM:
          case MAP:
          case SET:
          case LIST:
          case OBJECT:
            return "(err: any, result: " + getTSDocType(resultType) + ") => any";
          default:
            return "(err: any, result: any) => any";
        }
      default:
        return "todo";
    }
  }

  /**
   * Generate the module name of a type
   */
  protected String getModuleName(ClassTypeInfo type) {
    return type.getModuleName() + "-ts/" + Case.CAMEL.to(Case.SNAKE, type.getSimpleName());
  }
}
