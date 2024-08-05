package com.github.shaunhurryup.easycoderjetbrainsplugin.toolWindow

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.content.ContentFactory
import com.github.shaunhurryup.easycoderjetbrainsplugin.MyBundle
import com.github.shaunhurryup.easycoderjetbrainsplugin.services.MyProjectService
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel

class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(toolWindow: ToolWindow) {

        private val service = toolWindow.project.service<MyProjectService>()

        fun getContent() = JBPanel<JBPanel<*>>().apply {
            layout = BorderLayout()

            val label = JBLabel(MyBundle.message("randomLabel", "?"))
            val topPanel = JPanel()
            topPanel.add(label)
            topPanel.add(JButton(MyBundle.message("shuffle")).apply {
                addActionListener {
                    label.text = MyBundle.message("randomLabel", service.getRandomNumber())
                }
            })
            add(topPanel, BorderLayout.NORTH)

            // 使用 JBCefBrowser 加载本地 HTML 文件
            if (!JBCefApp.isSupported()) {
                throw RuntimeException("JCEF is not supported")
            }

            // 创建 JBCefClient 实例
            val client = JBCefApp.getInstance().createClient()

            // 创建 JBCefBrowser 实例
            val browser = JBCefBrowser("file://path/to/dist/index.html")

            add(browser.component, BorderLayout.CENTER)
        }
    }
}
