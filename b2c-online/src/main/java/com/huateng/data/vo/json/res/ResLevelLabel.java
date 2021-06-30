package com.huateng.data.vo.json.res;

import lombok.Data;

import java.util.List;

/**
 * Created with b2c-service.
 * User: Sam
 * Date: 2020/4/26
 * Time: 15:31
 * Description:
 */
@Data
public class ResLevelLabel {

    private String custId;
    private List<LevelLabelInfo> list;
}
