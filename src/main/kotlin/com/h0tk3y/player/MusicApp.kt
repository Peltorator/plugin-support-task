package com.h0tk3y.player

import java.io.File
import java.io.FileOutputStream
import java.net.URLClassLoader
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

open class MusicApp(
    private val pluginClasspath: List<File>,
    private val enabledPluginClasses: Set<String>
) : AutoCloseable {
    fun init() {
        /**
         * TODO: Инициализировать плагины с помощью функции [MusicPlugin.init],
         *       предоставив им байтовые потоки их состояния (для тех плагинов, для которых они сохранены).
         *       Обратите внимание на cлучаи, когда необходимо выбрасывать исключения
         *       [IllegalPluginException] и [PluginClassNotFoundException].
         **/

        plugins.forEach {
            it.init(if (File("tmp/${it.pluginId}").exists()) File("tmp/${it.pluginId}").inputStream() else null)
        }

        musicLibrary // access to initialize
        player.init()
    }

    override fun close() {
        if (isClosed) return
        isClosed = true

        /** TODO: Сохранить состояние плагинов с помощью [MusicPlugin.persist]. */
        plugins.forEach {
            it.persist(FileOutputStream(File("tmp/${it.pluginId}"), true))
        }
    }

    fun wipePersistedPluginData() {
        // TODO: Удалить сохранённое состояние плагинов.
        plugins.forEach {
            File("tmp/${it.pluginId}").delete()
        }
    }

    private val pluginClassLoader: ClassLoader = URLClassLoader(pluginClasspath.map {
        it.toURI().toURL()
    }.toTypedArray())
    // TODO("Создать загрузчик классов для плагинов.")

    private val plugins: List<MusicPlugin> by lazy {
        /**
         * TODO используя [pluginClassLoader] и следуя контракту [MusicPlugin],
         *      загрузить плагины, перечисленные в [enabledPluginClasses].
         *      Эта функция не должна вызывать [MusicPlugin.init]
         */

        enabledPluginClasses.map {
            val curClass: KClass<*>
            try {
                curClass = pluginClassLoader.loadClass(it).kotlin
            } catch (_: Exception) {
                throw PluginClassNotFoundException(it)
            }

            try {
                val mp: MusicPlugin = curClass.primaryConstructor?.call(this) as MusicPlugin
                return@map mp
            } catch (_: Exception) {

            }

            try {
                val mp: MusicPlugin = curClass.primaryConstructor?.call() as MusicPlugin
                val pr: KMutableProperty1<MusicPlugin, MusicApp> = mp::class.memberProperties.singleOrNull {
                    it.name == "musicAppInstance"
                } as KMutableProperty1<MusicPlugin, MusicApp>
                pr.set(mp, this)
                return@map mp
            } catch (_: Exception) {

            }

            throw IllegalPluginException(curClass.java)
        }.toList()
    }

    fun findSinglePlugin(pluginClassName: String): MusicPlugin? = plugins.singleOrNull { it::class.qualifiedName == pluginClassName }
    // TODO("Если есть единственный плагин, принадлежащий типу по имени pluginClassName, вернуть его, иначе null.")

    fun <T : MusicPlugin> getPlugins(pluginClass: Class<T>): List<T> =
        plugins.filterIsInstance(pluginClass)

    private val musicLibraryContributors: List<MusicLibraryContributorPlugin>
        get() = getPlugins(MusicLibraryContributorPlugin::class.java)

    protected val playbackListeners: List<PlaybackListenerPlugin>
        get() = getPlugins(PlaybackListenerPlugin::class.java)

    val musicLibrary: MusicLibrary by lazy {
        musicLibraryContributors
            .sortedWith(compareBy({ it.preferredOrder }, { it.pluginId }))
            .fold(MusicLibrary(mutableListOf())) { acc, it -> it.contribute(acc) }
    }

    open val player: MusicPlayer by lazy {
        JLayerMusicPlayer(
            playbackListeners
        )
    }

    fun startPlayback(playlist: Playlist, fromPosition: Int) {
        player.playbackState = PlaybackState.Playing(
            PlaylistPosition(
                playlist,
                fromPosition
            ), isResumed = false)
    }

    fun nextOrStop() = player.playbackState.playlistPosition?.let {
        val nextPosition = it.position + 1
        player.playbackState =
            if (nextPosition in it.playlist.tracks.indices)
                PlaybackState.Playing(
                    PlaylistPosition(
                        it.playlist,
                        nextPosition
                    ), isResumed = false
                )
            else
                PlaybackState.Stopped
    }

    @Volatile
    var isClosed = false
        private set
}