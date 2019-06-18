package com.worldapp.coverage.filters

import com.worldapp.diff.ClassModifications
import org.jacoco.core.internal.analysis.filter.IFilter
import org.jacoco.core.internal.analysis.filter.IFilterContext
import org.jacoco.core.internal.analysis.filter.IFilterOutput
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import java.util.*

class ModifiedLinesFilter(private val classModifications: ClassModifications) : IFilter {

    override fun filter(
            methodNode: MethodNode,
            context: IFilterContext,
            output: IFilterOutput
    ) {
        collectLineNodes(methodNode.instructions)
                .filter {
                    !classModifications.isLineModified(it.lineNode.line)
                }
                .forEach {
                    output.ignore(it.lineNode, it.lineNodeLastInstruction)
                }
    }

    private fun collectLineNodes(instructionNodes: InsnList): Sequence<LineNode> {
        val lineNodes = ArrayList<LineNode>()

        val iterator = instructionNodes.iterator()
        val nextLineNode = getNextLineNode(iterator) ?: return emptySequence()
        var currentNode = LineNode(nextLineNode)
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next is LineNumberNode) {
                lineNodes.add(currentNode)
                currentNode = LineNode(next)
            } else {
                currentNode.lineNodeLastInstruction = next
            }
        }
        lineNodes.add(currentNode)
        return lineNodes.asSequence()
    }


    private fun getNextLineNode(instructionNodes: ListIterator<AbstractInsnNode>): LineNumberNode? {
        while (instructionNodes.hasNext()) {
            val node = instructionNodes.next()
            if (node is LineNumberNode) {
                return node
            }
        }
        return null
    }

    private class LineNode(
            val lineNode: LineNumberNode,
            var lineNodeLastInstruction: AbstractInsnNode = lineNode
    )
}
