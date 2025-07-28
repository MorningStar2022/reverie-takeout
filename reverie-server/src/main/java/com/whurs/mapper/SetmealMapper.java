package com.whurs.mapper;

import com.github.pagehelper.Page;
import com.whurs.annotation.AutoFill;
import com.whurs.dto.SetmealPageQueryDTO;
import com.whurs.entity.Setmeal;
import com.whurs.enumeration.OperationType;
import com.whurs.vo.DishItemVO;
import com.whurs.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 插入一条套餐记录
     * @param setmeal
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 根据id获取对应套餐
     * @param id
     * @return
     */
    Setmeal getById(Long id);

    /**
     * 根据ids批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 更新套餐表
     * @param setmeal
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 动态查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询对应菜品集合
     * @param setmealId
     * @return
     */
    @Select("select d.name,d.description,d.image,sd.copies from setmeal_dish sd left join dish d on d.id = sd.dish_id "+
    "where sd.setmeal_id=#{setmealId}")
    List<DishItemVO> getDishItemById(Long setmealId);

    Integer countByMap(Map map);

}
