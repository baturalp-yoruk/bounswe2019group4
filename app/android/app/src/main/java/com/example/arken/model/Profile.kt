package com.example.arken.model

import java.io.Serializable

class Profile(
    val user: User?,
    val following: Int?,
    val followings: MutableList<FollowRequest>?,
    val follower: Int?,
    val followers: MutableList<FollowRequest>?,
    val followStatus: String?,
    val followRequest: Int?,
    val followRequests: MutableList<FollowRequest>,
    val articles: List<Article>,
    val portfolios: MutableList<Portfolio>?,
    val followingPortfolios: MutableList<FollowingPortfolio>?
) : Serializable