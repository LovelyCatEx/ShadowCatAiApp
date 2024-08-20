package com.lovelycatv.ai.shadowcat.app.net.retrofit;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSON;
import com.lovelycatv.ai.shadowcat.app.net.response.NetworkResult;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class FastJsonConverterFactory extends Converter.Factory {

    public static FastJsonConverterFactory create() {
        return new FastJsonConverterFactory();
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            @NonNull Type type,
            @NonNull Annotation[] parameterAnnotations,
            @NonNull Annotation[] methodAnnotations,
            @NonNull Retrofit retrofit
    ) {
        return new FastJsonRequestBodyConverter<>();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            @NonNull Type type,
            @NonNull Annotation[] annotations,
            @NonNull Retrofit retrofit
    ) {
        return new FastJsonResponseBodyConverter<>(type);
    }

    private static class FastJsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
        private final Type type;

        FastJsonResponseBodyConverter(Type type) {
            this.type = type;
        }

        @Override
        public T convert(ResponseBody value) throws IOException {
            String resStr = value.string();
            System.out.println("Remote => " + resStr);
            return (T) NetworkResult.fromJSONString(resStr);
        }
    }

    private static class FastJsonRequestBodyConverter<T> implements Converter<T, RequestBody> {
        @Override
        public RequestBody convert(T value) {
            System.out.println("Convert => " + JSON.toJSONString(value));
            return RequestBody.create(null, JSON.toJSONString(value));
        }
    }
}