package com.lovelycatv.ai.shadowcat.app.im.vm

/**
 * StreamingDataPack from remote server
 *
 * @property isNewStream If this is the start of the response, the value will be true
 * @property streamId Unique StreamId
 * @property sessionId SessionId
 * @property messageId If the response finished, this param is assistant messageId in remote database
 * @property data Stream Data
 * @property completed If the response finished, this param will be true
 */
data class StreamingDataPack(
    var isNewStream: Boolean,
    var streamId: String,
    var sessionId: String,
    var messageId: Long,
    var data: String,
    var completed: Boolean
) {
}