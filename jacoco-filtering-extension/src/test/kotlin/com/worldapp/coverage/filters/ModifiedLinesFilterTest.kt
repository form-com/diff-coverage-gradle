package com.worldapp.coverage.filters

import com.worldapp.diff.CodeUpdateInfo
import io.kotlintest.data.forall
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import org.jacoco.core.internal.analysis.filter.IFilterContext
import org.jacoco.core.internal.analysis.filter.IFilterOutput
import org.objectweb.asm.tree.*
import kotlin.reflect.KClass

class ModifiedLinesFilterTest : StringSpec({

    "filter should ignore all non-modified lines" {
        // setup
        val classPackage = "com/worldapp"
        val classFileName = "Class.java"
        val contextClassName = "$classPackage/Class"
        val classUpdateInfo = CodeUpdateInfo(
                mapOf("$classPackage/$classFileName" to setOf(51))
        )
        val context = mockk<IFilterContext> {
            every { className } returns contextClassName
            every { sourceFileName } returns classFileName
        }

        val modifiedLineInstructions = listOf(
                lineNode(51,
                        VarInsnNode::class, MethodInsnNode::class, VarInsnNode::class, InsnNode::class,
                        VarInsnNode::class, VarInsnNode::class, VarInsnNode::class, MethodInsnNode::class),
                lineNode(51, VarInsnNode::class)
        )

        val instructionsToIgnorePartOne = listOf(
                lineNode(44, TypeInsnNode::class, InsnNode::class, MethodInsnNode::class, VarInsnNode::class),
                lineNode(45, VarInsnNode::class),
                lineNode(46, VarInsnNode::class, FieldInsnNode::class),
                lineNode(47,
                        TypeInsnNode::class, InsnNode::class, MethodInsnNode::class, MethodInsnNode::class,
                        InsnNode::class, LdcInsnNode::class, MethodInsnNode::class),
                lineNode(45, MethodInsnNode::class, VarInsnNode::class),
                lineNode(49,
                        VarInsnNode::class, MethodInsnNode::class, TypeInsnNode::class, MethodInsnNode::class,
                        TypeInsnNode::class, VarInsnNode::class),
                lineNode(50, VarInsnNode::class, MethodInsnNode::class, TypeInsnNode::class, VarInsnNode::class)
        )
        val instructionsToIgnorePartTwo = listOf(
                lineNode(53, InsnNode::class, VarInsnNode::class),
                lineNode(54,
                        InsnNode::class, VarInsnNode::class, InsnNode::class, VarInsnNode::class, InsnNode::class,
                        VarInsnNode::class, VarInsnNode::class, VarInsnNode::class, LabelNode::class, FrameNode::class,
                        VarInsnNode::class, VarInsnNode::class, JumpInsnNode::class, VarInsnNode::class,
                        VarInsnNode::class, LabelNode::class, InsnNode::class, VarInsnNode::class),
                lineNode(55,
                        VarInsnNode::class, MethodInsnNode::class, VarInsnNode::class, InsnNode::class,
                        VarInsnNode::class, VarInsnNode::class, InsnNode::class, InsnNode::class, VarInsnNode::class),
                lineNode(56, VarInsnNode::class, VarInsnNode::class, InsnNode::class, VarInsnNode::class),
                lineNode(57, TypeInsnNode::class, InsnNode::class, VarInsnNode::class, MethodInsnNode::class, VarInsnNode::class),
                lineNode(58,
                        VarInsnNode::class, TypeInsnNode::class, VarInsnNode::class, MethodInsnNode::class,
                        VarInsnNode::class, VarInsnNode::class, VarInsnNode::class, MethodInsnNode::class,
                        MethodInsnNode::class, InsnNode::class),
                lineNode(59,
                        VarInsnNode::class, TypeInsnNode::class, VarInsnNode::class, TypeInsnNode::class, InsnNode::class,
                        VarInsnNode::class, TypeInsnNode::class, InsnNode::class, InsnNode::class, InsnNode::class,
                        MethodInsnNode::class, VarInsnNode::class, InsnNode::class, VarInsnNode::class, VarInsnNode::class,
                        VarInsnNode::class, MethodInsnNode::class, InsnNode::class),
                lineNode(60, VarInsnNode::class, VarInsnNode::class),
                lineNode(61, InsnNode::class, LabelNode::class, InsnNode::class),
                lineNode(54, IincInsnNode::class, JumpInsnNode::class),
                lineNode(63, FrameNode::class, VarInsnNode::class, TypeInsnNode::class, InsnNode::class, LabelNode::class)
        )

        val instructionsList = InsnList().apply {
            instructionsToIgnorePartOne
                    .union(modifiedLineInstructions)
                    .union(instructionsToIgnorePartTwo)
                    .flatten()
                    .forEach(::add)
        }

        val methodNode = MethodNode().apply {
            instructions = instructionsList
        }

        val output = mockk<IFilterOutput>(relaxed = true)

        // run
        ModifiedLinesFilter(classUpdateInfo).filter(
                methodNode,
                context,
                output
        )

        // assert
        instructionsToIgnorePartOne.union(instructionsToIgnorePartTwo).forEach {
            verify(exactly = 1) {
                output.ignore(it.first(), it.last())
            }
        }

        modifiedLineInstructions.forEach {
            verify(exactly = 0) {
                output.ignore(it.first(), it.last())
            }
        }
    }

    "filter should correctly fetch class modifications" {
        forall(
                row("com/wa/ModClass"),
                row("com/wa/ModClass\$InnerClass"),
                row("com/wa/ModClass\$InnerClass\$Lambda"),
                row("com/wa/ClassNameDoesNotMatchSourceFile")
        ) { contextClassName ->
            // setup
            val modifiedFilePath = "module/src/main/kotlin/com/wa/ModClass.kt"
            val classFileName = "ModClass.kt"
            val classUpdateInfo = CodeUpdateInfo(
                    mapOf(modifiedFilePath to setOf(2))
            )
            val context = mockk<IFilterContext> {
                every { className } returns contextClassName
                every { sourceFileName } returns classFileName
            }

            val modifiedLine: Set<AbstractInsnNode> = lineNode(2)
            val instructionsToIgnore: Set<AbstractInsnNode> = lineNode(1)

            val instructionsList = InsnList().apply {
                instructionsToIgnore.union(modifiedLine).forEach(::add)
            }

            val methodNode = MethodNode().apply {
                instructions = instructionsList
            }

            val output = mockk<IFilterOutput>(relaxed = true)

            // run
            ModifiedLinesFilter(classUpdateInfo).filter(
                    methodNode,
                    context,
                    output
            )

            // assert
            verify(exactly = 1) {
                output.ignore(instructionsToIgnore.first(), instructionsToIgnore.last())
            }

            verify(exactly = 0) {
                output.ignore(modifiedLine.first(), modifiedLine.last())
            }
        }

    }
})

fun lineNode(
        line: Int,
        vararg lineNodes: KClass<out AbstractInsnNode>
): Set<AbstractInsnNode> {
    return LineNumberNode(line, null).let { lineNode ->
        listOf(
                mockk<LabelNode> { every { next } returns lineNode },
                lineNode
        ).union(lineNodes.map {
            mockkClass(it) {
                every { next } returns mockk()
            }
        })
    }
}