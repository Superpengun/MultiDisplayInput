package com.sinovoice.ai.nlu


data class StartSessionRequest(
    val Config: Config,
    val robotId: String
)

data class Config(
    val multiIntent: Boolean,
    val needContext: Boolean
)

data class StartSessionResBean(
    val result: StartSessionResult,
    val traceToken: String
)

data class StartSessionResult(
    val robotId: String,
    val sessionId: String
)

data class StopSessionRequest(
    val sessionId: String
)

data class NLUTokenResBean(
    val life: Int,
    val refresh_token: String,
    val token: String
)

data class Result(
    val robotId: String,
    val sessionId: String
)

data class StopSessionResBean(
    val result: Result
)

public data class NluDialogResult(
    val responses: NluResult? = null
)

data class NluCmdBean(
    val content: String,
    val intent: String,
    val skill: String
)

data class NluResult(val content: String?, val cmd: String? = "", val isDialogEnd: Boolean = true)

data class DialogResBean(
    val result: DialogResult?,
    val traceToken: String?
)

data class DialogResult(
    val dialogContext: DialogContext?,
    val responses: List<NluResponse>,
    val robotId: String?,
    val sessionId: String?,
    val context: IntentContext?
)

data class DialogContext(
    val enterSkill: EnterSkill?,
    val intent: Intent?,
    val vars: Vars?
)

data class IntentContext(
    val intent: Intent?
)


data class NluResponse(
    val answerId: Int?,
    val cmd: String?,
    val content: String?,
    val id: Int?,
    val type: String?
)

data class EnterSkill(
    val id: Int?,
    val name: String?,
    val type: String?
)

data class Intent(
    val id: Int?,
    val name: String?,
    val state: String?
)

data class Vars(
    val appKey: String?,
    val global: Global?,
    val hitQuestion: HitQuestion?,
    val slots: Slots?,
    val user: String?
)

data class Global(
    val round: Int?,
    val tenant: Tenant?
)

data class Slots(
    val windspeed: String
)

data class HitQuestion(
    val ext: Ext?,
    val id: Int?,
    val label: String?,
    val score: Double?,
    val text: String?
)

data class DialogRequest(
    val config: Config,
    val sessionId: String,
    val userQuery: String
)

data class Tenant(
    val id: String?
)

data class Ext(
    val id: Int?,
    val text: String?
)


data class Context(
    val enterSkill: EnterSkill,
    val intent: Intent,
    val vars: Vars
)

data class Response(
    val answerId: Int,
    val cmd: String,
    val content: String,
    val id: Int,
    val type: String
)
