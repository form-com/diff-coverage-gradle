package com.worldapp.coverage.filters

import com.worldapp.diff.ClassModifications
import org.jacoco.core.internal.analysis.filter.IFilter
import org.jacoco.core.internal.analysis.filter.IFilterContext
import org.jacoco.core.internal.analysis.filter.IFilterOutput
import org.objectweb.asm.tree.*
import org.slf4j.LoggerFactory
import java.util.*

class ModifiedLinesFilter(private val classModifications: ClassModifications) : IFilter {

    override fun filter(
            methodNode: MethodNode,
            context: IFilterContext,
            output: IFilterOutput
    ) {
        val groupedModifiedLines = collectLineNodes(methodNode.instructions).groupBy {
            classModifications.isLineModified(it.lineNode.line)
        }

        groupedModifiedLines[false]?.forEach {
            output.ignore(it.lineNode.previous, it.lineNodeLastInstruction)
        }

        if(log.isDebugEnabled) {
            log.debug("Modified lines in ${context.className}#${methodNode.name}")
            val lines = groupedModifiedLines[true]
                    ?.map { it.lineNode.line }
                    ?: emptyList()
            log.debug("\tlines: $lines")
        }
    }

    private fun collectLineNodes(instructionNodes: InsnList): Sequence<LineNode> {
        val lineNodes = ArrayList<LineNode>()

        val iterator = instructionNodes.iterator()
        val nextLineNode = getNextLineNode(iterator) ?: return emptySequence()

        var currentNode = LineNode(nextLineNode)
        while (iterator.hasNext()) {
            val instructionNode = iterator.next()
            if (instructionNode is LabelNode && instructionNode.next is LineNumberNode) {
                lineNodes.add(currentNode)
                currentNode = LineNode(instructionNode.next as LineNumberNode)
            } else {
                currentNode.lineNodeLastInstruction = instructionNode
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

    private companion object {
        val log = LoggerFactory.getLogger( ModifiedLinesFilter::class.java )
    }
}
