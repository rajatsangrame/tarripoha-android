package com.tarripoha.android.data.model

import com.google.gson.annotations.SerializedName

/*
* Sample Response
{
  "wordOfTheDay": "",
  "labeledViews": [
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
    val wordOfTheDay: String,
    val labeledViews: List<LabeledView>
)

data class LabeledView(
    val type: String,
    val lang: String?,
    val category: String?,
)

