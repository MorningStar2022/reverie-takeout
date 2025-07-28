package com.whurs.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.whurs.annotation.AutoFill;
import com.whurs.constant.MessageConstant;
import com.whurs.constant.StatusConstant;
import com.whurs.dto.SetmealDTO;
import com.whurs.dto.SetmealPageQueryDTO;
import com.whurs.entity.Dish;
import com.whurs.entity.Setmeal;
import com.whurs.entity.SetmealDish;
import com.whurs.enumeration.OperationType;
import com.whurs.exception.DeletionNotAllowedException;
import com.whurs.exception.SetmealEnableFailedException;
import com.whurs.mapper.DishMapper;
import com.whurs.mapper.SetmealDishMapper;
import com.whurs.mapper.SetmealMapper;
import com.whurs.result.PageResult;
import com.whurs.service.SetmealService;
import com.whurs.vo.DishItemVO;
import com.whurs.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.pageQuery(setmealPageQueryDTO);
        PageResult pageResult=new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @AutoFill(value = OperationType.INSERT)
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        //向套餐表中插入一条套餐记录
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        //向套餐-菜品表中插入多条记录
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            //判断套餐是否起售，若是，不允许删除
            Setmeal setmeal=setmealMapper.getById(id);
            if(setmeal.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //批量删除套餐
        setmealMapper.deleteBatch(ids);
        //批量删除套餐菜品表对应的套餐与菜品
        setmealDishMapper.deleteBatch(ids);

    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //根据id查询套餐，还差关联的菜品
        Setmeal setmeal=setmealMapper.getById(id);
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        //根据套餐id查关联的菜品集合
        List<SetmealDish> setmealDishes=setmealDishMapper.getBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO) {
        //更新套餐表基本信息
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        Long setmealId = setmealDTO.getId();
        setmealMapper.update(setmeal);
        //先删除该套餐id在套餐-菜品表中关联的所有菜品
        setmealDishMapper.deleteById(setmealId);
        //更新套餐-菜品表中与该套餐关联的菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 修改套餐售卖状态
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //若套餐包含已停售菜品，则不能起售
        if(status.equals(StatusConstant.ENABLE)){
            List<Dish> dishList=dishMapper.getBySetmealId(id);
            if(dishList!=null&&dishList.size()>0){
                dishList.forEach(dish -> {
                    if(dish.getStatus().equals(StatusConstant.DISABLE)){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });

            }
        }
        Setmeal setmeal=new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealMapper.update(setmeal);
    }

    /**
     * 动态查询套餐
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    /**
     * 根据套餐id查询对应菜品集合
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemById(id);

    }
}
