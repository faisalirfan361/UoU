package com.UoU.core;

public interface Task<T> {
  void run(T params);

  interface WithNoParams {
    void run();
  }
}
