package com.UoU.core.nylas.auth;

import com.nylas.ProviderSettings;

/**
 * Provider settings for Nylas virtual accounts.
 */
class VirtualAccountProviderSettings extends ProviderSettings {

  /**
   * Static settings instance, since these settings are always the same for all accounts.
   */
  public static final VirtualAccountProviderSettings INSTANCE =
      new VirtualAccountProviderSettings();

  private VirtualAccountProviderSettings() {
    super("nylas");
  }
}
