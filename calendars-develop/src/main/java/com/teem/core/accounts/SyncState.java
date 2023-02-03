package com.UoU.core.accounts;

/**
 * Account sync state, which for now comes directly from Nylas without translation.
 *
 * <p>See https://developer.nylas.com/docs/the-basics/manage-accounts/account-sync-status/
 */
public enum SyncState {
  /**
   * Used instead of null when we don't have a status from nylas yet, or if we get an unknown one.
   */
  UNKNOWN,

  INITIALIZING,

  DOWNLOADING,

  RUNNING,

  /**
   * The "partial" status IS NOT documented by Nylas as a possible option for the accounts API,
   * although it is documented as a dashboard status. We see it often for newly connected Google
   * accounts, and it means basically "running" but some stuff is still syncing.
   */
  PARTIAL,

  INVALID_CREDENTIALS,

  EXCEPTION,

  SYNC_ERROR,

  STOPPED,
}
