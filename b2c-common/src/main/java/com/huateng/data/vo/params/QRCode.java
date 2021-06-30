package com.huateng.data.vo.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QRCode {
    private String code;//二维码
    @Builder.Default
    private Boolean isCheckTime = true;//是否校验超时
    @Builder.Default
    private Boolean isCheckRepeat = true;//是否校验重复使用
}
