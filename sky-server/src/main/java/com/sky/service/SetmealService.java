package com.sky.service;

import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SetmealService {
    /**
     * 新增套餐
     *
     * @param setmealDTO
     */
     void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 根据ID查询套餐
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);




    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 套餐起售、停售
     * @param setmealId
     * @param status
     */
    void startOrStop(Long setmealId, Integer status);


    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    void update(SetmealDTO setmealDTO);
}
