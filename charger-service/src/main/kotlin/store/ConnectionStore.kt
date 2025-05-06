package org.paragontech.store

interface ConnectionStore {
    fun storeConnection(connectionId: String, chargerId: String): Boolean
    fun removeConnection(connectionId: String): Boolean
    fun getChargerId(connectionId: String): String?
}
class InMemoryConnectionStore : ConnectionStore {
    private val connections = mutableMapOf<String, String>()

    override fun storeConnection(connectionId: String, chargerId: String): Boolean {
        connections[connectionId] = chargerId
        return true
    }

    override fun removeConnection(connectionId: String): Boolean {
        return connections.remove(connectionId) != null
    }

    override fun getChargerId(connectionId: String): String? {
        return connections[connectionId]
    }
}