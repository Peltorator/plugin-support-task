package com.h0tk3y.player.test

import com.h0tk3y.player.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.test.*

private val thirdPartyPluginClasses: List<File> =
    System.getProperty("third-party-plugin-classes").split(File.pathSeparator).map { File(it) }

private val adPluginClasses: List<File> =
    System.getProperty("adPlugin-classes").split(File.pathSeparator).map { File(it) }

private val usageStatsPluginName = "com.h0tk3y.third.party.plugin.UsageStatsPlugin"
private val pluginWithAppPropertyName = "com.h0tk3y.third.party.plugin.PluginWithAppProperty"

private val pName = "com.h0tk3y.ad.AdPlugin"

class AddPluginTest {

    private val defaultEnabledPlugins = setOf(
        StaticPlaylistsLibraryContributor::class.java.canonicalName,
        usageStatsPluginName,
        pluginWithAppPropertyName
    )

    private fun withApp(
        wipePersistedData: Boolean = false,
        pluginClasspath: List<File> = thirdPartyPluginClasses,
        enabledPlugins: Set<String> = defaultEnabledPlugins,
        doTest: TestableMusicApp.() -> Unit
    ) {
        val app = TestableMusicApp(pluginClasspath, enabledPlugins)
        if (wipePersistedData) {
            app.wipePersistedPluginData()
        }
        app.use {
            it.init()
            it.doTest()
        }
    }

    @Test
    fun adPluginTest() {
        
    }
}