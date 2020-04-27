package com.egm.stellio.entity.service

import org.springframework.cloud.stream.annotation.Input
import org.springframework.messaging.SubscribableChannel

interface SubscriptionSink {

    @Input("cim.subscription")
    fun subscriptionsInput(): SubscribableChannel

    @Input("cim.notification")
    fun notificationsInput(): SubscribableChannel
}
