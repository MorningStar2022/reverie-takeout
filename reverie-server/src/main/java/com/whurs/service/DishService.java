package com.whurs.service;

import com.whurs.dto.DishDTO;
import com.whurs.dto.DishPageQueryDTO;
import com.whurs.entity.Dish;
import com.whurs.result.PageResult;
import com.whurs.vo.DishVO;

import java.util.List;

public interface DishService {

    /**
     * 新增菜品
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据Id查询菜品
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 菜品起售或停售
     * @param id
     * @param status
     */
    void startOrStop(Long id, Integer status);

    /**
     * 根据分类获取菜品
     * @param categoryId
     * @return
     */
    List<Dish> getByCategory(Long categoryId);


    /**
     * 根据分类id查询菜品集合
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
