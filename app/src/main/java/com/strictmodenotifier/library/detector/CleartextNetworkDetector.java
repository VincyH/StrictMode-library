package com.strictmodenotifier.library.detector;

import com.strictmodenotifier.library.StrictModeLog;

public class CleartextNetworkDetector implements Detector {

  @Override
  public boolean detect(StrictModeLog log) {
    return log.message.contains("CLEARTEXT communication not supported:");
  }
}
