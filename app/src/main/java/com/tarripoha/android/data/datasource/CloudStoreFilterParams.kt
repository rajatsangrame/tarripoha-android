package com.tarripoha.android.data.datasource

data class CloudStoreFilterParams(
    val data: Map<String, Any>,
    var sortField: String? = null,
    var asc: Boolean? = null,
    var cursor: String? = null,
    val limit: Long = 20
)