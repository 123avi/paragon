package org.paragontech.controllers

import org.paragontech.charger.CommandQueueManager
import org.paragontech.typesaliases.ChargerId
import org.paragontech.typesaliases.IdTag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlinx.coroutines.runBlocking
import org.paragontech.route.HandlerResponse

@RestController
@RequestMapping("/commands")
class CommandController (
    private val commandQueueManager: CommandQueueManager
) {
    @PostMapping("/start")
    fun startTransaction(@RequestParam chargerId: ChargerId, @RequestParam idTag: IdTag) = runBlocking {
        return@runBlocking try {
            commandQueueManager.queueRemoteStartTransaction(chargerId, idTag)
            HandlerResponse.Success(body ="Transaction started successfully").toResponseEntity()
        }catch (e: Exception){
            HandlerResponse.internalServerError("Error starting transaction: ${e.message}").toResponseEntity()
        }
    }

}