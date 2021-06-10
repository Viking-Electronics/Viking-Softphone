package com.vikingelectronics.softphone.extensions

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await


@JvmName("unitResultVoid")
suspend fun Task<Void>.unitResult(): Result<Unit> {
    await()
    return exception?.let {
        Result.failure(it)
    } ?: Result.success(Unit)
}

suspend fun Task<Void>.emitUnitResult(): Flow<Result<Unit>> = flow {
    await()
    exception?.let {
        emit(Result.failure<Unit>(it))
    } ?: Result.success(Unit)
}

suspend fun Task<DocumentReference>.unitResult(): Result<Unit> {
    await()
    return exception?.let {
        Result.failure(it)
    } ?: Result.success(Unit)
}