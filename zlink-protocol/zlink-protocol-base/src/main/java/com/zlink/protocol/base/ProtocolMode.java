package com.zlink.protocol.base;

/**
 * 协议通信的三种模式, 包括
 * <ul>
 * <li>
 * 同步模式: 请求并等待响应
 * <li>
 * 纯异步模式: 请求, 不关心响应
 * <li>
 * 伪异步模式: 请求, 执行其他操作, 当响应时通知
 * </ul>
 *
 * <br>The three different protocol modes:
 * <ul>
 * <li>
 * synchronous: ask and wait for answer
 * <li>
 * asynchronous: ask but ignore answer
 * <li>
 * pseudo-asynchronous: ask and do something else, let me know if answer's back
 * </ul>
 *
 * @author chentao
 * <p>
 * Created on 2010-10-27, 2010
 */
public enum ProtocolMode {
    SYNCHRONOUS,
	ASYNCHRONOUS,
	PSEUDO_ASYNCHRONOUS
}
