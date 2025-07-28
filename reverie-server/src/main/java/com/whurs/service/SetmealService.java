package com.whurs.service;

import com.whurs.dto.SetmealDTO;
import com.whurs.dto.SetmealPageQueryDTO;
import com.whurs.entity.Setmeal;
import com.whurs.result.PageResult;
import com.whurs.vo.DishItemVO;
import com.whurs.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     */
    void updateWithDish(SetmealDTO setmealDTO);

    /**
     * 修改套餐售卖状态
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 动态查询套餐
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询对应菜品集合
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
