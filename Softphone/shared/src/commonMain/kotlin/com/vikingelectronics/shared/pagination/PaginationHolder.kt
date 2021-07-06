package com.vikingelectronics.shared.pagination

import dev.gitlive.firebase.firestore.DocumentSnapshot

data class PaginationHolder<T> (val entries: List<T>, val index: DocumentSnapshot?)
