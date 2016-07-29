package com.strictmodenotifier.library.detector;

import com.strictmodenotifier.library.StrictModeLog;

public class LeakedClosableObjectsDetector implements Detector {

  @Override public boolean detect(StrictModeLog log) {
    return log.message.contains("A resource was acquired at attached stack trace but never released.");
  }
}