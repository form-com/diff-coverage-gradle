package org.jacoco.core.internal.analysis

import org.jacoco.core.internal.analysis.filter.Filters
import org.jacoco.core.internal.analysis.filter.IFilter
import org.jacoco.core.internal.analysis.filter.IFilterContext
import org.jacoco.core.internal.analysis.filter.IFilterOutput
import org.jacoco.core.internal.flow.MethodProbesVisitor
import org.jacoco.core.internal.instr.InstrSupport
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.tree.MethodNode

internal open class FilteringClassAnalyzer(
        private val coverage: ClassCoverageImpl,
        private val probes: BooleanArray?,
        private val stringPool: StringPool,
        customFilter: IFilter
) : ClassAnalyzer(coverage, probes, stringPool) {

    private val filter: IFilter

    init {
        this.filter = createFilters(customFilter)
    }

    private fun createFilters(customFilter: IFilter): IFilter {
        return object : IFilter {
            private val allFilters = Filters.all()
            override fun filter(
                    methodNode: MethodNode, context: IFilterContext, output: IFilterOutput
            ) {
                allFilters.filter(methodNode, context, output)
                customFilter.filter(methodNode, context, output)
            }
        }
    }

    override fun visitMethod(
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            exceptions: Array<String>?
    ): MethodProbesVisitor {
        InstrSupport.assertNotInstrumented(name, coverage.name)

        val builder = InstructionsBuilder(probes)
        return object : MethodAnalyzer(builder) {

            override fun accept(methodNode: MethodNode, methodVisitor: MethodVisitor) {
                super.accept(methodNode, methodVisitor)
                addMethodCoverage(
                        stringPool[name],
                        stringPool[desc],
                        stringPool[signature],
                        builder,
                        methodNode
                )
            }
        }
    }

    private fun addMethodCoverage(
            name: String,
            desc: String,
            signature: String?,
            icc: InstructionsBuilder,
            methodNode: MethodNode
    ) {
        val methodCoverageCalculator = MethodCoverageCalculator(icc.instructions)
        filter.filter(methodNode, this, methodCoverageCalculator)

        MethodCoverageImpl(name, desc, signature).run {
            methodCoverageCalculator.calculate(this)
            if (containsCode()) {
                coverage.addMethod(this)
            }
        }
    }
}
