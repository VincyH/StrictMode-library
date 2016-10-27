package com.nshmura.strictmodenotifier;

/**
 * 自定义添加违反规则
 */
public interface CustomAction {
  void onViolation(StrictModeViolation violation);
}