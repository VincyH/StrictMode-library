package com.nshmura.strictmodenotifier;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.nshmura.strictmodenotifier.R;

public class LogWatchService extends IntentService {

  private static final String THREAD_NAME = LogWatchService.class.getSimpleName(); //类的简单名称  LogWatchService
  private static final String TAG = THREAD_NAME;

  private static final String LOGCAT_COMMAND =  "logcat -v time";
//  "logcat -v time -s StrictMode:* System.err:* SQLiteConnection";
  private static final java.lang.String PARSE_REGEXP = "(StrictMode|System.err)(\\([0-9]+\\))?:";
  private static final CharSequence EXCEPTION_KEY = "System.err";

  private static final long NOTIFICATION_DELAY = 2000;     //ms   通知栏响应延时
  private static final long LOG_DELAY  = 1000;            //ms   Log延时
  private static final long EXIT_SPAN = 1000 * 60;       //ms   退出 readLoop

  private NotifierConfig notifierConfig = NotifierConfig.getInstance();  //单例模式 NotifierConfig
  private Process proc;  // 进程
  private final ViolationStore violationStore;  // 违背规则库
  private List<StrictModeLog> logs = new ArrayList<>();  // StrictModeLog logs
  private Timer timer = null;
  private ViolationType[] violationTypes = ViolationType.values();  // 转化数组
  boolean flags=false;    // 从detector出发的标记
  static int hhh=0;       // 搭配flags

  public LogWatchService() {
     this(TAG);           //  TAG = LogWatchService
  }

  public LogWatchService(String name) {
    super(name);
    violationStore = new ViolationStore(this);  // this Context
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    return START_STICKY;
  }

  @Override protected void onHandleIntent(Intent intent) {   // Intent是从Activity发过来的，携带识别参数，根据参数不同执行不同的任务
    startReadLoop();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    log("onDestroy");

    if (proc != null) {
      proc.destroy();
      proc = null;
    }
  }

  /**
   * notify the StrictModeViolation.
   *
   * @param violation StrictModeViolation
   */
  protected void notifyViolation(StrictModeViolation violation) {

    //Custom Actions
    List<CustomAction> customActions = notifierConfig.getCustomActions();
    for (CustomAction customAction : customActions) {
      customAction.onViolation(violation);
    }

    //Default Action
    String notificationTitle;
    if (violation.violationType != null) {
      notificationTitle = ViolationTypeInfo.convert(violation.violationType).violationName();
      StrictModeNotifierInternals.showNotification(this, notificationTitle,
              getString(R.string.strictmode_notifier_more_detail), notifierConfig.isHeadupEnabled(),
              StrictModeReportActivity.createPendingIntent(this, violation));
    } else {
     notificationTitle = getString(R.string.strictmode_notifier_title, getPackageName());
    }
//    StrictModeNotifierInternals.showNotification(this, notificationTitle,
//        getString(R.string.strictmode_notifier_more_detail), notifierConfig.isHeadupEnabled(),
//        StrictModeReportActivity.createPendingIntent(this, violation));
  }

  private void startReadLoop() {
    while (true) {
      long startTime = System.currentTimeMillis();

      log("start readLoop");

      readLoop();

      log("end readLoop");

      if (System.currentTimeMillis() - startTime <= EXIT_SPAN) {   // 小于1分钟，跳出读循环
        log("exit readLoop");
        break;
      }
    }
  }

