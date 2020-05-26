package com.tencent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author DK
 * @date 2020/5/26 15:25
 * @describe：
 */

@AllArgsConstructor
@Getter
@ToString
public enum DllSuffixEnum {

    WINDOWS(1,".dll"),
    LINUX(2,".os")
    ;

    //状态值
    Integer type;
    //状态值描述
    String vaule;
}
