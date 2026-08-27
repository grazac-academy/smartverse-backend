package org.smartvert.smartvert.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"message", "data"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String message;

    private final T data;

    private ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(final String message, final T data) {
        return new ApiResponse<>(message, data);
    }

    public static <T> ApiResponse<T> failure(final String message) {
        return new ApiResponse<>(message, null);
    }
}
