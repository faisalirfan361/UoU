package com.UoU.app.security;

/**
 * Custom claim names to be used in JWTs.
 */
public class CustomClaims {

  /**
   * Organization id used to scope data access to a single organization.
   */
  public static final String ORG_ID = "org_id";

  /**
   * Space-separated scopes to authorize access to specific things.
   */
  public static final String SCOPE = "scope";
}
