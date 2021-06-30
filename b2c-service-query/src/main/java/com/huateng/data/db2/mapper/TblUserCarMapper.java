package com.huateng.data.db2.mapper;

import com.huateng.data.model.db2.TblUserCar;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TblUserCarMapper {

    List<TblUserCar> queryUserCarsByPlateNumber(@Param("plateNumber") String plateNumber);
}
