package com.worldapp.coverage.filters

import com.worldapp.diff.ClassModifications
import io.kotlintest.specs.StringSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import org.jacoco.core.internal.analysis.filter.IFilterOutput
import org.objectweb.asm.tree.*
import kotlin.reflect.KClass

class ModifiedLinesFilterTest : StringSpec({

    "filter should ignore all non-modified lines" {
        // setup
        val classModifications = ClassModifications(setOf(51))

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
        ModifiedLinesFilter(classModifications).filter(
                methodNode,
                mockk {
                    every { className } returns "Class"
                },
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