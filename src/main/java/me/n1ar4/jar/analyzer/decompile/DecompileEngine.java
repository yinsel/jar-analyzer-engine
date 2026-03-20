/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.decompile;

import me.n1ar4.jar.analyzer.engine.EngineConst;
import me.n1ar4.jar.analyzer.engine.log.LogManager;
import me.n1ar4.jar.analyzer.engine.log.Logger;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Decompile Engine
 */
public class DecompileEngine {
    private static final String JAVA_DIR = "jar-analyzer-decompile";
    private static final String JAVA_FILE = ".java";
    private static final String FERN_PREFIX = "//\n" +
            "// Jar Analyzer Engine by 4ra1n\n" +
            "// (powered by FernFlower decompiler)\n" +
            "//\n";

    /**
     * CLI 模式：从 temp 目录中反编译指定的 class 并返回源码字符串
     * <p>
     * 前提：已通过 EngineBuildRunner 完成 JAR 分析，class 文件已解压到 jar-analyzer-temp 目录
     * 不依赖 GUI 组件，可直接在命令行中使用
     *
     * @param className 类的全限定名（例如 com.example.MyClass 或 com/example/MyClass）
     * @return 反编译后的 Java 源代码，失败返回 null
     */
    public static String decompileClass(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }

        // 统一类名格式：com.example.MyClass -> com/example/MyClass
        String classPathStr = className.replace('.', '/');
        // 确保以 .class 结尾
        if (!classPathStr.endsWith(".class")) {
            classPathStr = classPathStr + ".class";
        }

        Path tempDir = Paths.get(EngineConst.tempDir);
        if (!Files.exists(tempDir)) {
            System.err.println("Error: temp directory does not exist: " + tempDir.toAbsolutePath());
            System.err.println("Please run build first: java -jar engine.jar --jar <path>");
            return null;
        }

        // 在 temp 目录中查找 class 文件
        Path classFilePath = tempDir.resolve(classPathStr);

        // 如果直接路径找不到，尝试 BOOT-INF/classes/ 和 WEB-INF/classes/ 前缀
        if (!Files.exists(classFilePath)) {
            Path bootInfPath = tempDir.resolve("BOOT-INF/classes/" + classPathStr);
            Path webInfPath = tempDir.resolve("WEB-INF/classes/" + classPathStr);
            if (Files.exists(bootInfPath)) {
                classFilePath = bootInfPath;
            } else if (Files.exists(webInfPath)) {
                classFilePath = webInfPath;
            } else {
                System.err.println("Error: class file not found: " + classFilePath.toAbsolutePath());
                // 尝试模糊搜索并给出建议
                String simpleFileName = Paths.get(classPathStr).getFileName().toString();
                List<String> candidates = new ArrayList<>();
                try {
                    Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            if (file.getFileName().toString().equals(simpleFileName)) {
                                // 转换为类名格式
                                String relative = tempDir.relativize(file).toString()
                                        .replace('\\', '/').replaceAll("\\.class$", "")
                                        .replace('/', '.');
                                candidates.add(relative);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException ignored) {
                }
                if (!candidates.isEmpty()) {
                    System.err.println("Did you mean one of these?");
                    for (String candidate : candidates) {
                        System.err.println("  - " + candidate);
                    }
                }
                return null;
            }
        }

        // 反编译输出目录
        Path deDirPath = tempDir.resolve(JAVA_DIR);
        try {
            if (!Files.exists(deDirPath)) {
                Files.createDirectories(deDirPath);
            }
        } catch (IOException e) {
            System.err.println("Error: cannot create decompile output dir: " + e.getMessage());
            return null;
        }
        String javaDir = deDirPath.toAbsolutePath().toString();

        // 获取类名前缀（不含 .class），用于查找内部类 ($)
        String fileName = classFilePath.getFileName().toString();
        String classNamePrefix = fileName.split("\\.")[0];
        String newFileName = classNamePrefix + JAVA_FILE;
        Path newFilePath = deDirPath.resolve(newFileName);

        // 删除可能存在的旧反编译文件
        try {
            Files.deleteIfExists(newFilePath);
        } catch (IOException ignored) {
        }

        // 查找内部类文件
        List<String> extraClassList = new ArrayList<>();
        Path classDirPath = classFilePath.getParent();
        if (Files.exists(classDirPath)) {
            try {
                Files.walkFileTree(classDirPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.getFileName().toString().startsWith(classNamePrefix + "$")) {
                            extraClassList.add(file.toAbsolutePath().toString());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ignored) {
            }
        }

        // 构造 ConsoleDecompiler 命令参数
        List<String> cmd = new ArrayList<>();
        cmd.add(classFilePath.toAbsolutePath().toString());
        cmd.addAll(extraClassList);
        cmd.add(javaDir);

        try {
            // FERN FLOWER API
            ConsoleDecompiler.main(cmd.toArray(new String[0]));
        } catch (Throwable t) {
            System.err.println("Warning: decompile error: " + t.getMessage());
        }

        if (Files.exists(newFilePath)) {
            try {
                byte[] code = Files.readAllBytes(newFilePath);
                String codeStr = new String(code);
                // 删除临时反编译文件
                try {
                    Files.delete(newFilePath);
                } catch (IOException ignored) {
                }
                return FERN_PREFIX + codeStr;
            } catch (IOException e) {
                System.err.println("Error: cannot read decompiled file: " + e.getMessage());
                return null;
            }
        } else {
            System.err.println("Error: decompilation produced no output for: " + className);
            return null;
        }
    }
}