  private void readLoop() {

    BufferedReader reader = null;
    try {

      Runtime.getRuntime().exec("logcat -c");       //clear log

      Runtime.getRuntime().exec("setprop persist.sys.perf.io 1");
      Runtime.getRuntime().exec("setprop persist.sys.perf.db 1");    //  设置 io 和 db 开关

      proc = Runtime.getRuntime().exec(LOGCAT_COMMAND);   //  logcat -v time  抓取ｌｏｇ

      reader = new BufferedReader(new InputStreamReader(proc.getInputStream()), 1024);

      while (true) {
        String line = reader.readLine();

       if (line != null) {
          StrictModeLog log = parseLine(line);   // 分割每一行log，分割后成为 StrictModeLog log

          if(!flags) {
            for (ViolationType type : violationTypes) {
              ViolationTypeInfo info = ViolationTypeInfo.convert(type);
              if (log!=null&&info.detector.detect(log)) {
                flags = true;
                break;
              }
            }
          }

          if (log != null&& flags) {
            storeLog(log);
            startReportTimer();
          }
        }

        else {
          error("error readLoop");
          break;
        }
      }
    } catch (IOException e) {
      error(e.getMessage());
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
  }

  private StrictModeLog parseLine(String line) {
    String[] split = line.split(PARSE_REGEXP);
    if (split.length < 2) {
      split= line.split("SQLiteConnection");
        if (split.length > 1) {
          return new StrictModeLog("DiskReadOrWrite", line.split(" ",2)[1].split(" ", 2)[1], System.currentTimeMillis());
        }
      split= line.split("Perf_IO");
      if (split.length > 1) {
        return new StrictModeLog("DiskReadOrWrite", line.split(" ",2)[1].split(" ",2)[1], System.currentTimeMillis());
      }
      return null;
    }
    if (split[1].equals("null")){
      return null;
    }
    return new StrictModeLog(split[0], split[1], System.currentTimeMillis());
  }

  private void storeLog(StrictModeLog log) {
    synchronized (this) {
      logs.add(log);
    }
  }

  private void startReportTimer() {
    synchronized (this) {
      if (timer != null) {
        return;
      }
      timer = new Timer(true);
    }

    timer.schedule(new TimerTask() {
      @Override public void run() {
        synchronized (LogWatchService.this) {
          int count = logs.size();
          boolean prevIsAt = false;
          long lastReadTime = 0;
          List<StrictModeLog> targets = new ArrayList<>();
          for (int i = 0; i < count; i++) {
            StrictModeLog log = logs.get(i);
            boolean isAt = log.isAt();
            if (!isAt && prevIsAt && targets.size() > 0) {
              StrictModeViolation report = null;
              report = createViolation(targets);

              if (report != null) {
                notifyViolation(report);
                flags=false;
//                System.out.println("oooo" + (hhh++));
              }
              targets.clear();
            }
            if(flags) {
              prevIsAt = isAt;
              targets.add(log);
              lastReadTime = log.time;
            }
          }

          if (targets.size() > 0 && System.currentTimeMillis() - lastReadTime >= LOG_DELAY) {
            StrictModeViolation report = null;
              report = createViolation(targets);
            if (report != null) {
              notifyViolation(report);
              flags=false;
//             System.out.println("oooo" + (hhh++));
            }
            targets.clear();
          }

          timer = null;
          logs.clear();

//          if (targets.size() > 0) {
//            logs = targets;
//            startReportTimer();
//          } else {
//            logs.clear();
//          }
        }
      }
    }, NOTIFICATION_DELAY);
  }

  private StrictModeViolation createViolation(List<StrictModeLog> logs) {
    ArrayList<String> stacktreace = new ArrayList<>(logs.size());
    String title = "";
    String logKey = "";
    long time = 0;
    for (StrictModeLog log : logs) {
      if (TextUtils.isEmpty(title)) {
        title = log.message;
        logKey = log.tag;
        time = log.time;
      }
      stacktreace.add(log.message);
    }

    ViolationType violationType = getViolationType(logs);

//    if (violationType == ViolationType.UNKNOWN && logKey.contains(EXCEPTION_KEY)) {
    if (violationType == ViolationType.UNKNOWN ) {
      return null;
    }
    if (StrictModeNotifier.FLAGS||violationType == ViolationType.UNKNOWN ) {
      return null;
    }

    StrictModeViolation violation = new StrictModeViolation(violationType, title, logKey, stacktreace, time);

    //Ignore Action
    if (notifierConfig.getIgnoreAction() != null) {
      if (notifierConfig.getIgnoreAction().ignore(violation)) {
        return null;
      }
    }
    try {
      violationStore.append(violation);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return violation;
  }

  private ViolationType getViolationType(List<StrictModeLog> logs)  {
    for (StrictModeLog log : logs) {
      for (ViolationType type : violationTypes) {
        ViolationTypeInfo info = ViolationTypeInfo.convert(type);
        if (info != null && info.detector.detect(log)) {
//          if (type==ViolationType.DISK_READS_WRITES&&!StrictModeNotifier.FLAGS){
//            StrictModeNotifier.FLAGS=true;
//          }else{
//            return ViolationType.UNKNOWN;
//          }
          return type;
        }
      }
    }
    return ViolationType.UNKNOWN;
  }

  private void log(String message) {
    if (notifierConfig.isDebugMode()) {
      Log.d(TAG, message);
    }
  }

  private void error(String message) {
    if (notifierConfig.isDebugMode()) {
      Log.e(TAG, message);
    }
  }
}