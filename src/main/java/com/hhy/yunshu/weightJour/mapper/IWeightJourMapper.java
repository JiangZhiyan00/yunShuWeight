package com.hhy.yunshu.weightJour.mapper;

import com.hhy.yunshu.weightJour.entity.WeightJour;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IWeightJourMapper {

    List<WeightJour> queryAllOverWeightList();

    List<WeightJour> queryOverWeightListByCenter(String centerName);
}