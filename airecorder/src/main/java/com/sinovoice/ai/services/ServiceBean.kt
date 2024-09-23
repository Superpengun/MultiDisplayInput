package com.sinovoice.ai.services

/**
 *  {
"robotCode": "zcVM7R",
"channelCode": "默认",
"sessQaType": "QA",
"fromUser": "123",
"isNeedClearHistory": 0,
"isQuestionQuery": 0,
"question": "你好",
"sendTime": 1605934161000,
"sessionId": "222",
"tagUids": [],
"type": "text"
}
 */
data class ServiceRequestBean(
    val robotCode: String,
    val channelCode: String = "默认",
    val sessQaType: String = "QA",
    val fromUser: String = "ai_health",
    val question: String,
    val sendTime: Long,
    val sessionId: String
)

data class KeFuResult(
    val answerResult: AnswerResult,
    val answerType: Int,
    val answerTypeName: String,
    val emotionStatus: Int,
    val permissionFlag: Int,
    val sessionId: String,
    val transferType: Int,
    val uid: String
)

data class AnswerResult(
    val answer: String,
    var CMD: String,
    val error: Boolean = false,
    val fileAnswers: List<FileAnswer>? = null
)

data class FileAnswer(
    val type: Int,
    val title: String,
    val url: String,
    val hyperlink: String
)