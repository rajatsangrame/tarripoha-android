package com.tarripoha.android.firebase

import com.google.gson.annotations.SerializedName

/*
* Sample Response
{
  "word_of_the_day": "",
  "labeled_views": [
    {
      "type": "word",
      "lang": "Marathi",
      "category": "most-viewed",
      "key": "mr-most-viewed"
    },
    {
      "type": "word",
      "lang": "Hindi",
      "category": "top-liked",
      "key": "hn-top-liked"
    },
    {
      "type": "word",
      "lang": "Marathi",
      "category": "top-liked",
      "key": "mr-top-liked"
    }
  ]
}
*/
data class DashboardResponse(
    @SerializedName("word_of_the_day")
    val wordOfTheDay: String,
    @SerializedName("labeled_views")
    val labeledViews: List<LabeledView>
)

data class LabeledView(
    val type: String,
    val lang: String?,
    val category: String?,
    val key: String?
)

