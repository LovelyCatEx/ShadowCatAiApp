package com.lovelycatv.ai.shadowcat.app.exception.im

class StreamingDataReceiveException(message: String) : RuntimeException(message) {
    constructor() : this("Streaming data completion message received, but data is not record in local")
}