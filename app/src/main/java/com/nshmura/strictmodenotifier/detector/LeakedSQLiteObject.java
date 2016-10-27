package com.nshmura.strictmodenotifier.detector;

import com.nshmura.strictmodenotifier.StrictModeLog;

/**
 * Created by huangjingqing on 2016/8/9.
 */
public class LeakedSQLiteObject implements Detector {
    @Override public boolean detect(StrictModeLog log) {
        return log.message.contains("a Cursor that has not been deactivated or closed");
    }
}
