package com.strictmodenotifier.library.detector;

import com.strictmodenotifier.library.StrictModeLog;

public class ResourceMismatchDetector implements Detector {

  @Override public boolean detect(StrictModeLog log) {
    return log.message.contains("StrictMode$StrictModeResourceMismatchViolation");
  }
}
