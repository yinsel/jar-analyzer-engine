/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.engine.EngineConst;
import org.objectweb.asm.ClassVisitor;

public class FixClassVisitor extends ClassVisitor {
    private String name;

    public String getName() {
        return name;
    }

    public FixClassVisitor() {
        super(EngineConst.ASMVersion);
    }

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        this.name = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
}
