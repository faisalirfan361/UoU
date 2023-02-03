package com.UoU.core.accounts;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Provider {
  INTERNAL("internal", "Internal"),
  MICROSOFT("microsoft", "Microsoft"),
  GOOGLE("google", "Google");

  private final String value;
  private final String displayName;
}
