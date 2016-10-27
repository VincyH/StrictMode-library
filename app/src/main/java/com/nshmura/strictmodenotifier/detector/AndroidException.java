package com.nshmura.strictmodenotifier.detector;

import com.nshmura.strictmodenotifier.StrictModeLog;

/**
 * Created by huangjingqing on 2016/10/10.
 */
public class AndroidException implements Detector  {
    @Override
    public boolean detect(StrictModeLog log) {
        return (log.message.contains("android.os.DeadObjectException")||log.message.contains("NullPointerException")||log.message.contains("net.ssl"));
    }
}
