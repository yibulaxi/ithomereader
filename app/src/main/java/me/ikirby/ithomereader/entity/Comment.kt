package me.ikirby.ithomereader.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment(
    val nick: String,
    val floor: String,
    val posAndTime: String,
    val content: String,
    val device: String,
    val parentId: String? = null,
    val selfId: String,
    var supportCount: Int = 0,
    var againstCount: Int = 0,
    var isVotedSupport: Boolean = false,
    var isVotedAgainst: Boolean = false
) : Parcelable
