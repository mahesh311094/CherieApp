package com.ar7lab.cherieapp.enums

enum class NotificationTypeEnum(val value: String) {
    CREATE_ACCOUNT("1"), CREATE_PROFILE("2"),
    UPDATE_PROFILE("3"), FOLLOW("4"),
    ADD_ART("5"), LIKE("6"), COMMENT("7")
}