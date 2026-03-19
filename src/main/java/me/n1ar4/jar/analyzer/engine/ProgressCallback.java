/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.engine;

/**
 * 引擎构建进度回调
 * GUI 模式下可用于更新进度条和日志面板
 * CLI 模式下可用于打印进度信息
 */
public interface ProgressCallback {
    /**
     * 更新进度百分比
     *
     * @param percent 0-100
     */
    void onProgress(int percent);

    /**
     * 输出信息日志
     */
    void onInfo(String message);

    /**
     * 输出警告日志
     */
    void onWarn(String message);

    /**
     * 输出错误日志
     */
    void onError(String message);

    /**
     * 统计信息回调
     */
    void onStats(String key, String value);

    /**
     * 默认的控制台回调（CLI 模式使用）
     */
    ProgressCallback CONSOLE = new ProgressCallback() {
        @Override
        public void onProgress(int percent) {
            System.out.printf("[ENGINE] Progress: %d%%\n", percent);
        }

        @Override
        public void onInfo(String message) {
            System.out.printf("[ENGINE] INFO: %s\n", message);
        }

        @Override
        public void onWarn(String message) {
            System.out.printf("[ENGINE] WARN: %s\n", message);
        }

        @Override
        public void onError(String message) {
            System.err.printf("[ENGINE] ERROR: %s\n", message);
        }

        @Override
        public void onStats(String key, String value) {
            System.out.printf("[ENGINE] %s: %s\n", key, value);
        }
    };
}
