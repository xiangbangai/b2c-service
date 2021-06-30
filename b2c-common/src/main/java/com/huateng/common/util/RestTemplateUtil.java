package com.huateng.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;


/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/2
 * Time: 17:18
 * Description:
 */
@Slf4j
public class RestTemplateUtil {

    private RestTemplateUtil() {
    }

    static RestTemplate restTemplate;

    static{
        OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory();
        factory.setReadTimeout(5000);//单位为ms
        factory.setConnectTimeout(5000);//单位为ms
        restTemplate = new RestTemplate(factory);
    }

    public static String httpPostForWeChat(String url, String data, HttpHeaders headers) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        if (headers != null) {
            httpHeaders.putAll(headers);
        }
        HttpEntity<String> stringHttpEntity = new HttpEntity<String>(data, httpHeaders);
        return restTemplate.postForObject(url, stringHttpEntity, String.class);
    }


    public static String httpPostUrl(String url, Map<String,Object> data, HttpHeaders headers) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        if (headers != null) {
            httpHeaders.putAll(headers);
        }
        int i = 0;
        StringBuilder stringBuilder = new StringBuilder(url);
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (i == 0) {
                stringBuilder.append("?");
            } else {
                stringBuilder.append("&");
            }
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            i++;
        }
        return restTemplate.postForObject(stringBuilder.toString(), null, String.class);
    }

    public static String httpPostUrl(String url, MultiValueMap<String, String> params) {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<?> entity = new HttpEntity<>(httpHeaders);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParams(params);

        ResponseEntity<String> responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.POST, entity, String.class);
        return responseEntity.getBody();
    }

}
