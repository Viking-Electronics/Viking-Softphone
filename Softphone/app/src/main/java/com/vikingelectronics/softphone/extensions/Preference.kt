package com.vikingelectronics.softphone.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.tfcporciuncula.flow.Preference
import kotlinx.coroutines.flow.Flow

interface NonSettablePreference<T> {
    fun asFlow(): Flow<T>
    fun get(): T
    fun isNotSet(): Boolean
    fun isSet(): Boolean
    @Composable
    fun collectAsFlowState(): State<T>
}

fun <T> Preference<T>.nonSettable(): NonSettablePreference<T> = object : NonSettablePreference<T> {
    override fun asFlow(): Flow<T> = this@nonSettable.asFlow()
    override fun get(): T = this@nonSettable.get()
    override fun isNotSet(): Boolean = this@nonSettable.isNotSet()
    override fun isSet(): Boolean = this@nonSettable.isNotSet()

    @Composable
    override fun collectAsFlowState(): State<T> = this@nonSettable.asFlow().collectAsState(initial = get())
}