package com.tarripoha.android.data.model

data class User(
  val id: String,
  val name: String,
  val phone: String,
  val email: String,
  var emailVerified: Boolean? = null,
  val city: String? = null,
  var dirty: Boolean? = null,
  val admin: Boolean = false,
  val timestamp: Long,
) {
  constructor() : this(
      id = "", name = "", phone = "", email = "",
      admin = false, timestamp = 0L
  )
}
