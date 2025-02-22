package server.apigateway.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
data class ApiResponse<T>(
    val status: String,
    val code: Int,
    val message: String,
    val data: T? = null,
    val error: String? = null
) {
    companion object {
        fun <T> success(data: T, message: String = "요청이 성공적으로 처리되었습니다."): ApiResponse<T> {
            return ApiResponse("success", 200, message, data, null)
        }

        fun error(code: Int, message: String, error: String? = null): ApiResponse<Nothing> {
            return ApiResponse("error", code, message, null, error)
        }
    }
}

inline fun <reified T> ApiResponse<T>.toJson(): String {
    return Json.encodeToString(this)
}