package com.ita.home.exception;

/**
 * @Author:  Mikkeyf
 * @CreateTime: 2025/9/22 14:43
 */
public class BaseException extends RuntimeException {
  public BaseException() {

  }
  public BaseException(String message) {
      super(message);
  }
}
