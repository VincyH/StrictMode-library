package com.strictmodenotifier.library.detector;

import com.nshmura.strictmodenotifier.StrictModeLog;

public class ClassInstanceLimitDetector implements Detector {

  @Override public boolean detect(StrictModeLog log) {
    return log.message.contains("StrictMode.setClassInstanceLimit");
  }
}
