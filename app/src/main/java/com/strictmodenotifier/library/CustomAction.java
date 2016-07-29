package com.strictmodenotifier.library;

public interface CustomAction {
  void onViolation(StrictModeViolation violation);
}