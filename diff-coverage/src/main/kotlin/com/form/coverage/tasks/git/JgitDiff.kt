package com.form.coverage.tasks.git

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.DiffFormatter
import org.eclipse.jgit.lib.ConfigConstants
import org.eclipse.jgit.lib.CoreConfig
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevTree
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.AbstractTreeIterator
import org.eclipse.jgit.treewalk.CanonicalTreeParser
import org.eclipse.jgit.treewalk.FileTreeIterator
import org.eclipse.jgit.treewalk.filter.TreeFilter
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.IllegalArgumentException
import java.util.logging.Logger


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

    fun obtain(commit: String): String {
        val diffContent = ByteArrayOutputStream()
        Git(repository).use {
            DiffFormatter(diffContent).apply {
                initialize()

                scan(
                        getTreeIterator(repository, commit),
                        FileTreeIterator(repository)
                ).forEach {
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
        pathFilter = TreeFilter.ALL
    }

    private fun getTreeIterator(repo: Repository, name: String): AbstractTreeIterator {
        val id: ObjectId = repo.resolve(name)
        val parser = CanonicalTreeParser()
        repo.newObjectReader().use { objectReader ->
            RevWalk(repo).use { revWalk ->
                parser.reset(objectReader, revWalk.parseTree(id))
                return parser
            }
        }
    }
}
