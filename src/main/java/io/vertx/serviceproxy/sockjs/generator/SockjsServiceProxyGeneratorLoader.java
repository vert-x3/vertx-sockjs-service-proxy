package io.vertx.serviceproxy.sockjs.generator;

import io.vertx.codegen.processor.Generator;
import io.vertx.codegen.processor.GeneratorLoader;

import javax.annotation.processing.ProcessingEnvironment;
import java.util.stream.Stream;

public class SockjsServiceProxyGeneratorLoader implements GeneratorLoader {
  @Override
  public Stream<Generator<?>> loadGenerators(ProcessingEnvironment processingEnv) {
    return Stream.of(new SockjsServiceProxyJSGenerator(), new SockjsServiceProxyTSGenerator());
  }
}
