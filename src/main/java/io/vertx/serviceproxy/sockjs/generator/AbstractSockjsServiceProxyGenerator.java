package io.vertx.serviceproxy.sockjs.generator;

import io.vertx.codegen.Generator;
import io.vertx.codegen.MethodInfo;
import io.vertx.codegen.doc.Tag;
import io.vertx.codegen.type.ClassTypeInfo;
import io.vertx.codegen.type.EnumTypeInfo;
import io.vertx.codegen.type.ParameterizedTypeInfo;
import io.vertx.codegen.type.TypeInfo;
import io.vertx.serviceproxy.generator.model.ProxyMethodInfo;
import io.vertx.serviceproxy.generator.model.ProxyModel;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.io.PrintWriter;
import java.util.Collections;

import static io.vertx.codegen.type.ClassKind.DATA_OBJECT;
import static io.vertx.codegen.type.ClassKind.ENUM;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.ElementKind.METHOD;

abstract class AbstractSockjsServiceProxyGenerator extends Generator<ProxyModel> {
  AbstractSockjsServiceProxyGenerator() {
    this.name = "sockjs_service_proxies";
    this.kinds = Collections.singleton("proxy");
  }

  boolean methodFilter(MethodInfo m){
    ProxyMethodInfo method = (ProxyMethodInfo) m;
    return !method.isProxyIgnore();
  }

  protected void genLicenses(PrintWriter writer) {
    writer.println("/*");
    writer.println(" * Copyright 2014 Red Hat, Inc.");
    writer.println(" *");
    writer.println(" * Red Hat licenses this file to you under the Apache License, version 2.0");
    writer.println(" * (the \"License\"); you may not use this file except in compliance with the");
    writer.println(" * License.  You may obtain a copy of the License at:");
    writer.println(" *");
    writer.println(" * http://www.apache.org/licenses/LICENSE-2.0");
    writer.println(" *");
    writer.println(" * Unless required by applicable law or agreed to in writing, software");
    writer.println(" * distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT");
    writer.println(" * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the");
    writer.println(" * License for the specific language governing permissions and limitations");
    writer.println(" * under the License.");
    writer.println(" */");
  }

  /**
   * Render a tag link to an html link, this function is used as parameter of the
   * renderDocToHtml function when it needs to render tag links.
   */
  protected String renderLinkToHtml(Tag.Link link) {
    ClassTypeInfo rawType = link.getTargetType().getRaw();
    if (rawType.getModule() != null) {
      String label = link.getLabel().trim();
      if (rawType.getKind() == DATA_OBJECT) {
        if (label.length() == 0) {
          label = rawType.getSimpleName();
        }
        return "<a href=\"../../dataobjects.html#" + rawType.getSimpleName() + "\">" + label + "</a>";
      } else if (rawType.getKind() == ENUM && ((EnumTypeInfo) rawType).isGen()) {
        if (label.length() == 0) {
          label = rawType.getSimpleName();
        }
        return "<a href=\"../../enums.html#" + rawType.getSimpleName() + "\">" + label + "</a>";
      } else {
        if (label.length() > 0) {
          label = "[" + label + "] ";
        }
        Element elt = link.getTargetElement();
        String jsType = rawType.getSimpleName();
        ElementKind kind = elt.getKind();
        if (kind == CLASS || kind == INTERFACE) {
          return label + "{@link " + jsType + "}";
        } else if (kind == METHOD) {
          return label + "{@link " + jsType + "#" + elt.getSimpleName().toString() + "}";
        } else {
          System.out.println("Unhandled kind " + kind);
        }
      }
    }
    return null;
  }

  /**
   * Generate the JSDoc type of a type
   */
  protected String getJSDocType(TypeInfo type) {
    switch (type.getKind()) {
      case STRING:
        return "string";
      case PRIMITIVE:
      case BOXED_PRIMITIVE:
        switch (type.getSimpleName()) {
          case "boolean":
          case "Boolean":
            return "boolean";
          case "char":
          case "Character":
            return "string";
          default:
            return "number";
        }
      case JSON_OBJECT:
      case DATA_OBJECT:
      case ENUM:
      case OBJECT:
        return "Object";
      case JSON_ARRAY:
        return "Array";
      case API:
        return type.getRaw().getSimpleName();
      case MAP:
        //`Map` before `collection`, because of MAP.collection is true
        return "Object.<string, " + getJSDocType(((ParameterizedTypeInfo) type).getArg(1)) + ">";
      case HANDLER:
      case FUNCTION:
        return "function";
      case SET:
      case LIST:
        return "Array.<" + getJSDocType(((ParameterizedTypeInfo) type).getArg(0)) + ">";
      default:
        return "todo";
    }
  }
}
