package me.fornever.todosaurus.testFramework

import com.intellij.diagnostic.ActivityCategory
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.messages.MessageBus

@Suppress("UnstableApiUsage")
class FakeProject : Project {
    override fun <T : Any?> getUserData(p0: Key<T>): T = error("Not implemented.")

    override fun <T : Any?> putUserData(p0: Key<T>, p1: T?) = error("Not implemented.")

    override fun dispose() = error("Not implemented.")

    override fun getExtensionArea(): ExtensionsArea = error("Not implemented.")

    @Deprecated("Deprecated in Java", ReplaceWith("error(\"Not implemented.\")"))
    override fun <T : Any?> getComponent(p0: Class<T>): T = error("Not implemented.")

    override fun hasComponent(p0: Class<*>): Boolean = error("Not implemented.")

    override fun isInjectionForExtensionSupported(): Boolean = error("Not implemented.")

    override fun getMessageBus(): MessageBus = error("Not implemented.")

    override fun isDisposed(): Boolean = error("Not implemented.")

    override fun getDisposed(): Condition<*> = error("Not implemented.")

    override fun <T : Any?> getService(p0: Class<T>): T = error("Not implemented.")

    override fun <T : Any?> instantiateClass(p0: Class<T>, p1: PluginId): T = error("Not implemented.")

    override fun <T : Any?> instantiateClass(p0: String, p1: PluginDescriptor): T & Any = error("Not implemented.")

    override fun <T : Any?> instantiateClassWithConstructorInjection(p0: Class<T>, p1: Any, p2: PluginId): T = error("Not implemented.")

    override fun createError(p0: Throwable, p1: PluginId): RuntimeException = error("Not implemented.")

    override fun createError(p0: String, p1: PluginId): RuntimeException = error("Not implemented.")

    override fun createError(
        p0: String,
        p1: Throwable?,
        p2: PluginId,
        p3: MutableMap<String, String>?
    ): RuntimeException = error("Not implemented.")

    override fun <T : Any?> loadClass(p0: String, p1: PluginDescriptor): Class<T> = error("Not implemented.")

    override fun getActivityCategory(p0: Boolean): ActivityCategory = error("Not implemented.")

    override fun getName(): String = error("Not implemented.")

    @Deprecated("Deprecated in Java", ReplaceWith("error(\"Not implemented.\")"))
    override fun getBaseDir(): VirtualFile = error("Not implemented.")

    override fun getBasePath(): String = error("Not implemented.")

    override fun getProjectFile(): VirtualFile = error("Not implemented.")

    override fun getProjectFilePath(): String = error("Not implemented.")

    override fun getWorkspaceFile(): VirtualFile = error("Not implemented.")

    override fun getLocationHash(): String = error("Not implemented.")

    override fun save() = error("Not implemented.")

    override fun isOpen(): Boolean = error("Not implemented.")

    override fun isInitialized(): Boolean = error("Not implemented.")
}
