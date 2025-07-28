package com.whurs.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.whurs.constant.MessageConstant;
import com.whurs.constant.StatusConstant;
import com.whurs.dto.DishDTO;
import com.whurs.dto.DishPageQueryDTO;
import com.whurs.entity.Dish;
import com.whurs.entity.DishFlavor;
import com.whurs.exception.DeletionNotAllowedException;
import com.whurs.mapper.DishFlavorMapper;
import com.whurs.mapper.DishMapper;
import com.whurs.mapper.SetmealDishMapper;
import com.whurs.result.PageResult;
import com.whurs.service.DishService;
import com.whurs.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 新增菜品
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //向菜品表插入一条菜品数据
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        //向口味表插入多条口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        PageResult pageResult=new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {

        for (Long id : ids) {
            //判断菜品是否起售，若起售，不允许删除
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断菜品是否关联套餐，若是，不允许删除
        List<Long> setmealIds=setmealDishMapper.getByDishIds(ids);
        if(setmealIds!=null&&setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            dishFlavorMapper.deleteByDishId(id);
//        }
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }


    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询对应菜品，但是还差口味属性
        Dish dish=dishMapper.getById(id);
        //根据菜品id查询口味
        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        //修改菜品基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //删除该菜品的所有口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //再重新插入传入的新的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品起售或停售
     * @param id
     * @param status
     */
    @Override
    public void startOrStop(Long id, Integer status) {
        Dish dish = dishMapper.getById(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    /**
     * 根据categoryid获取菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getByCategory(Long categoryId) {
        Dish dish=new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        return dishMapper.list(dish);

    }

    /**
     * 根据分类id查询菜品集合
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishes = dishMapper.list(dish);
        List<DishVO> dishVOS=new ArrayList<>();
        for (Dish d : dishes) {
            DishVO dishVO=new DishVO();
            BeanUtils.copyProperties(d,dishVO);
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());
            dishVO.setFlavors(flavors);
            dishVOS.add(dishVO);
        }
        return dishVOS;
    }
}
