package com.nshmura.strictmodenotifier.detector;

import com.nshmura.strictmodenotifier.StrictModeLog;

/**
 * Created by huangjingqing on 2016/8/9.
 */
public class ActivityLeaks implements  Detector {
    @Override public boolean detect(StrictModeLog log) {
        return log.message.contains("StrictMode$InstanceCountViolation"); //?
    }
}
