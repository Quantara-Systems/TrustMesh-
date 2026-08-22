package com.trustmesh.data.local

import androidx.room.TypeConverter
import com.trustmesh.domain.model.Category
import com.trustmesh.domain.model.EscalationRule
import com.trustmesh.domain.model.AgentStatus
import com.trustmesh.domain.model.WindowType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromCategoryList(value: List<Category>): String = Json.encodeToString(value)

    @TypeConverter
    fun toCategoryList(value: String): List<Category> = Json.decodeFromString(value)

    @TypeConverter
    fun fromEscalationRules(value: List<EscalationRule>): String = Json.encodeToString(value)

    @TypeConverter
    fun toEscalationRules(value: String): List<EscalationRule> = Json.decodeFromString(value)

    @TypeConverter
    fun fromAgentStatus(value: AgentStatus): String = value.name

    @TypeConverter
    fun toAgentStatus(value: String): AgentStatus = AgentStatus.valueOf(value)

    @TypeConverter
    fun fromWindowType(value: WindowType): String = value.name

    @TypeConverter
    fun toWindowType(value: String): WindowType = WindowType.valueOf(value)
}
