// File: src/main/java/com/example/tradeup/core/utils/Event.java
package com.example.tradeup.core.utils;

/**
 * Một lớp wrapper cho dữ liệu được cung cấp bởi LiveData mà chỉ nên được xử lý một lần.
 * Ví dụ như hiển thị một SnackBar, một Toast, hoặc điều hướng đến màn hình khác.
 * @param <T> Kiểu dữ liệu của nội dung.
 */
public class Event<T> {

    private final T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Trả về nội dung và ngăn không cho nó được sử dụng lại.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Trả về nội dung, ngay cả khi nó đã được xử lý.
     */
    public T peekContent() {
        return content;
    }
}