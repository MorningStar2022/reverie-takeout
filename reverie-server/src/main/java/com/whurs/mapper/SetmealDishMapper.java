package com.whurs.mapper;

import com.whurs.annotation.AutoFill;
import com.whurs.entity.SetmealDish;
import com.whurs.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据ids查询关联套餐
     * @param dishIds
     * @return
     */
    List<Long> getByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐与菜品关系数据
     * @param setmealDishes
     */
    @AutoFill(value = OperationType.INSERT)
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据传入的套餐ids删除对应的套餐与关联菜品
     * @param setmealIds
     */
    void deleteBatch(List<Long> setmealIds);

    /**
     * 根据套餐id查询对应菜品集合
     * @param setmealId
     * @return
     */
    List<SetmealDish> getBySetmealId(Long setmealId);

    /**
     * 根据id删除该套餐对应菜品
     * @param setmealId
     */
    void deleteById(Long setmealId);
}
