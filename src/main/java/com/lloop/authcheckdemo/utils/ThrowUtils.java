package com.lloop.authcheckdemo.utils;

import com.lloop.authcheckdemo.common.BusinessException;
import com.lloop.authcheckdemo.common.ErrorCode;

public class ThrowUtils {

	public static void throwIf(boolean condition, ErrorCode errorCode) {
		throwIf(condition, new BusinessException(errorCode));
	}

	public final void throwIf(boolean condition, ErrorCode errorCode, String message) {
		throwIf(condition, new BusinessException(errorCode, message));
	}

	public static void throwIf(boolean condition, RuntimeException runtimeException) {
		if(condition) {
			throw runtimeException;
		}
	}

}