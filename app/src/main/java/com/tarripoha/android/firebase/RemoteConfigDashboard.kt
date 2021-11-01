package com.tarripoha.android.firebase

/*
* Sample Response
[
  {
    "type": "word",
    "lang": "mr",
    "category": "most-viewed",
    "key":"mr-most-viewed"
  },
  {
	"type" : "google-ad
  },
  {
    "type": "word",
    "lang": "mr",
    "category": "top-liked",
    "key":"mr-top-liked"
  }
]
*/
data class DashboardResponse(
    val type: String,
    val lang: String?,
    val category: String?,
    val key: String?
)

