package cc.unitmesh.devti.analysis

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import com.intellij.psi.util.PsiTreeUtil


class JavaModifier(val project: Project) {
    private val psiElementFactory = JavaPsiFacade.getElementFactory(project)
    private val controllers = getAllControllerFiles()

    private fun getAllControllerFiles(): List<PsiFile> {
        val psiManager = PsiManager.getInstance(project)

        val searchScope: GlobalSearchScope = ProjectScope.getContentScope(project)
        val javaFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, searchScope)

        return filterFiles(javaFiles, psiManager, ::controllerFilter)
    }

    private fun filterFiles(
        javaFiles: Collection<VirtualFile>,
        psiManager: PsiManager,
        filter: (PsiClass) -> Boolean
    ) = javaFiles
        .mapNotNull { virtualFile -> psiManager.findFile(virtualFile) }
        .filter { psiFile ->
            val javaClasses = PsiTreeUtil.findChildrenOfType(psiFile, PsiClass::class.java)
            javaClasses.any(filter)
        }

    private fun controllerFilter(clazz: PsiClass) =
        clazz.hasAnnotation("Controller") || clazz.hasAnnotation("RestController")

    fun addMethodToClass(psiClass: PsiClass, method: String): PsiClass {
        val methodFromText = psiElementFactory.createMethodFromText(method, psiClass)
        var lastMethod: PsiMethod? = null
        val allMethods = psiClass.methods

        if (allMethods.isNotEmpty()) {
            lastMethod = allMethods[allMethods.size - 1]
        }

        if (lastMethod != null) {
            psiClass.addAfter(methodFromText, lastMethod)
        } else {
            psiClass.add(methodFromText)
        }

        return psiClass
    }
}