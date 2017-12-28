package org.seckill.dto;

/**
 * 封装json数据类型的
 *  所有的ajax请求的返回结果都是这个
 * @param <T>
 */
public class SeckillResult<T> {

    // 状态信息 (我原来常写成int state 类似于http的状态码)
    private boolean success;

    // 封装的数据 一般就是json数据 (原来常用Object 现在有泛型啊)
    private T data;

    private String error;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
