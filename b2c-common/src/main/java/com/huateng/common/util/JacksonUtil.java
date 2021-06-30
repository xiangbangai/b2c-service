package com.huateng.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.StringWriter;

/**
 * @User Sam
 * @Date 2018/4/19
 * @Time 15:44
 * @param 
 * @return 
 * @Description java对象转json
 */
public class JacksonUtil {

    private JacksonUtil() {
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object obj) throws Exception{
        StringWriter stringWriter = new StringWriter();
        if (obj == null) {
            return null;
        }
        /**
         * 与application.yml中的default-property-inclusion设置匹配
         * 保证返回签名原文与返回报文一致
         */
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.writeValue(stringWriter, obj);
        return stringWriter.toString();
    }

    public static <T> T toObject(String json, Class<T> c) throws Exception{
        return json == null ? null : objectMapper.readValue(json, c);
    }

    public static <T> T toObject(String json, JavaType javaType) throws Exception{
        return json == null ? null : objectMapper.readValue(json, javaType);
    }

    public static <T> T toObject(InputStream inputStream, Class<T> c) throws Exception{
        return inputStream == null ? null : objectMapper.readValue(inputStream, c);
    }

    public static <T> T toObject(String json, TypeReference typeReference) throws Exception{
        return json == null ? null : objectMapper.readValue(json, typeReference);
    }

    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static JavaType getObjectType(Class<?> obj, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(obj, elementClasses);
    }

}
