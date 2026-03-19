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

import com.beust.jcommander.JCommander;
import me.n1ar4.jar.analyzer.engine.log.LogManager;
import me.n1ar4.jar.analyzer.engine.log.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EngineMain {
    private static final Logger logger = LogManager.getLogger();

    @SuppressWarnings("all")
    public static void main(String[] args) {
        logger.info("=== Jar Analyzer Engine {} ===", EngineConst.version);
        logger.info("Build SQLite database from JAR/WAR files");

        EngineBuildCmd cmd = new EngineBuildCmd();
        JCommander jc = JCommander.newBuilder()
                .addObject(cmd)
                .build();
        jc.setProgramName("jar-analyzer-engine");

        try {
            jc.parse(args);
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
            jc.usage();
            System.exit(1);
            return;
        }

        if (cmd.help) {
            jc.usage();
            return;
        }

        if (cmd.jarPath == null || cmd.jarPath.isEmpty()) {
            logger.error("Error: --jar parameter is required");
            jc.usage();
            System.exit(1);
            return;
        }

        Path jarPath = Paths.get(cmd.jarPath);
        if (!Files.exists(jarPath)) {
            logger.error("Error: JAR path does not exist: {}", cmd.jarPath);
            System.exit(1);
            return;
        }

        // Build EngineConfig from CLI parameters
        EngineConfig config = new EngineConfig();
        config.setJarPath(jarPath);
        config.setQuickMode(cmd.quickMode);
        config.setFixClass(cmd.fixClass);
        config.setJarsInJar(cmd.jarsInJar);
        config.setFixMethodImpl(!cmd.noFixMethodImpl);
        config.setProgressCallback(ProgressCallback.CONSOLE);

        if (cmd.rtJarPath != null && !cmd.rtJarPath.isEmpty()) {
            Path rtPath = Paths.get(cmd.rtJarPath);
            if (Files.exists(rtPath)) {
                config.setRtJarPath(rtPath);
            } else {
                logger.warn("rt.jar path does not exist: {}", cmd.rtJarPath);
            }
        }

        // Handle black list
        if (cmd.classBlackListFile != null && !cmd.classBlackListFile.isEmpty()) {
            try {
                config.setClassBlackList(new String(Files.readAllBytes(
                        Paths.get(cmd.classBlackListFile))));
            } catch (IOException e) {
                logger.warn("cannot read black list file: {}", cmd.classBlackListFile);
            }
        } else if (cmd.classBlackList != null) {
            config.setClassBlackList(cmd.classBlackList);
        }

        // Handle white list
        if (cmd.classWhiteListFile != null && !cmd.classWhiteListFile.isEmpty()) {
            try {
                config.setClassWhiteList(new String(Files.readAllBytes(
                        Paths.get(cmd.classWhiteListFile))));
            } catch (IOException e) {
                logger.warn("cannot read white list file: {}", cmd.classWhiteListFile);
            }
        } else if (cmd.classWhiteList != null) {
            config.setClassWhiteList(cmd.classWhiteList);
        }

        // Print config summary
        logger.info("Configuration:");
        logger.info("  JAR Path:      {}", config.getJarPath());
        logger.info("  DB Path:       jar-analyzer.db");
        logger.info("  Temp Dir:      {}", EngineConst.tempDir);
        logger.info("  Quick Mode:    {}", config.isQuickMode());
        logger.info("  Fix Class:     {}", config.isFixClass());
        logger.info("  Inner JARs:    {}", config.isJarsInJar());
        logger.info("  Fix Impl:      {}", config.isFixMethodImpl());
        logger.info("  Black List:    {}", config.getClassBlackList() != null ? "set" : "none");
        logger.info("  White List:    {}", config.getClassWhiteList() != null ? "set" : "none");

        long startTime = System.currentTimeMillis();

        try {
            EngineBuildRunner.run(config);
        } catch (Exception e) {
            logger.error("build failed: {}", e.toString());
            e.printStackTrace();
            System.exit(1);
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("=== Build Complete ===");
        logger.info("Time elapsed: {} seconds", String.format("%.2f", elapsed / 1000.0));
        logger.info("Database: jar-analyzer.db");
    }
}
