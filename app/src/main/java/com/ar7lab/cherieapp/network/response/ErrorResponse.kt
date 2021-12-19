package com.ar7lab.cherieapp.network.response

class ErrorResponse : BaseResponse<ErrorResponse.ErrorData>() {
	class ErrorData(
		val isVerified: Boolean? = null
	)
}
