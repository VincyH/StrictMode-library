package com.nshmura.strictmodenotifier;

import com.nshmura.strictmodenotifier.detector.ActivityLeaks;

import com.nshmura.strictmodenotifier.detector.CleartextNetworkDetector;
import com.nshmura.strictmodenotifier.detector.Detector;
import com.nshmura.strictmodenotifier.detector.DiskReadOrWrite;
import com.nshmura.strictmodenotifier.detector.FileUriExposureDetector;
import com.nshmura.strictmodenotifier.detector.LeakedClosableObjectsDetector;
import com.nshmura.strictmodenotifier.detector.LeakedSQLiteObject;
import com.nshmura.strictmodenotifier.detector.NetworkDetector;
import com.nshmura.strictmodenotifier.detector.ResourceMismatchDetector;


public enum ViolationTypeInfo {

  // ThreadPolicy

  NETWORK(
      "Network",
      ViolationType.NETWORK,
      new NetworkDetector()),

  RESOURCE_MISMATCHES(
      "Resource Mismatches",
      ViolationType.RESOURCE_MISMATCHES,
      new ResourceMismatchDetector()),

  // VmPolicy

  CLEARTEXT_NETWORK(
      "Cleartext Network",
      ViolationType.CLEARTEXT_NETWORK,
      new CleartextNetworkDetector()),

  FILE_URI_EXPOSURE(
      "File Uri Exposure",
      ViolationType.FILE_URI_EXPOSURE,
      new FileUriExposureDetector()),

  LEAKED_CLOSABLE_OBJECTS(
      "Leaked Closable Objects",
      ViolationType.LEAKED_CLOSABLE_OBJECTS,
      new LeakedClosableObjectsDetector()),

  ACTIVITY_LEAKS(
      "Activity Leaks",
      ViolationType.ACTIVITY_LEAKS,
      new ActivityLeaks()),

    DISK_READS_WRITES(
            "Disk  READS or WRITES",
            ViolationType.DISK_READS_WRITES,
            new DiskReadOrWrite()),

  LEAKED_SQL_LITE_OBJECTS(
      "Leaked Sql Lite Objects",
      ViolationType.LEAKED_SQL_LITE_OBJECTS,
          new LeakedSQLiteObject()),

    ANDROID_EXCEPTION(
            "Android Exception",
            ViolationType.ANDROID_EXCEPTION,
            new LeakedSQLiteObject()),

  UNKNOWN("UNKNOWN", ViolationType.UNKNOWN,
          null);

  private String name;
  public final ViolationType violationType;
  public final Detector detector;

  ViolationTypeInfo(String name, ViolationType violationType,
      Detector detector) {
    this.name = name;
    this.violationType = violationType;

    if (detector != null) {
      this.detector = detector;
    } else {
      this.detector = new Detector() {
        @Override public boolean detect(StrictModeLog log) {
          return false;
        }
      };
    }
  }

    public static ViolationTypeInfo convert(ViolationType type) {
    for (ViolationTypeInfo info : values()) {
      if (info.violationType == type) {
        return info;
      }
    }
    return ViolationTypeInfo.UNKNOWN;
  }

  public String violationName() {
    return name + " Violation";
  }
}
