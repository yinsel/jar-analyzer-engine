/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.engine.utils;

import me.n1ar4.jar.analyzer.engine.EngineConst;
import me.n1ar4.jar.analyzer.engine.log.LogManager;
import me.n1ar4.jar.analyzer.engine.log.Logger;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Engine version of CoreUtil - no GUI dependency
 */
public class CoreUtil {
    private static final Logger logger = LogManager.getLogger();

    public static List<ClassFileEntity> getAllClassesFromJars(List<String> jarPathList,
                                                              Map<String, Integer> jarIdMap) {
        logger.info("collect all class");
        Set<ClassFileEntity> classFileSet = new HashSet<>();
        Path temp = Paths.get(EngineConst.tempDir);
        try {
            Files.createDirectory(temp);
        } catch (IOException ignored) {
        }
        for (String jarPath : jarPathList) {
            classFileSet.addAll(JarUtil.resolveNormalJarFile(jarPath, jarIdMap.get(jarPath)));
        }
        return new ArrayList<>(classFileSet);
    }
}
