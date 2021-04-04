package com.tarripoha.android.data.model

data class User(
  val id: String,
  val name: String,
  val email: String,
  val city: String,
  val comment: String,
  val admin: Boolean,
  val timestamp: String,
)