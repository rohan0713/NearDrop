package com.rohan.neardrop.data.mapper

/**
 * Generic abstraction for mapping between data structures across architectural boundaries.
 * Demonstrates Interface Segregation and Single Responsibility Principles.
 */
interface Mapper<in From, out To> {
    fun map(from: From): To

    fun mapList(fromList: List<From>): List<To> {
        return fromList.map { map(it) }
    }
}

/**
 * Bi-directional contract for two-way entity <-> DTO translation.
 */
interface BiDirectionalMapper<From, To> : Mapper<From, To> {
    fun mapBack(to: To): From

    fun mapBackList(toList: List<To>): List<From> {
        return toList.map { mapBack(it) }
    }
}
