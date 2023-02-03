package com.UoU.core.diagnostics;

/**
 * Status for a diagnostics run.
 */
public enum Status {

  PENDING {
    @Override
    public boolean isTerminal() {
      return false;
    }
  },

  PROCESSING {
    @Override
    public boolean isTerminal() {
      return false;
    }
  },

  SUCCEEDED {
    @Override
    public boolean isTerminal() {
      return true;
    }
  },

  FAILED {
    @Override
    public boolean isTerminal() {
      return true;
    }
  };

  public abstract boolean isTerminal();
}
