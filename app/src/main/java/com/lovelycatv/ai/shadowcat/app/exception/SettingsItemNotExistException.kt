package com.lovelycatv.ai.shadowcat.app.exception

class SettingsItemNotExistException(key: String) : RuntimeException("Setting item with key '$key' does not exist") {
}