package com.nshmura.strictmodenotifier;

/**
 * 自定义删除违反规则
 */
public interface IgnoreAction {
  boolean ignore(StrictModeViolation violation);
}