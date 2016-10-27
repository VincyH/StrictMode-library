package com.nshmura.strictmodenotifier.detector;

import com.nshmura.strictmodenotifier.StrictModeLog;

/**
 * Created by huangjingqing on 2016/8/9.
 */
public class DiskReadOrWrite implements Detector {

    @Override public boolean detect(StrictModeLog log){

        return (log.message.contains("took")||log.message.contains("MainThread")||log.message.contains("prepare"));
        //executeForLong
    }
}
