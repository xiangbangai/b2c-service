package com.huateng.data.db1.mapper;


import com.huateng.data.model.db1.TblCardUser;
import com.huateng.data.vo.params.CardUser;

import java.util.List;

public interface TblCardUserMapper {

    List<TblCardUser> queryCouponPageList(CardUser cardUser);
}