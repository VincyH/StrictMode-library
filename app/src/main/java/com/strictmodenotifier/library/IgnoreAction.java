package com.strictmodenotifier.library;

public interface IgnoreAction {
  boolean ignore(StrictModeViolation violation);
}