/*
 * Copyright 2021 Apollo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.ctrip.framework.apollo.common.exception;

import org.springframework.http.HttpStatus;

public abstract class AbstractApolloHttpException extends RuntimeException{

  private static final long serialVersionUID = -1713129594004951820L;
  
  protected HttpStatus httpStatus;

  public AbstractApolloHttpException(String msg){
    super(msg);
  }
  
  public AbstractApolloHttpException(String msg, Exception e){
    super(msg,e);
  }

  protected void setHttpStatus(HttpStatus httpStatus){
    this.httpStatus = httpStatus;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }
}
