package io.kudos.tools.codegen.core.merge

import java.util.regex.Pattern

/**
 * import语句抓取器
 *
 * @author K
 * @since 1.0.0
 */
class ImportStmtRetriever(private val fileContent: CharSequence) {
    fun retrieveImports(): List<String> {
        val imports: MutableList<String> = ArrayList()
        val p = Pattern.compile("import .+")
        val m = p.matcher(fileContent)
        while (m.find()) {
            imports.add(m.group(0))
        }
        return imports
    }

}