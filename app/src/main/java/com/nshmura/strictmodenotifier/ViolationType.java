package com.nshmura.strictmodenotifier;



  public enum ViolationType {

    // ThreadPolicy
    // CUSTOM_SLOW_CALL,      发现 UI线程 调用的那些方法执行得比较慢，要和 StrictMode.noteSlowCall("") 配合使用 // 待改进
    NETWORK,                // 检查UI线程中是否有网络请求操作
    RESOURCE_MISMATCHES,    //
    DISK_READS_WRITES,      // 新增检查UI线程中是否有磁盘读写   UserDebug 固件OK//待优化


    // VmPolicy
    //  CLASS_INSTANCE_LIMIT,    setClassInstanceLimit()，设置某个类的同时处于内存中的实例上限，可以协助检查内存泄露
    CLEARTEXT_NETWORK,             //
    FILE_URI_EXPOSURE,             //
    LEAKED_CLOSABLE_OBJECTS,      // 检查（SQLite）对象是否被正确关闭 **
    ACTIVITY_LEAKS,                // Activity 的内存泄露情况 //待优化
    // LEAKED_REGISTRATION_OBJECTS,
    LEAKED_SQL_LITE_OBJECTS,       //检查在使用Cursor对数据库进行操作后没有关闭Cursor 对象是否被正确关闭 **

    ANDROID_EXCEPTION,
    HHH,

    //UNKNOWN
    UNKNOWN                     //  //待优化
  }

