package org.paragontech.common.fp

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

interface FpQueue<T>  {
    fun enqueue(item: T): FpQueue<T>
    fun dequeue(): FpQueue<T>
    fun peek(): T?
    fun isEmpty(): Boolean
    fun toList(): List<T>

}

class PersistentFpQueue<T> (
    private val queue: PersistentList<T> = persistentListOf()

): FpQueue<T> {
    override fun enqueue(item: T): FpQueue<T> = PersistentFpQueue(queue.add(item))
    override fun dequeue(): FpQueue<T> =
        if (queue.isEmpty()) {
            this
        } else {
            PersistentFpQueue(queue.removeAt(0))
        }


    override fun peek(): T? = queue.firstOrNull()

    override fun isEmpty(): Boolean = queue.isEmpty()

    override fun toList(): List<T> = queue

}

fun <T> fpQueueOf(vararg elements: T): FpQueue<T> =
    PersistentFpQueue(persistentListOf(*elements))
