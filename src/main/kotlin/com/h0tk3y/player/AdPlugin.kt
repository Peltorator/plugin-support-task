package com.h0tk3y.ad

import com.h0tk3y.player.*
import java.io.File
import java.io.InputStream
import java.io.OutputStream



class AdPlugin(override val musicAppInstance: MusicApp) : PlaybackListenerPlugin {

    private var counter = 0
    private var listOfAds: MutableList<Pair<String, Int>> = mutableListOf(
        Pair("Cheesy cheese", 620),
        Pair("Coke", 200),
        Pair("Smetany smetana", 150),
        Pair("Nutsy nuts", 350),
        Pair("Milky milk", 230),
        Pair("Water", 95),
        Pair("Stickers", 450),
        Pair("Gamy games", 1750)
    )


    override fun onPlaybackStateChange(oldPlaybackState: PlaybackState, newPlaybackState: PlaybackState) {
        when (newPlaybackState) {
            is PlaybackState.Playing -> {

                println("Buy ${listOfAds[counter].first} only in our store for only ${listOfAds[counter].second / 100}.${listOfAds[counter].second % 100}$. Best price ever!")
                counter += 1
                if (counter == listOfAds.size) {
                    counter = 0
                    listOfAds = listOfAds.map {
                        it.copy(second = it.second * 2 / 3)
                    }.toMutableList()
                    listOfAds.shuffle()
                }
            }
        }
    }

    override fun init(persistedState: InputStream?) = Unit

    override fun persist(stateStream: OutputStream) = Unit
}