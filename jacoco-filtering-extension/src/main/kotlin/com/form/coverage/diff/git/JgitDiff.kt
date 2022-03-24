package com.form.coverage.diff.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.filter.RevFilter
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.filter.TreeFilter
import java.io.ByteArrayOutputStream
import java.io.File

class JgitDiff(workingDir: File) {

    private val repository: Repository = initRepository(workingDir)

    private fun initRepository(workingDir: File): Repository = try {
        FileRepositoryBuilder().apply {
            findGitDir(workingDir)
            readEnvironment()
            isMustExist = true
        }.build()
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException(
            "Git directory not found in the project root ${workingDir.absolutePath}",
            e
        )
    }

    fun obtain(revision: String): String {
        val diffContent = ByteArrayOutputStream()
        Git(repository).use { git ->
            DiffFormatter(diffContent).apply {
                initialize()

                obtainDiffEntries(git, revision).forEach {
                    format(it)
                }

                close()
            }
        }

        return String(diffContent.toByteArray())
    }

    private fun DiffFormatter.initialize() {
        setRepository(repository)
        repository.config.setEnum(
            ConfigConstants.CONFIG_CORE_SECTION,
            null,
            ConfigConstants.CONFIG_KEY_AUTOCRLF,
            getCrlf()
        )
        setQuotePaths(false)
        pathFilter = TreeFilter.ALL
    }

    private fun obtainDiffEntries(git: Git, target: String): List<DiffEntry> {
        repository.newObjectReader().use { reader ->
            RevWalk(repository).use { revWalk ->
                revWalk.revFilter = RevFilter.MERGE_BASE

                val targetId: ObjectId = repository.resolve(target) ?: throw buildUnknownRevisionException(target)
                revWalk.markStart(revWalk.parseCommit(targetId))

                val currentHeadCommit: RevCommit = revWalk.parseCommit(repository.resolve(Constants.HEAD))
                revWalk.markStart(currentHeadCommit)

                val targetRevisionTreeParser: AbstractTreeIterator = CanonicalTreeParser().apply {
                    // Be careful, commits may have multiple merge bases where diff A...B is complicated
                    val base: RevCommit = revWalk.parseCommit(revWalk.next())
                    reset(reader, base.tree)
                }

                val currentHeadTreeParser = CanonicalTreeParser().apply {
                    reset(reader, currentHeadCommit.tree)
                }

                return git.diff()
                        .setOldTree(targetRevisionTreeParser)
                        .setNewTree(currentHeadTreeParser)
                        .setCached(true)
                        .call()
            }
        }
    }

    private fun buildUnknownRevisionException(name: String): UnknownRevisionException {
        return UnknownRevisionException(
            """
            Unknown revision '$name'. Available branches: ${branches()}
            """.trimIndent()
        )
    }

    private fun branches(): String {
        return Git(repository).branchList().call()
            .asSequence()
            .map { it.name }
            .sorted()
            .joinToString(", ")
    }

}

class UnknownRevisionException(message: String) : RuntimeException(message)
