package com.elewa.arkleaptask.core.model

sealed class DomainExceptions:Throwable() {

    object UnknownException:DomainExceptions()

}