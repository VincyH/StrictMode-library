package com.nshmura.strictmodenotifier;

import android.content.Context;

public class StrictModeNotifier {
  static boolean FLAGS = false;

  public static NotifierConfig install(Context context) {
    return install(context, LogWatchService.class);
  }

  public static NotifierConfig install(Context context,
      Class<? extends LogWatchService> serviceClass) {
    StrictModeNotifierInternals.enableReportActivity(context);
    StrictModeNotifierInternals.startLogWatchService(context, serviceClass);
    return NotifierConfig.getInstance();
  }
}