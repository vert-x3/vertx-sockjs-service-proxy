/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.serviceproxy.testmodel.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.testmodel.TestConnectionWithCloseFuture;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TestConnectionWithCloseFutureImpl implements TestConnectionWithCloseFuture {

  private Vertx vertx;

  public TestConnectionWithCloseFutureImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public Future<Void> close() {
    vertx.eventBus().send("closeCalled", "blah");
    return Future.succeededFuture();
  }

  @Override
  public Future<String> someMethod() {
    return Future.succeededFuture("the_result");
  }
}
