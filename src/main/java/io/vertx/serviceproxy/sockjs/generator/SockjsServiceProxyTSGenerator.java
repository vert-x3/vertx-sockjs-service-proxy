package io.vertx.serviceproxy.sockjs.generator;

import io.vertx.codegen.Case;
import io.vertx.codegen.Helper;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.type.*;
import io.vertx.codegen.writer.CodeWriter;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SockjsServiceProxyTSGenerator extends SockjsServiceProxyJSGenerator {

  @Override
  public String filename(ProxyModel model) {
    ClassTypeInfo type = model.getType();
    return "resources/" + type.getModuleName() + "-js/" + Helper.convertCamelCaseToUnderscores(type.getRaw().getSimpleName()) + "-proxy.d.ts";
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
        writer.format("import { %s } from './%s-proxy';", refedType, getModuleName(referencedType)).println();
      }
    }
    writer.println();

    genDoc(model, writer);
    writer.format("export default class %s {", simpleName).println();
    writer.println();

    //The constructor
    writer.indent();
    writer.println("constructor (eb: any, address: string);");
    //Now iterate through each unique method
    for (String methodName : model.getMethodMap().keySet()) {
      //Call out to actually generate the method, we only consider non static methods here
      genMethod(model, methodName, false, this::methodFilter, writer);
    }

    writer.unindent();

    writer.print("}");
    return sw.toString();
  }

  /**
   * Generate a TypeScript Method
   */
  private void genMethod(ProxyModel model, String methodName, boolean genStatic, Predicate<MethodInfo> methodFilter, CodeWriter writer) {
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
        writer.println(";");
      }
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
  private String getModuleName(ClassTypeInfo type) {
    return type.getModuleName() + "-js/" + Case.CAMEL.to(Case.SNAKE, type.getSimpleName());
  }
}
